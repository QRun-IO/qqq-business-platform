import Link from 'next/link'
import { ArrowUpRight } from 'lucide-react'

const groups = [
  { title: 'Platform', links: [['Overview', '/#platform'], ['Journal', '/blog'], ['Member space', '/member']] },
  { title: 'Company', links: [['Principles', '/#principles'], ['Pricing', '/#pricing'], ['Contact', 'mailto:hello@qrun.io']] },
  { title: 'Access', links: [['Sign in', '/auth/login'], ['Create account', '/auth/register'], ['Admin', '/admin']] },
] as const

export function Footer() {
  return <footer className="site-footer">
    <div className="shell footer-grid">
      <div className="footer-brand">
        <Link href="/" className="brand" aria-label="QQQ home"><span className="brand-mark">Q³</span><span>QQQ</span></Link>
        <p>One composed operating layer for content, relationships, audiences, and member experiences.</p>
        <a className="footer-email" href="mailto:hello@qrun.io">hello@qrun.io <ArrowUpRight size={14}/></a>
      </div>
      {groups.map(group => <div className="footer-column" key={group.title}><h2>{group.title}</h2>{group.links.map(([label, href]) => <Link href={href} key={label}>{label}</Link>)}</div>)}
    </div>
    <div className="shell footer-bottom"><span>© {new Date().getFullYear()} QRunIO. All rights reserved.</span><span>Built for focused teams.</span></div>
  </footer>
}
