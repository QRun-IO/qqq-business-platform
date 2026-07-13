import { afterEach, describe, expect, it, vi } from 'vitest'
import {
  getMemberActivity,
  getMemberContent,
  getMemberProfile,
  getMemberPreferences,
  updateMemberPreferences,
  updateMemberProfile,
} from './member-api'

describe('member API', () => {
  afterEach(() => vi.unstubAllGlobals())

  it('uses the authenticated member endpoint for isolated profile reads', async () => {
    const fetchMock = vi.fn().mockResolvedValue(new Response(JSON.stringify({ id: 7, displayName: 'Ada Lovelace' }), { status: 200 }))
    vi.stubGlobal('fetch', fetchMock)

    await expect(getMemberProfile('token')).resolves.toMatchObject({ id: 7, displayName: 'Ada Lovelace' })
    expect(fetchMock).toHaveBeenCalledWith('/api/member/v1/profile', expect.objectContaining({
      headers: expect.any(Headers),
      cache: 'no-store',
    }))
    expect((fetchMock.mock.calls[0][1].headers as Headers).get('Authorization')).toBe('Bearer token')
  })

  it('maps profile form fields to the backend displayName contract', async () => {
    const fetchMock = vi.fn().mockResolvedValue(new Response(JSON.stringify({ id: 7 }), { status: 200 }))
    vi.stubGlobal('fetch', fetchMock)

    await updateMemberProfile('token', { firstName: ' Ada ', lastName: ' Lovelace ', email: 'ada@example.com', company: '', bio: '' })
    expect(JSON.parse(fetchMock.mock.calls[0][1].body)).toEqual({ displayName: 'Ada Lovelace', email: 'ada@example.com' })
  })

  it('loads exclusive content and activity from authenticated member endpoints', async () => {
    const fetchMock = vi.fn()
      .mockResolvedValueOnce(new Response(JSON.stringify({ items: [] }), { status: 200 }))
      .mockResolvedValueOnce(new Response(JSON.stringify({ items: [] }), { status: 200 }))
    vi.stubGlobal('fetch', fetchMock)

    await getMemberContent('token')
    await getMemberActivity('token')
    expect(fetchMock.mock.calls.map(call => call[0])).toEqual([
      '/api/member/v1/content/posts?page=0&size=12',
      '/api/member/v1/activity?page=0&size=20',
    ])
  })

  it('falls back to published content and profile history while member endpoints are being deployed', async () => {
    const profile = { id: 7, email: 'ada@example.com', displayName: 'Ada', createdAt: '2026-01-01T00:00:00Z', updatedAt: '2026-02-01T00:00:00Z' }
    const fetchMock = vi.fn()
      .mockResolvedValueOnce(new Response('{}', { status: 405 }))
      .mockResolvedValueOnce(new Response(JSON.stringify({ items: [{ id: 'post-1', title: 'Guide' }] }), { status: 200 }))
      .mockResolvedValueOnce(new Response('{}', { status: 404 }))
      .mockResolvedValueOnce(new Response(JSON.stringify(profile), { status: 200 }))
    vi.stubGlobal('fetch', fetchMock)

    await expect(getMemberContent('token')).resolves.toMatchObject({ items: [{ title: 'Guide' }] })
    await expect(getMemberActivity('token')).resolves.toMatchObject({ items: [
      { type: 'profile.updated' },
      { type: 'membership.created' },
    ] })
  })

  it('stores preferences per member when the optional backend endpoint is unavailable', async () => {
    const storage = new Map<string, string>()
    vi.stubGlobal('localStorage', {
      getItem: (key: string) => storage.get(key) ?? null,
      setItem: (key: string, value: string) => storage.set(key, value),
    })
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue(new Response('{}', { status: 404 })))

    await updateMemberPreferences('token', 'member-7', { emailUpdates: false, productNews: true, theme: 'system' })
    await expect(getMemberPreferences('token', 'member-7')).resolves.toMatchObject({ emailUpdates: false, productNews: true })
  })
})
