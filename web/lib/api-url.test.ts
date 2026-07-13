import { describe, expect, it } from 'vitest'
import { apiUrl } from './api'

describe('API URL construction', () => {
  it('uses the same-origin API path when no public API root is configured', () => {
    expect(apiUrl('/api/public/v1/newsletter/subscribe', '')).toBe('/api/public/v1/newsletter/subscribe')
  })

  it('does not duplicate an API prefix supplied by the public API root', () => {
    expect(apiUrl('/api/public/v1/newsletter/subscribe', 'http://localhost:18082/api')).toBe(
      'http://localhost:18082/api/public/v1/newsletter/subscribe',
    )
  })
})
