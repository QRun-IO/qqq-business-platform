package io.qrun.qqq.platform.app;


import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@Validated
class PlatformApiController {
  private final JdbcTemplate db;
  private final ContentService content;
  private final UserService users;
  private final UploadService uploads;
  private final PasswordEncoder passwords;

  PlatformApiController(JdbcTemplate db, ContentService content,
      UserService users, UploadService uploads, PasswordEncoder passwords) {
    this.db = db;
    this.content = content;
    this.users = users;
    this.uploads = uploads;
    this.passwords = passwords;
  }

  @GetMapping({"/health", "/api/public/v1/health"})
  @Operation(summary = "Application health check")
  Map<String, String> health() { return Map.of("status", "UP"); }


  @GetMapping("/api/public/v1/content/posts")
  ContentService.Page<ContentService.PostView> publicPosts(@RequestParam(defaultValue = "0") @Min(0) int page,
      @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) { return content.publicPosts(page, size); }

  @GetMapping("/api/public/v1/content/posts/{id}")
  ContentService.PostView publicPost(@PathVariable String id) {
    try { return content.publicPost(UUID.fromString(id)); } catch (IllegalArgumentException ignored) { return content.publicPost(id); }
  }

  @GetMapping("/api/public/v1/content/assets/{id}")
  ResponseEntity<?> publicAsset(@PathVariable UUID id) { return uploads.publicDownload(id); }

  @PostMapping("/api/public/v1/auth/register")
  @Transactional
  ResponseEntity<?> register(@Valid @RequestBody ApiModels.RegisterRequest body) {
    String normalized = body.email().trim().toLowerCase(java.util.Locale.ROOT);
    Integer existing = db.queryForObject("select count(*) from local_registration where email=?", Integer.class, normalized);
    if (existing != null && existing > 0) throw ApiException.conflict("Email is already registered");
    UUID id = UUID.randomUUID();
    db.update("insert into local_registration(id,email,password_hash,display_name,enabled,created_at,updated_at) values(?,?,?,?,true,current_timestamp,current_timestamp)",
        id, normalized, passwords.encode(body.password()), body.displayName());
    return ResponseEntity.created(URI.create("/api/public/v1/auth/registrations/" + id))
        .body(Map.of("id", id, "email", normalized, "status", "registered"));
  }

  @GetMapping("/api/member/v1/profile")
  UserService.UserView profile(HttpServletRequest request) { return users.get(userId(request)); }

  @PutMapping("/api/member/v1/profile")
  UserService.UserView updateProfile(@Valid @RequestBody ApiModels.ProfileUpdateRequest body, HttpServletRequest request) {
    return users.update(userId(request), new ApiModels.UserUpdateRequest(body.email(), body.displayName()));
  }

  @PostMapping("/api/member/v1/content/posts")
  ResponseEntity<?> createPost(@Valid @RequestBody ApiModels.PostRequest body, HttpServletRequest request) {
    var post = content.create(userId(request), body);
    return ResponseEntity.created(URI.create("/api/member/v1/content/posts/" + post.id())).body(post);
  }

  @PutMapping("/api/member/v1/content/posts/{id}")
  ContentService.PostView updatePost(@PathVariable UUID id, @Valid @RequestBody ApiModels.PostRequest body, HttpServletRequest request) {
    return content.updateOwn(id, userId(request), body);
  }

  @DeleteMapping("/api/member/v1/content/posts/{id}")
  ResponseEntity<Void> deletePost(@PathVariable UUID id, HttpServletRequest request) {
    content.deleteOwn(id, userId(request));
    return ResponseEntity.noContent().build();
  }

  @PostMapping(value = "/api/member/v1/uploads", consumes = "multipart/form-data")
  ResponseEntity<?> upload(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
    var result = uploads.upload(userId(request), file);
    return ResponseEntity.created(URI.create("/api/member/v1/uploads/" + result.id())).body(result);
  }

  @GetMapping("/api/member/v1/uploads/{id}")
  ResponseEntity<?> download(@PathVariable UUID id, HttpServletRequest request) { return uploads.download(id, userId(request)); }

  @GetMapping("/api/staff/v1/users")
  ContentService.Page<UserService.UserView> listUsers(@RequestParam(defaultValue = "0") @Min(0) int page,
      @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) { return users.list(page, size); }

  @GetMapping("/api/staff/v1/users/{id}")
  UserService.UserView getUser(@PathVariable long id) { return users.get(id); }

  @PutMapping("/api/staff/v1/users/{id}")
  UserService.UserView updateUser(@PathVariable long id, @Valid @RequestBody ApiModels.UserUpdateRequest body) { return users.update(id, body); }

