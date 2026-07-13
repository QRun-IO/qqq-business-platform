'use client'

import { useEffect, useState } from 'react'
import { signOut, useSession } from 'next-auth/react'
import toast from 'react-hot-toast'
import { Bell, LogOut, Monitor, ShieldCheck } from 'lucide-react'
import { PageShell, Card } from '@/components/page-shell'
import { Button } from '@/components/ui/button'
import { defaultMemberPreferences, getMemberPreferences, updateMemberPreferences, type MemberPreferences } from '@/lib/member-api'

export default function MemberSettingsPage() {
  const { data: session } = useSession()
  const memberId = session?.user?.email || session?.user?.name || 'member'
  const [preferences, setPreferences] = useState<MemberPreferences>(defaultMemberPreferences)
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)

  useEffect(() => {
    if (!session?.accessToken) return
    getMemberPreferences(session.accessToken, memberId).then(setPreferences).finally(() => setLoading(false))
  }, [session?.accessToken, memberId])

  const save = async () => {
    if (!session?.accessToken) return
    setSaving(true)
    try { await updateMemberPreferences(session.accessToken, memberId, preferences); toast.success('Preferences saved') }
    catch (error) { toast.error(error instanceof Error ? error.message : 'Preferences could not be saved') }
    finally { setSaving(false) }
  }

  return <PageShell title="Account settings" description="Choose how QQQ communicates with you and manage your active member session.">
    <div className="grid gap-7 lg:grid-cols-[minmax(0,1fr)_320px]">
      <div className="space-y-7"><Card><SectionHeading icon={<Bell/>} title="Communication preferences" detail="Select the membership updates you want to receive."/><div className="mt-7 divide-y divide-black/10">{[
        ['weeklyDigest','Weekly member digest','A concise roundup of new private resources.'],
        ['productNews','Product and platform news','Meaningful changes to QQQ products and services.'],
        ['activityEmails','Account activity notices','Optional email summaries of your member activity.'],
        ['emailUpdates','Membership announcements','Important news about your membership.'],
      ].map(([key,title,detail]) => <PreferenceToggle key={key} id={key} title={title} detail={detail} checked={preferences[key as keyof MemberPreferences] as boolean} disabled={loading} onChange={checked => setPreferences(current => ({ ...current, [key]: checked }))}/>)}</div></Card>
      <Card><SectionHeading icon={<Monitor/>} title="Appearance" detail="Choose how the member workspace should appear on this device."/><fieldset className="mt-7 grid gap-3 sm:grid-cols-3"><legend className="sr-only">Workspace theme</legend>{(['system','light','dark'] as const).map(theme => <label key={theme} className={`flex min-h-12 cursor-pointer items-center justify-center rounded-xl border px-4 text-sm font-semibold capitalize transition ${preferences.theme === theme ? 'border-[#173f35] bg-[#edf0e9] text-[#173f35]' : 'border-black/10 bg-white text-[#657069] hover:border-black/20'}`}><input className="sr-only" type="radio" name="theme" value={theme} checked={preferences.theme === theme} onChange={() => setPreferences(current => ({ ...current, theme }))}/>{theme}</label>)}</fieldset></Card>
      <Button onClick={save} disabled={loading || saving}>{saving ? 'Saving preferences…' : 'Save preferences'}</Button></div>
      <div className="space-y-5"><Card><SectionHeading icon={<ShieldCheck/>} title="Account security" detail="Authentication is protected by QQQ Identity."/><dl className="mt-7 space-y-4 text-sm"><div><dt className="text-[#657069]">Signed in as</dt><dd className="mt-1 break-all font-semibold">{session?.user?.email || 'Authenticated member'}</dd></div><div><dt className="text-[#657069]">Session</dt><dd className="mt-1 font-semibold text-emerald-700">Active</dd></div></dl></Card><Card><LogOut size={20} className="text-[#55776b]"/><h2 className="mt-5 font-semibold">End this session</h2><p className="mt-2 text-sm leading-6 text-[#657069]">Sign out safely when using a shared device.</p><Button variant="outline" className="mt-6 w-full" onClick={() => signOut({ callbackUrl: '/' })}><LogOut className="mr-2" size={16}/>Sign out</Button></Card></div>
    </div>
  </PageShell>
}

function SectionHeading({ icon, title, detail }: { icon: React.ReactNode; title: string; detail: string }) { return <div className="flex gap-4"><span className="grid h-11 w-11 shrink-0 place-items-center rounded-xl bg-[#dfe7dc] text-[#173f35]">{icon}</span><div><h2 className="font-semibold">{title}</h2><p className="mt-1 text-sm leading-6 text-[#657069]">{detail}</p></div></div> }
function PreferenceToggle({ id, title, detail, checked, disabled, onChange }: { id: string; title: string; detail: string; checked: boolean; disabled: boolean; onChange: (checked: boolean) => void }) { return <div className="flex items-center justify-between gap-5 py-5"><label htmlFor={id} className="cursor-pointer"><span className="block font-semibold">{title}</span><span className="mt-1 block text-sm leading-6 text-[#657069]">{detail}</span></label><button id={id} type="button" role="switch" aria-checked={checked} disabled={disabled} onClick={() => onChange(!checked)} className={`relative h-7 w-12 shrink-0 rounded-full transition focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-[#7468f2] disabled:opacity-50 ${checked ? 'bg-[#173f35]' : 'bg-black/20'}`}><span className={`absolute top-1 h-5 w-5 rounded-full bg-white shadow-sm transition ${checked ? 'left-6' : 'left-1'}`}/><span className="sr-only">{title}</span></button></div> }
