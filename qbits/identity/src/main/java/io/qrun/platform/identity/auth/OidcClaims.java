package io.qrun.platform.identity.auth;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public record OidcClaims(String subject, String email, String displayName, Set<String> realmRoles)
{
   public static OidcClaims from(Map<String, ?> claims)
   {
      String subject = stringClaim(claims, "sub");
      if(subject == null || subject.isBlank())
      {
         throw new IllegalArgumentException("OIDC sub claim is required");
      }
      String email = stringClaim(claims, "email");
      String displayName = stringClaim(claims, "preferred_username");
      if(displayName == null || displayName.isBlank())
      {
         displayName = email == null || email.isBlank() ? subject : email;
      }

      Set<String> roles = new LinkedHashSet<>();
      Object realmAccess = claims.get("realm_access");
      if(realmAccess instanceof Map<?, ?> realmMap && realmMap.get("roles") instanceof Collection<?> values)
      {
         values.stream().filter(String.class::isInstance).map(String.class::cast).forEach(roles::add);
      }
      return new OidcClaims(subject, email, displayName, Set.copyOf(roles));
   }

   private static String stringClaim(Map<String, ?> claims, String key)
   {
      Object value = claims.get(key);
      return value instanceof String string ? string : null;
   }

   public boolean hasRealmRole(String role)
   {
      return realmRoles.contains(role);
   }
}
