'use client'

import type { ReactNode } from 'react'
import Link from 'next/link'
import { usePathname, useRouter } from 'next/navigation'
import { useSession } from 'next-auth/react'
import { BookOpen, LayoutDashboard, Settings, UserRound } from 'lucide-react'

const navigation = [
  { href: '/member', label: 'Overview', icon: LayoutDashboard },
  { href: '/member/content', label: 'Member library', icon: BookOpen },
  { href: '/member/profile', label: 'Profile', icon: UserRound },
  { href: '/member/settings', label: 'Settings', icon: Settings },
]

export function MemberLayout({ children }: { children: ReactNode }) {
  const { data: session, status } = useSession()
  const pathname = usePathname()
  const router = useRouter()
  const staffOnly = session?.roles?.some(role => ['STAFF', 'ADMIN'].includes(role))

  if (status === 'loading') return <MemberLoading />
  if (status === 'unauthenticated') {
    router.replace(`/auth/login?callbackUrl=${encodeURIComponent(pathname)}`)
    return <MemberLoading />
  }
  if (staffOnly) return <AccessDenied />

  return <div className="min-h-screen bg-[#f6f5f0] text-[#17201b]">
    <div className="border-b border-black/10 bg-[#edf0e9]">
      <div className="container py-8 sm:py-10">
        <div className="flex flex-col gap-6 lg:flex-row lg:items-center lg:justify-between">
          <div><p className="eyebrow text-[#55776b]">Member workspace</p><p className="mt-2 text-sm text-[#657069]">Your private QQQ membership home</p></div>
          <nav aria-label="Member workspace" className="flex max-w-full gap-1 overflow-x-auto rounded-2xl border border-black/10 bg-white/70 p-1.5">
            {navigation.map(({ href, label, icon: Icon }) => {
              const active = href === '/member' ? pathname === href : pathname.startsWith(href)
              return <Link key={href} href={href} aria-current={active ? 'page' : undefined} className={`flex min-h-11 shrink-0 items-center gap-2 rounded-xl px-3.5 text-sm font-semibold transition ${active ? 'bg-[#173f35] text-white shadow-sm' : 'text-[#657069] hover:bg-white hover:text-[#173f35]'}`}><Icon aria-hidden size={16}/>{label}</Link>
            })}
          </nav>
        </div>
      </div>
    </div>
    {children}
  </div>
}

function MemberLoading() {
  return <div className="min-h-[60vh] bg-[#f6f5f0]"><div className="container py-20" role="status"><div className="h-4 w-32 animate-pulse rounded bg-black/10"/><div className="mt-5 h-12 max-w-xl animate-pulse rounded bg-black/10"/><span className="sr-only">Loading member workspace</span></div></div>
}

function AccessDenied() {
  return <main className="min-h-[60vh] bg-[#f6f5f0]"><div className="container py-20"><div className="premium-card max-w-xl rounded-[1.5rem] p-8"><p className="eyebrow text-[#55776b]">Member access</p><h1 className="display mt-4 text-4xl">This space is for members.</h1><p className="mt-4 leading-7 text-[#657069]">Your staff-only account does not include a member workspace. Use the admin area for operational tools.</p><Link className="mt-7 inline-flex min-h-11 items-center rounded-xl bg-[#173f35] px-5 text-sm font-semibold text-white" href="/admin">Open admin workspace</Link></div></div></main>
}
