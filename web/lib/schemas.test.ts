import { describe, expect, it } from 'vitest'
import { newsletterSchema, postSchema, profileSchema } from './schemas'

describe('business workflow validation', () => {
  it('rejects malformed newsletter addresses', () => {
    expect(newsletterSchema.safeParse({ email: 'not-an-email' }).success).toBe(false)
  })

  it('requires complete member profiles', () => {
    expect(profileSchema.safeParse({ firstName: '', lastName: 'User', email: 'member@example.com' }).success).toBe(false)
  })

  it('requires meaningful blog content and a URL-safe slug', () => {
    expect(postSchema.safeParse({ title: 'Hi', slug: 'Not valid', excerpt: 'short', content: '<p>x</p>', status: 'draft' }).success).toBe(false)
    expect(postSchema.safeParse({
      title: 'A useful platform update', slug: 'useful-platform-update',
      excerpt: 'A concise summary of the latest platform capabilities.',
      content: '<p>This update contains enough useful detail for our members.</p>', status: 'draft'
    }).success).toBe(true)
  })
})