  @DeleteMapping("/api/staff/v1/users/{id}")
  ResponseEntity<Void> deleteUser(@PathVariable long id, HttpServletRequest request) {
    users.delete(id, userId(request));
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/api/staff/v1/content/posts")
  ContentService.Page<ContentService.PostView> staffPosts(@RequestParam(defaultValue = "0") @Min(0) int page,
      @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) { return content.allPosts(page, size); }
  @GetMapping("/api/staff/v1/content/posts/{id}") ContentService.PostView staffPost(@PathVariable UUID id) { return content.allPost(id); }
  @PostMapping("/api/staff/v1/content/posts") ResponseEntity<?> staffCreatePost(@Valid @RequestBody ApiModels.PostRequest body, HttpServletRequest request) { var post=content.create(userId(request),body); return ResponseEntity.created(URI.create("/api/staff/v1/content/posts/"+post.id())).body(post); }
  @PutMapping("/api/staff/v1/content/posts/{id}") ContentService.PostView staffUpdatePost(@PathVariable UUID id,@Valid @RequestBody ApiModels.PostRequest body,HttpServletRequest request) { return content.updateOwn(id,userId(request),body); }
  @DeleteMapping("/api/staff/v1/content/posts/{id}") ResponseEntity<Void> staffDeletePost(@PathVariable UUID id,HttpServletRequest request) { content.deleteOwn(id,userId(request)); return ResponseEntity.noContent().build(); }
  @PutMapping("/api/staff/v1/content/posts/{id}/publish") ContentService.PostView publish(@PathVariable UUID id) { return content.publish(id); }
  @PutMapping("/api/staff/v1/content/posts/{id}/unpublish") ContentService.PostView unpublish(@PathVariable UUID id) { return content.unpublish(id); }
  @GetMapping("/api/staff/v1/content/categories") List<ContentService.TaxonomyView> categories() { return content.categories(); }
  @PostMapping("/api/staff/v1/content/categories") ContentService.TaxonomyView createCategory(@Valid @RequestBody ApiModels.TaxonomyRequest body) { return content.createTaxonomy("cms_category",body); }
  @DeleteMapping("/api/staff/v1/content/categories/{id}") ResponseEntity<Void> deleteCategory(@PathVariable UUID id) { content.deleteTaxonomy("cms_category",id); return ResponseEntity.noContent().build(); }
  @GetMapping("/api/staff/v1/content/tags") List<ContentService.TaxonomyView> tags() { return content.tags(); }
  @PostMapping("/api/staff/v1/content/tags") ContentService.TaxonomyView createTag(@Valid @RequestBody ApiModels.TaxonomyRequest body) { return content.createTaxonomy("cms_tag",body); }
  @DeleteMapping("/api/staff/v1/content/tags/{id}") ResponseEntity<Void> deleteTag(@PathVariable UUID id) { content.deleteTaxonomy("cms_tag",id); return ResponseEntity.noContent().build(); }
  @GetMapping("/api/staff/v1/content/assets") List<UploadService.UploadView> assets() { return uploads.list(); }
  @PostMapping(value="/api/staff/v1/content/assets",consumes="multipart/form-data") ResponseEntity<?> staffUpload(@RequestParam("file") MultipartFile file,HttpServletRequest request) { var result=uploads.upload(userId(request),file); return ResponseEntity.created(URI.create("/api/staff/v1/content/assets/"+result.id())).body(result); }
  @DeleteMapping("/api/staff/v1/content/assets/{id}") ResponseEntity<Void> deleteAsset(@PathVariable UUID id) { uploads.delete(id); return ResponseEntity.noContent().build(); }

  @GetMapping("/api/staff/v1/newsletter/subscriptions")
  ContentService.Page<Map<String, Object>> subscriptions(@RequestParam(defaultValue = "0") @Min(0) int page,
      @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
    long total = db.queryForObject("select count(*) from newsletter_subscription", Long.class);
    List<Map<String, Object>> items = db.queryForList("select id,email,status,preferences,confirmed_at as \"confirmedAt\",created_at as \"createdAt\" from newsletter_subscription order by created_at desc limit ? offset ?", size, page * size);
    return new ContentService.Page<>(items, page, size, total, (total + size - 1) / size);
  }

  @GetMapping("/api/staff/v1/analytics")
  Map<String, Object> analytics() {
    return Map.of(
        "users", count("user_profile"),
        "posts", count("cms_blog_post"),
        "publishedPosts", scalar("select count(*) from cms_blog_post where publication_state='published'"),
        "newsletterSubscriptions", count("newsletter_subscription"),
        "confirmedSubscriptions", scalar("select count(*) from newsletter_subscription where status='confirmed'"),
        "uploads", count("cms_asset"));
  }

  private long count(String allowedTable) {
    if (!List.of("user_profile", "cms_blog_post", "newsletter_subscription", "cms_asset").contains(allowedTable)) throw new IllegalArgumentException("invalid table");
    return scalar("select count(*) from " + allowedTable);
  }

  private long scalar(String sql) { return db.queryForObject(sql, Long.class); }

  private long userId(HttpServletRequest request) {
    Object value = request.getAttribute("userProfileId");
    if (value == null) throw ApiException.forbidden("No platform profile is associated with this token");
    return Long.parseLong(value.toString());
  }
}
