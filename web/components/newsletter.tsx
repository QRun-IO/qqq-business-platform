'use client'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { newsletterSchema } from '@/lib/schemas'
import { z } from 'zod'
import toast from 'react-hot-toast'
import { apiFetch, backendPath } from '@/lib/api'
import { ArrowRight, Loader2 } from 'lucide-react'

export function Newsletter() {
  const { register, handleSubmit, reset, formState: { errors, isSubmitting } } = useForm<z.infer<typeof newsletterSchema>>({
    resolver: zodResolver(newsletterSchema)
  })

  const onSubmit = async (values: z.infer<typeof newsletterSchema>) => {
    try {
      console.log('Newsletter submission starting...', values)
      
      // Use Next.js API route instead of direct backend call (bypasses CORS)
      const response = await fetch('/api/newsletter/subscribe', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(values)
      })
      
      console.log('API response status:', response.status)
      
      if (!response.ok) {
        const errorData = await response.json()
        throw new Error(errorData.error || 'Subscription failed')
      }
      
      const result = await response.json()
      
      console.log('Newsletter subscription result:', result)
      toast.success('Check your inbox to confirm your subscription.')
      reset()
    } catch (error) {
      console.error('Newsletter subscription error:', error)
      toast.error(error instanceof Error ? error.message : 'Unable to subscribe')
    }
  }

  return (
    <form className="newsletter-form" onSubmit={handleSubmit(onSubmit)} noValidate>
      <div>
        <label className="sr-only" htmlFor="newsletter-email">
          Work email
        </label>
        <input 
          id="newsletter-email"
          type="email" 
          aria-invalid={!!errors.email}
          aria-describedby={errors.email ? 'newsletter-email-error' : undefined}
          placeholder="Work email address" 
          {...register('email')}
        />
        {errors.email && (
          <p id="newsletter-email-error">{errors.email.message}</p>
        )}
      </div>
      <button type="submit" disabled={isSubmitting}>
        {isSubmitting ? (
          <>
            <Loader2 className="spin" size={16} /> 
            Subscribing
          </>
        ) : (
          <>
            Join the list 
            <ArrowRight size={16} />
          </>
        )}
      </button>
    </form>
  )
}