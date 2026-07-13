import { z } from 'zod'

export const newsletterSchema = z.object({ email: z.string().trim().email('Enter a valid email address') })

export const profileSchema = z.object({
  firstName: z.string().trim().min(2, 'First name is required'),
  lastName: z.string().trim().min(2, 'Last name is required'),
  email: z.string().trim().email(),
  company: z.string().trim().max(100).optional(),
  bio: z.string().trim().max(500).optional(),
})

export const postSchema = z.object({
  title: z.string().trim().min(5, 'Use at least 5 characters').max(120),
  slug: z.string().trim().regex(/^[a-z0-9]+(?:-[a-z0-9]+)*$/, 'Use lowercase letters, numbers and dashes'),
  excerpt: z.string().trim().min(20).max(300),
  content: z.string().trim().min(30, 'Add more detail to your post'),
  status: z.enum(['draft', 'published']),
  coverImage: z.string().optional(),
})

export type ProfileInput = z.infer<typeof profileSchema>
export type PostInput = z.infer<typeof postSchema>
