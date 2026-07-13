import { apiFetch, backendPath } from './api'
import type { ProfileInput } from './schemas'

export interface MemberProfile {
  id: number
  subject?: string
  email: string
  displayName: string
  createdAt?: string
  updatedAt?: string
}

export interface MemberContentItem {
  id: string
  title: string
  slug?: string
  excerpt?: string
  publicationState?: string
  publishedAt?: string
  updatedAt?: string
  content?: unknown
}

export interface MemberActivityItem {
  id: string
  type: string
  title: string
  description?: string
  occurredAt: string
}

export interface Page<T> { items: T[]; page: number; size: number; total: number; totalPages: number }
export interface MemberPreferences {
  emailUpdates: boolean
  productNews: boolean
  weeklyDigest: boolean
  activityEmails: boolean
  theme: 'system' | 'light' | 'dark'
}

export const defaultMemberPreferences: MemberPreferences = {
  emailUpdates: true,
  productNews: true,
  weeklyDigest: true,
  activityEmails: false,
  theme: 'system',
}

const preferenceKey = (memberId: string) => `qqq:member-preferences:${memberId}`

export function getMemberProfile(token: string) {
  return apiFetch<MemberProfile>(backendPath('member', 'profile'), {}, token)
}

export function updateMemberProfile(token: string, profile: ProfileInput) {
  return apiFetch<MemberProfile>(backendPath('member', 'profile'), {
    method: 'PUT',
    body: JSON.stringify({
      displayName: `${profile.firstName.trim()} ${profile.lastName.trim()}`.trim(),
      email: profile.email.trim(),
    }),
  }, token)
}

export async function getMemberContent(token: string, page = 0, size = 12) {
  try {
    return await apiFetch<Page<MemberContentItem>>(`${backendPath('member', 'content/posts')}?page=${page}&size=${size}`, {}, token)
  } catch {
    // Older Identity deployments expose published resources through the public read endpoint.
    return apiFetch<Page<MemberContentItem>>(`${backendPath('public', 'content/posts')}?page=${page}&size=${size}`)
  }
}

export async function getMemberActivity(token: string, page = 0, size = 20) {
  try {
    return await apiFetch<Page<MemberActivityItem>>(`${backendPath('member', 'activity')}?page=${page}&size=${size}`, {}, token)
  } catch {
    const profile = await getMemberProfile(token)
    const items: MemberActivityItem[] = []
    if (profile.updatedAt) items.push({ id: `profile-${profile.id}`, type: 'profile.updated', title: 'Profile details updated', occurredAt: profile.updatedAt })
    if (profile.createdAt) items.push({ id: `joined-${profile.id}`, type: 'membership.created', title: 'Membership activated', occurredAt: profile.createdAt })
    return { items: items.slice(page * size, page * size + size), page, size, total: items.length, totalPages: items.length ? 1 : 0 }
  }
}

function readStoredPreferences(memberId: string): MemberPreferences {
  if (typeof localStorage === 'undefined') return defaultMemberPreferences
  try {
    return { ...defaultMemberPreferences, ...JSON.parse(localStorage.getItem(preferenceKey(memberId)) || '{}') }
  } catch {
    return defaultMemberPreferences
  }
}

export async function getMemberPreferences(token: string, memberId: string): Promise<MemberPreferences> {
  try {
    return { ...defaultMemberPreferences, ...await apiFetch<Partial<MemberPreferences>>(backendPath('member', 'preferences'), {}, token) }
  } catch {
    return readStoredPreferences(memberId)
  }
}

export async function updateMemberPreferences(token: string, memberId: string, preferences: MemberPreferences) {
  try {
    return await apiFetch<MemberPreferences>(backendPath('member', 'preferences'), {
      method: 'PUT', body: JSON.stringify(preferences),
    }, token)
  } catch {
    if (typeof localStorage !== 'undefined') localStorage.setItem(preferenceKey(memberId), JSON.stringify(preferences))
    return preferences
  }
}

export function memberInitials(profile?: Pick<MemberProfile, 'displayName' | 'email'>) {
  const source = profile?.displayName?.trim() || profile?.email || 'Member'
  return source.split(/\s+/).slice(0, 2).map(part => part[0]?.toUpperCase()).join('')
}

export function formatMemberDate(value?: string) {
  if (!value) return 'Recently'
  const date = new Date(value)
  return Number.isNaN(date.valueOf()) ? 'Recently' : new Intl.DateTimeFormat('en', { month: 'short', day: 'numeric', year: 'numeric' }).format(date)
}
