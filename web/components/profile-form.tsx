'use client'

import { useEffect } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import toast from 'react-hot-toast'
import { Button } from '@/components/ui/button'
import { profileSchema, type ProfileInput } from '@/lib/schemas'
import { type MemberProfile, updateMemberProfile } from '@/lib/member-api'

function splitName(displayName = '') {
  const parts = displayName.trim().split(/\s+/).filter(Boolean)
  return { firstName: parts.shift() || '', lastName: parts.join(' ') || '' }
}

export function ProfileForm({ profile, token, onSaved }: { profile: MemberProfile; token: string; onSaved?: (profile: MemberProfile) => void }) {
  const { register, reset, handleSubmit, formState: { errors, isDirty, isSubmitting } } = useForm<ProfileInput>({
    resolver: zodResolver(profileSchema),
    defaultValues: { ...splitName(profile.displayName), email: profile.email, company: '', bio: '' },
  })

  useEffect(() => reset({ ...splitName(profile.displayName), email: profile.email, company: '', bio: '' }), [profile, reset])

  const submit = handleSubmit(async values => {
    try {
      const saved = await updateMemberProfile(token, values)
      onSaved?.(saved)
      reset({ ...values, firstName: values.firstName.trim(), lastName: values.lastName.trim(), email: values.email.trim() })
      toast.success('Profile changes saved')
    } catch (error) {
      toast.error(error instanceof Error ? error.message : 'We could not save your profile')
    }
  })

  const fields: Array<{ name: keyof ProfileInput; label: string; type?: string; autoComplete?: string; description?: string }> = [
    { name: 'firstName', label: 'First name', autoComplete: 'given-name' },
    { name: 'lastName', label: 'Last name', autoComplete: 'family-name' },
    { name: 'email', label: 'Email address', type: 'email', autoComplete: 'email', description: 'Used for account notices and membership communications.' },
    { name: 'company', label: 'Company', autoComplete: 'organization', description: 'Optional profile information; backend profile support is coming soon.' },
  ]

  return <form onSubmit={submit} noValidate>
    <div className="grid gap-6 sm:grid-cols-2">
      {fields.map(field => <div key={field.name} className={field.name === 'email' ? 'sm:col-span-2' : ''}>
        <label className="label" htmlFor={`profile-${field.name}`}>{field.label}</label>
        <input id={`profile-${field.name}`} className="field" type={field.type || 'text'} autoComplete={field.autoComplete} aria-invalid={Boolean(errors[field.name])} aria-describedby={errors[field.name] || field.description ? `profile-${field.name}-help` : undefined} {...register(field.name)}/>
        {(errors[field.name] || field.description) && <p id={`profile-${field.name}-help`} className={errors[field.name] ? 'error' : 'mt-2 text-xs leading-5 text-[#657069]'}>{errors[field.name]?.message || field.description}</p>}
      </div>)}
      <div className="sm:col-span-2">
        <label className="label" htmlFor="profile-bio">Bio</label>
        <textarea id="profile-bio" rows={5} className="field resize-y" maxLength={500} aria-describedby="profile-bio-help" {...register('bio')}/>
        <p id="profile-bio-help" className="mt-2 text-xs leading-5 text-[#657069]">Optional profile information; backend profile support is coming soon.</p>
        {errors.bio && <p className="error">{errors.bio.message}</p>}
      </div>
    </div>
    <div className="mt-8 flex flex-wrap items-center gap-4 border-t border-black/10 pt-6">
      <Button type="submit" disabled={isSubmitting || !isDirty}>{isSubmitting ? 'Saving changes…' : 'Save changes'}</Button>
      {isDirty && <button type="button" className="min-h-10 px-3 text-sm font-semibold text-[#657069] hover:text-[#173f35]" onClick={() => reset()}>Discard</button>}
      <p className="w-full text-xs text-[#657069] sm:ml-auto sm:w-auto">Last updated {profile.updatedAt ? new Date(profile.updatedAt).toLocaleDateString() : 'recently'}</p>
    </div>
  </form>
}
