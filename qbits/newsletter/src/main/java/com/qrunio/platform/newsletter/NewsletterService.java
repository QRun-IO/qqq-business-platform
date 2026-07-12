package com.qrunio.platform.newsletter;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.sql.Timestamp;
import java.time.*;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NewsletterService {
  private final JdbcTemplate db; private final Clock clock; private final String confirmUrl; private final SecureRandom random = new SecureRandom();
  public NewsletterService(JdbcTemplate db, Clock clock, String confirmUrl){this.db=db;this.clock=clock;this.confirmUrl=confirmUrl;}
  @Transactional public SubscribeResult subscribe(String suppliedEmail,String preferences,String ip){
    String email=normalize(suppliedEmail); Instant now=clock.instant();
    Integer suppressed=db.queryForObject("select count(*) from email_suppression where email=? and (expires_at is null or expires_at>?)",Integer.class,email,ts(now));
    if(suppressed!=null&&suppressed>0) throw new IllegalStateException("Address is suppressed");
    List<UUID> ids=db.query("select id from newsletter_subscription where email=?",(r,n)->r.getObject(1,UUID.class),email);
    UUID subscriptionId=ids.isEmpty()?UUID.randomUUID():ids.get(0);
    if(ids.isEmpty()) db.update("insert into newsletter_subscription(id,email,status,preferences,source_ip,created_at,updated_at) values(?,?,?,?,?,?,?)",subscriptionId,email,"pending",preferences,ip,ts(now),ts(now));
    else db.update("update newsletter_subscription set status='pending',preferences=?,source_ip=?,updated_at=? where id=? and status<>'confirmed'",preferences,ip,ts(now),subscriptionId);
    byte[] token=new byte[32]; random.nextBytes(token); String raw=Base64.getUrlEncoder().withoutPadding().encodeToString(token);
    db.update("delete from opt_in_token where subscription_id=? and consumed_at is null",subscriptionId);
    db.update("insert into opt_in_token(id,subscription_id,token_hash,expires_at,created_at) values(?,?,?,?,?)",UUID.randomUUID(),subscriptionId,hash(raw),ts(now.plus(Duration.ofHours(48))),ts(now));
    String body="Confirm your subscription: "+confirmUrl+"?token="+raw;
    db.update("insert into outbound_email(id,subscription_id,message_type,recipient,subject,body,status,next_attempt_at,created_at) values(?,?,?,?,?,?,?,?,?)",UUID.randomUUID(),subscriptionId,"opt_in",email,"Confirm your newsletter subscription",body,"pending",ts(now),ts(now));
    return new SubscribeResult(subscriptionId,"pending",raw);
  }
  @Transactional public boolean confirm(String raw){
    Instant now=clock.instant(); List<UUID> ids=db.query("select subscription_id from opt_in_token where token_hash=? and consumed_at is null and expires_at>?",(r,n)->r.getObject(1,UUID.class),hash(raw),ts(now));
    if(ids.isEmpty()) return false; UUID id=ids.get(0);
    db.update("update opt_in_token set consumed_at=? where token_hash=? and consumed_at is null",ts(now),hash(raw));
    db.update("update newsletter_subscription set status='confirmed',confirmed_at=?,updated_at=? where id=?",ts(now),ts(now),id);
    String email=db.queryForObject("select email from newsletter_subscription where id=?",String.class,id);
    db.update("insert into outbound_email(id,subscription_id,message_type,recipient,subject,body,status,next_attempt_at,created_at) values(?,?,?,?,?,?,?,?,?)",UUID.randomUUID(),id,"crm_sync",email,"CRM subscriber sync","{\"event\":\"newsletter.confirmed\"}","pending",ts(now),ts(now));
    return true;
  }
  @Transactional public void unsubscribe(String suppliedEmail){String email=normalize(suppliedEmail);Instant n=clock.instant();db.update("update newsletter_subscription set status='unsubscribed',unsubscribed_at=?,updated_at=? where email=?",ts(n),ts(n),email);int changed=db.update("update email_suppression set reason='unsubscribe',source='self_service',expires_at=null where email=?",email);if(changed==0)db.update("insert into email_suppression(id,email,reason,source,created_at) values(?,?,?,?,?)",UUID.randomUUID(),email,"unsubscribe","self_service",ts(n));}
  static String normalize(String e){if(e==null)throw new IllegalArgumentException("email required");String n=e.trim().toLowerCase(Locale.ROOT);if(!n.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")||n.length()>320)throw new IllegalArgumentException("invalid email");return n;}
  static String hash(String value){try{byte[] h=MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));return HexFormat.of().formatHex(h);}catch(NoSuchAlgorithmException e){throw new IllegalStateException(e);}}
  static Timestamp ts(Instant i){return Timestamp.from(i);} public record SubscribeResult(UUID subscriptionId,String status,String rawToken){}
}
