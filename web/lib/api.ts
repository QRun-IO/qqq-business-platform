const API_ROOT = process.env.NEXT_PUBLIC_API_URL || ''

export function apiUrl(path: string, root = API_ROOT): string {
  const normalizedPath = path.startsWith('/') ? path : `/${path}`
  const normalizedRoot = root.replace(/\/$/, '')

  if (!normalizedRoot) return normalizedPath
  if (normalizedRoot.endsWith('/api') && normalizedPath.startsWith('/api/')) {
    return `${normalizedRoot}${normalizedPath.slice(4)}`
  }
  return `${normalizedRoot}${normalizedPath}`
}

export async function apiFetch<T>(path: string, options: RequestInit = {}, token?: string): Promise<T> {
  const headers = new Headers(options.headers)
  if (!(options.body instanceof FormData)) headers.set('Content-Type', 'application/json')
  if (token) headers.set('Authorization', `Bearer ${token}`)
  const response = await fetch(apiUrl(path), { ...options, headers, cache: 'no-store' })
  const body = await response.json().catch(() => ({}))
  if (!response.ok) throw new Error(body.message || body.error || `Request failed (${response.status})`)
  return body as T
}

export const backendPath = (surface: 'public'|'member'|'staff', path: string) => `/api/${surface}/v1/${path.replace(/^\//, '')}`
