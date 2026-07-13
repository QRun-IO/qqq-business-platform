// web/app/admin/layout.tsx
import Link from 'next/link';
import { Mail, Users, Building2, TrendingUp, FileText } from 'lucide-react';
import { PageShell, Card } from '@/components/page-shell';
import { SessionProvider } from 'next-auth/react';
import { Header } from '@/components/header'; // Assuming Header is in components

export default function AdminLayout({ children }: { children: React.ReactNode }) {
  return (
    <SessionProvider>
      <Header />
      <div className="container py-8">
        <nav className="mb-8 space-x-4">
          <Link href="/admin/crm/contacts" className="text-blue-600 hover:underline flex items-center gap-1">
            <Users size={16} /> Contacts
          </Link>
          <Link href="/admin/crm/companies" className="text-blue-600 hover:underline flex items-center gap-1">
            <Building2 size={16} /> Companies
          </Link>
          <Link href="/admin/crm/deals" className="text-blue-600 hover:underline flex items-center gap-1">
            <TrendingUp size={16} /> Deals
          </Link>
          <Link href="/admin/crm/page" className="text-blue-600 hover:underline flex items-center gap-1">
            <FileText size={16} /> CRM Overview
          </Link>
          <Link href="/admin/newsletter" className="text-blue-600 hover:underline flex items-center gap-1">
            <Mail size={16} /> Newsletter
          </Link>
        </nav>
        <PageShell title="Admin Dashboard" description="Manage your CRM and marketing operations.">
          {children}
        </PageShell>
      </div>
    </SessionProvider>
  );
}
