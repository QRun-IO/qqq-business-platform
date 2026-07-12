package io.qrun.platform.identity.auth;

public interface UserProfileRepository
{
   UserProfile provisionOrUpdate(OidcClaims claims);
}
