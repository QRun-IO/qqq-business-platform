# Platform Identity QBit

Gen-2 QQQ QBit that maps OIDC identities to a self-owned `user_profile` record.

## Authentication hook

Create `PlatformAuthCustomizer` with:

- `JdbcUserProfileRepository` for idempotent JIT provisioning by OIDC `sub`;
- `RolePermissionBundleResolver` (or a qbit-user-role-permissions-backed implementation) to bridge Keycloak realm roles to QQQ permissions;
- `CrmContactSynchronizer` to upsert a CRM contact for users carrying the `business-user` realm role.

Call `customize(QSession, OidcClaims)` from the middleware's successful OIDC-login/session creation hook. It sets the QQQ `idReference`, `userProfileId` security key, permissions, and frontend session value.

## Database

Apply `src/main/resources/db/identity-user-profile.sql`. The metadata table name is `userProfile`; its RDBMS table is `user_profile` as defined by the DDL/application naming convention.

## Security

The producer registers the `userProfileId` security-key type. `UserProfileRecord` has a read/write record lock matching its `id` against that session key, with null values denied.

## Test

```shell
mvn -f qbits/identity/pom.xml test
```
