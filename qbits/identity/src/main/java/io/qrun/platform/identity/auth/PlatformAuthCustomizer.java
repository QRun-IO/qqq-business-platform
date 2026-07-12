package io.qrun.platform.identity.auth;

import java.util.Objects;
import com.kingsrook.qqq.backend.core.model.session.QSession;

public class PlatformAuthCustomizer
{
   public static final String USER_PROFILE_ID_SECURITY_KEY = "userProfileId";
   public static final String USER_PROFILE_ID_SESSION_VALUE = "userProfileId";
   public static final String BUSINESS_USER_REALM_ROLE = "business-user";

   private final UserProfileRepository repository;
   private final PermissionBundleResolver permissionResolver;
   private final CrmContactSynchronizer crmSynchronizer;

   public PlatformAuthCustomizer(UserProfileRepository repository, PermissionBundleResolver permissionResolver, CrmContactSynchronizer crmSynchronizer)
   {
      this.repository = Objects.requireNonNull(repository);
      this.permissionResolver = Objects.requireNonNull(permissionResolver);
      this.crmSynchronizer = Objects.requireNonNull(crmSynchronizer);
   }

   public QSession customize(QSession session, OidcClaims claims)
   {
      Objects.requireNonNull(session, "session");
      Objects.requireNonNull(claims, "claims");
      UserProfile profile = repository.provisionOrUpdate(claims);
      session.setIdReference(claims.subject());
      session.withSecurityKeyValue(USER_PROFILE_ID_SECURITY_KEY, profile.id());
      session.withPermissions(permissionResolver.resolve(claims.realmRoles()));
      session.setValue(USER_PROFILE_ID_SESSION_VALUE, profile.id().toString());
      session.withValueForFrontend(USER_PROFILE_ID_SESSION_VALUE, profile.id());
      if(claims.hasRealmRole(BUSINESS_USER_REALM_ROLE))
      {
         crmSynchronizer.synchronize(profile, claims);
      }
      return session;
   }
}
