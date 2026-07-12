package io.qrun.platform.identity.auth;

@FunctionalInterface
public interface CrmContactSynchronizer
{
   CrmContactSynchronizer NO_OP = (profile, claims) -> { };

   void synchronize(UserProfile profile, OidcClaims claims);
}
