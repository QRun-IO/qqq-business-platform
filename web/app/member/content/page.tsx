'use client'

import { useEffect, useMemo, useState } from 'react'
import { useSession } from 'next-auth/react'
import Link from 'next/link'
import { ArrowRight, BookOpen, LockKeyhole, Search } from 'lucide-react'
import { PageShell } from '@/components/page-shell'
import { formatMemberDate, getMemberContent, type MemberContentItem } from '@/lib/member-api'

export default function MemberContentPage() {
  const { data: session } = useSession()
  const [items, setItems] = useState<MemberContentItem[]>([])
  const [query, setQuery] = useState('')
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    if (!session?.accessToken) return
    getMemberContent(session.accessToken).then(page => setItems(page.items || [])).catch(error => setError(error instanceof Error ? error.message : 'Library unavailable')).finally(() => setLoading(false))
  }, [session?.accessToken])

  const filtered = useMemo(() => items.filter(item => `${item.title} ${item.excerpt || ''}`.toLowerCase().includes(query.trim().toLowerCase())), [items, query])

  return <PageShell title="Member library" description="Private resources, practical guidance and new thinking reserved for authenticated members." actions={<span className="inline-flex items-center gap-2 rounded-full bg-[#dfe7dc] px-4 py-2 text-xs font-semibold text-[#173f35]"><LockKeyhole size={14}/> Member only</span>}>
    <div className="mb-8 flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between"><div className="relative w-full max-w-md"><Search aria-hidden className="absolute left-4 top-1/2 -translate-y-1/2 text-[#657069]" size={18}/><label htmlFor="library-search" className="sr-only">Search member library</label><input id="library-search" type="search" className="field pl-11" placeholder="Search the library" value={query} onChange={event => setQuery(event.target.value)}/></div><p className="text-sm text-[#657069]">{loading ? 'Loading collection…' : `${filtered.length} resource${filtered.length === 1 ? '' : 's'}`}</p></div>
    {error && <div role="alert" className="mb-6 rounded-xl border border-amber-200 bg-amber-50 p-4 text-sm text-amber-900">The member content service is not available yet. Please try again later.</div>}
    {loading ? <div role="status" className="grid animate-pulse gap-5 md:grid-cols-2 lg:grid-cols-3">{[1,2,3].map(value => <div key={value} className="h-72 rounded-[1.5rem] bg-black/10"/>)}<span className="sr-only">Loading member content</span></div> : filtered.length ? <div className="grid gap-5 md:grid-cols-2 lg:grid-cols-3">{filtered.map(item => <article key={item.id} className="premium-card group flex min-h-72 flex-col rounded-[1.5rem] p-7 transition hover:-translate-y-1 hover:bg-white"><div className="flex items-center justify-between"><span className="grid h-11 w-11 place-items-center rounded-xl bg-[#dfe7dc] text-[#173f35]"><BookOpen size={19}/></span><span className="eyebrow text-[#55776b]">Exclusive</span></div><h2 className="mt-8 text-xl font-semibold leading-7">{item.title}</h2><p className="mt-3 line-clamp-3 text-sm leading-6 text-[#657069]">{item.excerpt || 'A private resource for QQQ members.'}</p><div className="mt-auto flex items-end justify-between gap-3 pt-8"><time className="text-xs text-[#657069]">{formatMemberDate(item.publishedAt || item.updatedAt)}</time>{item.slug ? <Link className="inline-flex min-h-10 items-center gap-2 text-sm font-semibold text-[#173f35]" href={`/blog/${item.slug}`}>Read <ArrowRight size={15}/></Link> : <span className="text-xs font-semibold text-[#55776b]">Available in workspace</span>}</div></article>)}</div> : <div className="premium-card rounded-[1.5rem] py-16 text-center"><BookOpen className="mx-auto text-[#55776b]"/><h2 className="mt-5 text-xl font-semibold">{query ? 'No matching resources' : 'Your private library is being prepared'}</h2><p className="mx-auto mt-3 max-w-md text-sm leading-6 text-[#657069]">{query ? 'Try a broader search term.' : 'New member-only guides and briefings will appear here as they are published.'}</p></div>}
  </PageShell>
}
