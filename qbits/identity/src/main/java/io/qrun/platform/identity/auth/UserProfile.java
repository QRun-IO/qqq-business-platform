package io.qrun.platform.identity.auth;

import java.time.Instant;

public record UserProfile(Long id, String oidcSub, String email, String displayName, Instant createdAt, Instant updatedAt)
{
}
