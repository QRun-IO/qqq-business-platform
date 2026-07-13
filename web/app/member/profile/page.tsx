'use client'

import { useEffect, useState } from 'react'
import { useSession } from 'next-auth/react'
import { ShieldCheck, UserRound } from 'lucide-react'
import { PageShell, Card } from '@/components/page-shell'
import { ProfileForm } from '@/components/profile-form'
import { getMemberProfile, memberInitials, type MemberProfile } from '@/lib/member-api'

export default function ProfilePage() {
  const { data: session } = useSession()
  const [profile, setProfile] = useState<MemberProfile>()
  const [error, setError] = useState('')

  useEffect(() => {
    if (!session?.accessToken) return
    getMemberProfile(session.accessToken).then(setProfile).catch(error => setError(error instanceof Error ? error.message : 'Profile unavailable'))
  }, [session?.accessToken])

  return <PageShell title="Your profile" description="Keep your member identity and contact details accurate and up to date.">
    {error && <div role="alert" className="mb-6 rounded-xl border border-red-200 bg-red-50 p-4 text-sm text-red-800">{error}</div>}
    {!profile || !session?.accessToken ? <ProfileSkeleton/> : <div className="grid gap-7 lg:grid-cols-[minmax(0,1fr)_300px]">
      <Card><div className="mb-8 flex items-center gap-4 border-b border-black/10 pb-7"><div className="grid h-16 w-16 place-items-center rounded-2xl bg-[#173f35] text-lg font-semibold text-white">{memberInitials(profile)}</div><div><p className="text-lg font-semibold">{profile.displayName}</p><p className="mt-1 text-sm text-[#657069]">{profile.email}</p></div></div><ProfileForm profile={profile} token={session.accessToken} onSaved={setProfile}/></Card>
      <div className="space-y-5"><Card><UserRound size={20} className="text-[#55776b]"/><h2 className="mt-5 font-semibold">Profile visibility</h2><p className="mt-2 text-sm leading-6 text-[#657069]">Your profile details stay private to your authenticated account.</p></Card><Card><ShieldCheck size={20} className="text-[#55776b]"/><h2 className="mt-5 font-semibold">Identity protected</h2><p className="mt-2 text-sm leading-6 text-[#657069]">Sign-in and account security are managed through QQQ Identity.</p></Card></div>
    </div>}
  </PageShell>
}

function ProfileSkeleton() { return <Card className="max-w-3xl" ><div role="status" className="animate-pulse"><div className="h-16 w-16 rounded-2xl bg-black/10"/><div className="mt-8 grid gap-5 sm:grid-cols-2"><div className="h-14 rounded bg-black/10"/><div className="h-14 rounded bg-black/10"/><div className="h-14 rounded bg-black/10 sm:col-span-2"/></div><span className="sr-only">Loading profile</span></div></Card> }
