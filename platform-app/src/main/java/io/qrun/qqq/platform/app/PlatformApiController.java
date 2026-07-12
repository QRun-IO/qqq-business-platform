package io.qrun.qqq.platform.app;

import com.qrunio.platform.newsletter.NewsletterService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
class PlatformApiController {
  private final NewsletterService newsletter;
  private final JdbcTemplate jdbc;

  PlatformApiController(NewsletterService newsletter, JdbcTemplate jdbc) {
    this.newsletter = newsletter;
    this.jdbc = jdbc;
  }

  @GetMapping("/health")
  Map<String, String> health() {
    return Map.of("status", "UP");
  }

  @GetMapping("/api/public/v1/health")
  Map<String, String> publicHealth() {
    return health();
  }

  @PostMapping("/api/public/v1/newsletter/subscribe")
  ResponseEntity<?> subscribe(@Valid @RequestBody SubscribeRequest body, HttpServletRequest request) {
    var result = newsletter.subscribe(body.email(), "{}", request.getRemoteAddr());
    return ResponseEntity.accepted().body(Map.of(
        "subscriptionId", result.subscriptionId(), "status", result.status(),
        "message", "Check your email to confirm"));
  }

  @GetMapping("/api/public/v1/newsletter/confirm")
  ResponseEntity<?> confirm(@RequestParam String token) {
    return newsletter.confirm(token)
        ? ResponseEntity.ok(Map.of("status", "active"))
        : ResponseEntity.status(410).body(Map.of("error", "invalid_or_expired_token"));
  }

  @GetMapping("/api/member/v1/profile")
  ResponseEntity<?> profile(@AuthenticationPrincipal Jwt jwt, HttpServletRequest request) {
    return ResponseEntity.ok(Map.of(
        "userProfileId", request.getAttribute("userProfileId"),
        "subject", jwt.getSubject(),
        "email", jwt.getClaimAsString("email"),
        "displayName", jwt.getClaimAsString("preferred_username")));
  }

  @GetMapping("/api/member/v1/profile/{profileId}")
  ResponseEntity<?> profileById(@PathVariable long profileId, HttpServletRequest request) {
    long ownProfileId = Long.parseLong((String) request.getAttribute("userProfileId"));
    if (profileId != ownProfileId) {
      return ResponseEntity.status(403).body(Map.of("error", "cross_user_access_denied"));
    }
    return ResponseEntity.ok(jdbc.queryForMap(
        "select id, email, display_name as \"displayName\" from user_profile where id = ?", profileId));
  }

  @GetMapping("/api/member/v1/subscriptions")
  ResponseEntity<?> subscriptions(@AuthenticationPrincipal Jwt jwt) {
    var rows = jdbc.queryForList(
        "select id, email, status, confirmed_at as \"confirmedAt\" from newsletter_subscription where lower(email)=lower(?)",
        jwt.getClaimAsString("email"));
    return ResponseEntity.ok(Map.of("subscriptions", rows));
  }

  @RequestMapping("/api/staff/v1/content")
  Map<String, Object> staffContent() {
    return Map.of("items", java.util.List.of());
  }

  record SubscribeRequest(@Email String email) { }
}
