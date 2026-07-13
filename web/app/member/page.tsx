'use client'

import { useEffect, useState } from 'react'
import Link from 'next/link'
import { useSession } from 'next-auth/react'
import { ArrowRight, BookOpen, Clock3, Settings, Sparkles, UserRound } from 'lucide-react'
import { Card, PageShell } from '@/components/page-shell'
import { formatMemberDate, getMemberActivity, getMemberContent, getMemberProfile, type MemberActivityItem, type MemberContentItem, type MemberProfile } from '@/lib/member-api'

export default function MemberDashboard() {
  const { data: session } = useSession()
  const [profile, setProfile] = useState<MemberProfile>()
  const [content, setContent] = useState<MemberContentItem[]>([])
  const [activity, setActivity] = useState<MemberActivityItem[]>([])
  const [loading, setLoading] = useState(true)
  const [notice, setNotice] = useState('')

  useEffect(() => {
    if (!session?.accessToken) return
    const token = session.accessToken
    Promise.allSettled([getMemberProfile(token), getMemberContent(token, 0, 3), getMemberActivity(token, 0, 5)]).then(results => {
      if (results[0].status === 'fulfilled') setProfile(results[0].value)
      if (results[1].status === 'fulfilled') setContent(results[1].value.items || [])
      if (results[2].status === 'fulfilled') setActivity(results[2].value.items || [])
      if (results.some(result => result.status === 'rejected')) setNotice('Some workspace data is not available yet. Your account remains secure and usable.')
      setLoading(false)
    })
  }, [session?.accessToken])

  const firstName = profile?.displayName?.split(/\s+/)[0] || session?.user?.name?.split(/\s+/)[0] || 'member'
  const joined = formatMemberDate(profile?.createdAt)

  return <PageShell title={`Welcome back, ${firstName}`} description="Your membership, saved resources and recent account activity in one quiet place.">
    {notice && <div role="status" className="mb-6 rounded-xl border border-amber-200 bg-amber-50 p-4 text-sm text-amber-900">{notice}</div>}
    <section aria-label="Membership overview" className="grid gap-5 md:grid-cols-3">
      <SummaryCard icon={<Sparkles size={20}/>} label="Membership" value="Active" detail={`Member since ${joined}`}/>
      <SummaryCard icon={<BookOpen size={20}/>} label="Member library" value={loading ? '—' : String(content.length)} detail="Resources ready to explore"/>
      <SummaryCard icon={<Clock3 size={20}/>} label="Recent activity" value={loading ? '—' : String(activity.length)} detail="Account events in your history"/>
    </section>

    <section className="mt-7 overflow-hidden rounded-[2rem] bg-[#173f35] p-8 text-white sm:p-10">
      <div className="grid gap-10 md:grid-cols-[1fr_auto] md:items-end"><div><p className="eyebrow text-[#d9f99d]">Member collection</p><h2 className="display mt-5 max-w-2xl text-4xl sm:text-5xl">Ideas chosen for considered growth.</h2><p className="mt-5 max-w-xl leading-7 text-white/65">Open the private library for member-only briefings, playbooks and platform guidance.</p></div><Link href="/member/content" className="inline-flex min-h-11 items-center gap-2 text-sm font-semibold text-[#d9f99d]">Explore the library <ArrowRight size={16}/></Link></div>
    </section>

    <div className="mt-7 grid gap-7 lg:grid-cols-[minmax(0,1.3fr)_minmax(300px,.7fr)]">
      <Card><div className="flex items-center justify-between"><div><p className="eyebrow text-[#55776b]">Recent activity</p><h2 className="mt-3 text-xl font-semibold">Your account history</h2></div><Clock3 size={20} className="text-[#55776b]"/></div><div className="mt-7 divide-y divide-black/10">{loading ? <DashboardSkeleton/> : activity.length ? activity.slice(0, 5).map(item => <div key={item.id} className="flex gap-4 py-4 first:pt-0"><span className="mt-1 h-2.5 w-2.5 shrink-0 rounded-full bg-[#6f9b8c]"/><div className="min-w-0"><p className="font-semibold">{item.title}</p><p className="mt-1 text-sm leading-6 text-[#657069]">{item.description || item.type} · {formatMemberDate(item.occurredAt)}</p></div></div>) : <EmptyState text="Activity will appear here as you use your member account."/>}</div></Card>
      <div className="space-y-5"><QuickLink href="/member/profile" icon={<UserRound/>} title="Complete your profile" detail={profile?.email || 'Review your member identity'}/><QuickLink href="/member/settings" icon={<Settings/>} title="Choose preferences" detail="Control communications and appearance"/></div>
    </div>
  </PageShell>
}

function SummaryCard({ icon, label, value, detail }: { icon: React.ReactNode; label: string; value: string; detail: string }) { return <Card><div className="flex items-center justify-between text-[#55776b]">{icon}<span className="eyebrow">{label}</span></div><p className="display mt-7 text-4xl">{value}</p><p className="mt-3 text-sm text-[#657069]">{detail}</p></Card> }
function QuickLink({ href, icon, title, detail }: { href: string; icon: React.ReactNode; title: string; detail: string }) { return <Link href={href} className="premium-card group flex min-h-32 items-center gap-5 rounded-[1.4rem] p-6 transition hover:-translate-y-0.5 hover:bg-white"><span className="grid h-12 w-12 shrink-0 place-items-center rounded-xl bg-[#dfe7dc] text-[#173f35]">{icon}</span><span className="min-w-0"><strong className="block">{title}</strong><span className="mt-2 block truncate text-sm text-[#657069]">{detail}</span></span><ArrowRight size={17} className="ml-auto shrink-0 transition group-hover:translate-x-1"/></Link> }
function EmptyState({ text }: { text: string }) { return <div className="py-10 text-center text-sm leading-6 text-[#657069]">{text}</div> }
function DashboardSkeleton() { return <div role="status" className="animate-pulse space-y-3"><div className="h-14 rounded bg-black/10"/><div className="h-14 rounded bg-black/10"/><span className="sr-only">Loading dashboard</span></div> }
