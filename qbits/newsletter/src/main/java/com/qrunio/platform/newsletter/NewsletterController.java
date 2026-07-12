package com.qrunio.platform.newsletter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import java.util.Map;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/public/v1/newsletter") public class NewsletterController {
 private final NewsletterService service; NewsletterController(NewsletterService s){service=s;}
 @PostMapping("/subscribe") ResponseEntity<?> subscribe(@Valid @RequestBody SubscribeRequest r,HttpServletRequest req){var x=service.subscribe(r.email(),r.preferences()==null?"{}":r.preferences(),req.getRemoteAddr());return ResponseEntity.accepted().body(Map.of("subscriptionId",x.subscriptionId(),"status",x.status(),"message","Check your email to confirm"));}
 @GetMapping("/confirm") ResponseEntity<?> confirm(@RequestParam String token){return service.confirm(token)?ResponseEntity.ok(Map.of("status","confirmed")):ResponseEntity.status(410).body(Map.of("error","invalid_or_expired_token"));}
 @PostMapping("/unsubscribe") ResponseEntity<?> unsubscribe(@Valid @RequestBody UnsubscribeRequest r){service.unsubscribe(r.email());return ResponseEntity.ok(Map.of("status","unsubscribed"));}
 record SubscribeRequest(@Email String email,String preferences){} record UnsubscribeRequest(@Email String email){}
}
