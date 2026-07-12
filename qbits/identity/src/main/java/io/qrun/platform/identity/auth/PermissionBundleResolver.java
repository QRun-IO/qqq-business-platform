package io.qrun.platform.identity.auth;

import java.util.Collection;
import java.util.Set;

@FunctionalInterface
public interface PermissionBundleResolver
{
   PermissionBundleResolver NONE = roles -> Set.of();

   Collection<String> resolve(Collection<String> realmRoles);
}
