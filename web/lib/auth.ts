import type { NextAuthOptions } from 'next-auth'
import KeycloakProvider from 'next-auth/providers/keycloak'

export const authOptions: NextAuthOptions = {
  providers: [KeycloakProvider({
    clientId: process.env.KEYCLOAK_CLIENT_ID || 'platform-web',
    clientSecret: process.env.KEYCLOAK_CLIENT_SECRET || 'platform-web-secret',
    issuer: process.env.KEYCLOAK_ISSUER || 'http://localhost:18081/realms/qqq-platform',
  })],
  session: { strategy: 'jwt' },
  callbacks: {
    async jwt({ token, account, profile }) {
      if (account) {
        token.accessToken = account.access_token
        token.idToken = account.id_token
        token.roles = (profile as { realm_access?: { roles?: string[] } })?.realm_access?.roles || []
      }
      return token
    },
    async session({ session, token }) {
      session.accessToken = token.accessToken as string
      session.roles = (token.roles as string[]) || []
      return session
    },
  },
  pages: { signIn: '/auth/login', error: '/auth/login' },
}
