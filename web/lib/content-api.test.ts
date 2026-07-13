import { beforeEach, describe, expect, it, vi } from 'vitest'
import { createPost, deletePost, listPosts, publishPost, uploadMedia } from './content-api'

const fetchMock = vi.fn()
vi.stubGlobal('fetch', fetchMock)

beforeEach(() => fetchMock.mockReset())

describe('content API', () => {
  it('lists staff posts with bearer authentication and pagination', async () => {
    fetchMock.mockResolvedValue(new Response(JSON.stringify({ items: [], page: 1, size: 25, totalElements: 0, totalPages: 0 }), { status: 200 }))
    await listPosts('token', { page: 1, size: 25 })
    expect(fetchMock).toHaveBeenCalledWith('/api/staff/v1/content/posts?page=1&size=25', expect.objectContaining({ headers: expect.any(Headers) }))
    expect((fetchMock.mock.calls[0][1].headers as Headers).get('Authorization')).toBe('Bearer token')
  })

  it('serializes rich HTML into the backend content block contract', async () => {
    fetchMock.mockResolvedValue(new Response(JSON.stringify({ id: '1' }), { status: 201 }))
    await createPost('token', { title: 'A useful story', slug: 'a-useful-story', excerpt: 'A sufficiently detailed excerpt.', html: '<p>Hello</p>', categoryIds: [], tagIds: [] })
    const body = JSON.parse(fetchMock.mock.calls[0][1].body)
    expect(body.content).toEqual([{ type: 'html', html: '<p>Hello</p>' }])
    expect(fetchMock.mock.calls[0][0]).toBe('/api/staff/v1/content/posts')
  })

  it('uses explicit publish and delete endpoints', async () => {
    fetchMock.mockResolvedValue(new Response('{}', { status: 200 }))
    await publishPost('token', 'abc')
    expect(fetchMock.mock.calls[0][0]).toBe('/api/staff/v1/content/posts/abc/publish')
    fetchMock.mockResolvedValue(new Response(null, { status: 204 }))
    await deletePost('token', 'abc')
    expect(fetchMock.mock.calls[1][0]).toBe('/api/staff/v1/content/posts/abc')
    expect(fetchMock.mock.calls[1][1].method).toBe('DELETE')
  })

  it('uploads media as multipart and exposes its public URL', async () => {
    fetchMock.mockResolvedValue(new Response(JSON.stringify({ id: 'asset-1', fileName: 'hero.png', contentType: 'image/png', size: 3 }), { status: 201 }))
    const file = new File(['img'], 'hero.png', { type: 'image/png' })
    const asset = await uploadMedia('token', file)
    expect(asset.url).toBe('/api/public/v1/content/assets/asset-1')
    expect(fetchMock.mock.calls[0][0]).toBe('/api/staff/v1/content/assets')
    expect(fetchMock.mock.calls[0][1].body).toBeInstanceOf(FormData)
  })
})
