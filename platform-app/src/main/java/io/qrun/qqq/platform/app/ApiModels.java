package io.qrun.qqq.platform.app;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

final class ApiModels {
  private ApiModels() { }

  record SubscribeRequest(@NotBlank @Email @Size(max = 320) String email) { }
  record RegisterRequest(@NotBlank @Email @Size(max = 320) String email,
                         @NotBlank @Size(min = 12, max = 128) String password,
                         @NotBlank @Size(max = 255) String displayName) { }
  record ProfileUpdateRequest(@NotBlank @Size(max = 255) String displayName,
                              @Email @Size(max = 320) String email) { }
  record PostRequest(@NotBlank @Size(max = 300) String title,
                     @Size(max = 255) String slug,
                     @Size(max = 4000) String excerpt,
                     JsonNode content, List<UUID> categoryIds, List<UUID> tagIds, UUID featuredImageAssetId) { }
  record TaxonomyRequest(@NotBlank @Size(max = 150) String name, @Size(max = 150) String slug,
                         @Size(max = 2000) String description) { }
  record UserUpdateRequest(@Email @Size(max = 320) String email,
                           @NotBlank @Size(max = 255) String displayName) { }
}
