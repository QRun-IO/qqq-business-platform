package io.qrun.platform.identity.auth;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/** Bridges Keycloak realm-role names to QQQ permission names/bundles. */
public class RolePermissionBundleResolver implements PermissionBundleResolver
{
   private final Map<String, ? extends Collection<String>> bundles;

   public RolePermissionBundleResolver(Map<String, ? extends Collection<String>> bundles)
   {
      this.bundles = Map.copyOf(bundles);
   }

   @Override
   public Collection<String> resolve(Collection<String> realmRoles)
   {
      Set<String> permissions = new LinkedHashSet<>();
      for(String role : realmRoles)
      {
         Collection<String> bundle = bundles.get(role);
         if(bundle != null)
         {
            permissions.addAll(bundle);
         }
      }
      return Set.copyOf(permissions);
   }
}
