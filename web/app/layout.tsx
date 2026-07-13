import type { Metadata } from 'next'
import { Inter } from 'next/font/google'
import './globals.css'
import { Providers } from '@/components/providers'
import { Header } from '@/components/header'
import { Footer } from '@/components/footer'
import { Toaster } from 'react-hot-toast'

const inter = Inter({ subsets: ['latin'], variable: '--font-inter', display: 'swap' })
export const metadata: Metadata = { title: { default: 'QQQ — Business, beautifully connected', template: '%s | QQQ' }, description: 'A composed platform for content, relationships, newsletters and member experiences.', metadataBase: new URL(process.env.NEXT_PUBLIC_APP_URL || 'http://localhost:3004'), openGraph: { title: 'QQQ — Business, beautifully connected', description: 'A considered platform for modern teams.', type: 'website' } }
export default function RootLayout({children}:{children:React.ReactNode}) { return <html lang="en"><body className={inter.variable}><Providers><Header/><main>{children}</main><Footer/><Toaster position="top-right" toastOptions={{className:'toast'}}/></Providers></body></html> }
