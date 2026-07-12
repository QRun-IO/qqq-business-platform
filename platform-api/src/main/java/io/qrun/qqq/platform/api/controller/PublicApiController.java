package io.qrun.qqq.platform.api.controller.pub;

import io.qrun.qqq.platform.newsletter.service.NewsletterService;
import io.qrun.qqq.platform.newsletter.dto.SubscribeRequest;
import io.qrun.qqq.platform.newsletter.dto.UnsubscribeRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * Public API endpoints - no authentication required
 * Rate limiting should be applied at gateway level
 */
@RestController
@RequestMapping("/api/public/v1")
@Tag(name = "Public API", description = "Anonymous access endpoints")
@Validated
public class PublicApiController {

    @Autowired
    private NewsletterService newsletterService;

    /**
     * Newsletter subscription endpoint
     * Implements double opt-in process
     */
    @PostMapping("/newsletter/subscribe")
    @Operation(summary = "Subscribe to newsletter", description = "Initiates double opt-in subscription process")
    public ResponseEntity<Map<String, String>> subscribeNewsletter(
            @Valid @RequestBody SubscribeRequest request,
            HttpServletRequest httpRequest) {
        
        String clientIp = getClientIp(httpRequest);
        newsletterService.subscribe(request.getEmail(), clientIp);
        
        return ResponseEntity.ok(Map.of(
            "message", "Subscription initiated. Please check your email for confirmation.",
            "status", "pending"
        ));
    }
    
    /**
     * Newsletter confirmation endpoint
     * Validates and confirms subscription via token
     */
    @GetMapping("/newsletter/confirm")
    @Operation(summary = "Confirm newsletter subscription", description = "Confirms subscription via email token")
    public ResponseEntity<Map<String, String>> confirmSubscription(
            @RequestParam("token") String token) {
        
        boolean confirmed = newsletterService.confirmSubscription(token);
        
        if (confirmed) {
            return ResponseEntity.ok(Map.of(
                "message", "Subscription confirmed successfully!",
                "status", "confirmed"
            ));
        } else {
            return ResponseEntity.badRequest().body(Map.of(
                "message", "Invalid or expired confirmation token",
                "status", "error"
            ));
        }
    }
    
    /**
     * Newsletter unsubscribe endpoint
     */
    @PostMapping("/newsletter/unsubscribe")
    @Operation(summary = "Unsubscribe from newsletter", description = "Removes email from newsletter and adds to suppression list")
    public ResponseEntity<Map<String, String>> unsubscribe(
            @Valid @RequestBody UnsubscribeRequest request) {
        
        newsletterService.unsubscribe(request.getEmail());
        
        return ResponseEntity.ok(Map.of(
            "message", "Successfully unsubscribed from newsletter",
            "status", "unsubscribed"
        ));
    }
    
    // TODO: Content API endpoints
    // @GetMapping("/content/pages")
    // @GetMapping("/content/posts") 
    // @GetMapping("/content/navigation")
    // @GetMapping("/content/sitemap")
    
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIP = request.getHeader("X-Real-IP");
        if (xRealIP != null && !xRealIP.isEmpty()) {
            return xRealIP;
        }
        
        return request.getRemoteAddr();
    }
}