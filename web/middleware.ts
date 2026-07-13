export { default } from 'next-auth/middleware'
export const config = { matcher: ['/member/:path*', '/admin/:path*'] }
