'use client'
import Link from 'next/link'
import { signIn, signOut, useSession } from 'next-auth/react'
import { ArrowUpRight, Menu, X } from 'lucide-react'
import { useState } from 'react'

export function Header() {
  const { data: session, status } = useSession()
  const [open, setOpen] = useState(false)
  const staff = session?.roles?.some(role => ['STAFF', 'ADMIN'].includes(role))
  const close = () => setOpen(false)
  return <header className="site-header"><div className="shell nav-shell">
    <Link href="/" className="brand" onClick={close} aria-label="QQQ home"><span className="brand-mark">Q³</span><span>QQQ</span><span className="brand-edition">Business</span></Link>
    <button className="nav-toggle" onClick={() => setOpen(!open)} aria-label="Toggle navigation" aria-expanded={open}>{open ? <X size={19}/> : <Menu size={19}/>}</button>
    <nav className={`nav-links ${open ? 'is-open' : ''}`} aria-label="Primary navigation">
      <Link onClick={close} href="/#platform">Platform</Link><Link onClick={close} href="/#pricing">Pricing</Link><Link onClick={close} href="/blog">Journal</Link>
      {session && <Link onClick={close} href="/member">Workspace</Link>}{staff && <Link onClick={close} href="/admin">Admin</Link>}
      <span className="nav-divider"/>
      {status === 'authenticated' ? <button className="button button-ghost nav-action" onClick={() => signOut({callbackUrl:'/'})}>Sign out</button> : <><Link className="nav-signin" onClick={close} href="/auth/login">Sign in</Link><button className="button button-primary nav-action" onClick={() => signIn('keycloak',{callbackUrl:'/member'})}>Open workspace <ArrowUpRight size={15}/></button></>}
    </nav>
  </div></header>
}
