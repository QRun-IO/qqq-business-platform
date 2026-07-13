import { apiFetch, backendPath } from './api'

export type ContentStatus = 'draft' | 'scheduled' | 'published' | 'archived'
export type ContentBlock = { type: 'html'; html: string } | { type: 'image'; assetId: string; url: string; alt?: string }

export interface BlogPost {
  id: string
  title: string
  slug: string
  excerpt: string
  content: ContentBlock[]
  status: ContentStatus
  publishedAt?: string | null
  createdAt: string
  updatedAt: string
  authorName: string
  categoryIds?: string[]
  tagIds?: string[]
  featuredImageAssetId?: string | null
}

export interface ContentPage<T> {
  items: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}

export interface TaxonomyItem { id: string; name: string; slug: string; description?: string | null }
export interface MediaAsset { id: string; fileName: string; contentType: string; size: number; createdAt?: string; url: string }

export interface PostEditorInput {
  title: string
  slug: string
  excerpt: string
  html: string
  categoryIds: string[]
  tagIds: string[]
  featuredImageAssetId?: string | null
}

const authHeaders = (token: string, json = true) => {
  const headers = new Headers()
  headers.set('Authorization', `Bearer ${token}`)
  if (json) headers.set('Content-Type', 'application/json')
  return headers
}

async function request<T>(path: string, token: string, init: RequestInit = {}): Promise<T> {
  const response = await fetch(path, { ...init, headers: init.headers || authHeaders(token, !(init.body instanceof FormData)), cache: 'no-store' })
  if (response.status === 204) return undefined as T
  const body = await response.json().catch(() => ({}))
  if (!response.ok) throw new Error(body.message || body.error || `Request failed (${response.status})`)
  return body as T
}

const postBody = (input: PostEditorInput) => ({
  title: input.title.trim(), slug: input.slug.trim(), excerpt: input.excerpt.trim(),
  content: [{ type: 'html', html: input.html }],
  categoryIds: input.categoryIds, tagIds: input.tagIds,
  featuredImageAssetId: input.featuredImageAssetId || null,
})

export function contentToHtml(content?: ContentBlock[] | string): string {
  if (typeof content === 'string') return content
  return (content || []).map(block => block.type === 'html' ? block.html : `<img src="${block.url}" alt="${block.alt || ''}">`).join('\n')
}

export function listPosts(token: string, query: { page?: number; size?: number } = {}) {
  const params = new URLSearchParams({ page: String(query.page ?? 0), size: String(query.size ?? 20) })
  return request<ContentPage<BlogPost>>(`${backendPath('staff', 'content/posts')}?${params}`, token)
}

export function getPost(token: string, id: string) { return request<BlogPost>(backendPath('staff', `content/posts/${id}`), token) }
export function createPost(token: string, input: PostEditorInput) { return request<BlogPost>(backendPath('staff', 'content/posts'), token, { method: 'POST', body: JSON.stringify(postBody(input)) }) }
export function updatePost(token: string, id: string, input: PostEditorInput) { return request<BlogPost>(backendPath('staff', `content/posts/${id}`), token, { method: 'PUT', body: JSON.stringify(postBody(input)) }) }
export function publishPost(token: string, id: string) { return request<BlogPost>(backendPath('staff', `content/posts/${id}/publish`), token, { method: 'PUT' }) }
export function unpublishPost(token: string, id: string) { return request<BlogPost>(backendPath('staff', `content/posts/${id}/unpublish`), token, { method: 'PUT' }) }
export function deletePost(token: string, id: string) { return request<void>(backendPath('staff', `content/posts/${id}`), token, { method: 'DELETE' }) }

export function listCategories(token: string) { return request<TaxonomyItem[]>(backendPath('staff', 'content/categories'), token) }
export function createCategory(token: string, input: Omit<TaxonomyItem, 'id'>) { return request<TaxonomyItem>(backendPath('staff', 'content/categories'), token, { method: 'POST', body: JSON.stringify(input) }) }
export function deleteCategory(token: string, id: string) { return request<void>(backendPath('staff', `content/categories/${id}`), token, { method: 'DELETE' }) }
export function listTags(token: string) { return request<TaxonomyItem[]>(backendPath('staff', 'content/tags'), token) }
export function createTag(token: string, input: Omit<TaxonomyItem, 'id'>) { return request<TaxonomyItem>(backendPath('staff', 'content/tags'), token, { method: 'POST', body: JSON.stringify(input) }) }
export function deleteTag(token: string, id: string) { return request<void>(backendPath('staff', `content/tags/${id}`), token, { method: 'DELETE' }) }

export function listMedia(token: string) { return request<MediaAsset[]>(backendPath('staff', 'content/assets'), token) }
export async function uploadMedia(token: string, file: File): Promise<MediaAsset> {
  const form = new FormData(); form.append('file', file)
  const asset = await request<Omit<MediaAsset, 'url'>>(backendPath('staff', 'content/assets'), token, { method: 'POST', body: form })
  return { ...asset, url: backendPath('public', `content/assets/${asset.id}`) }
}
export function deleteMedia(token: string, id: string) { return request<void>(backendPath('staff', `content/assets/${id}`), token, { method: 'DELETE' }) }

export { apiFetch }
