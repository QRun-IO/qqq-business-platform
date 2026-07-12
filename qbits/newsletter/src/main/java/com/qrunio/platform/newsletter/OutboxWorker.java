package com.qrunio.platform.newsletter;
import java.sql.Timestamp; import java.time.*; import java.util.*;
import org.springframework.beans.factory.annotation.Value; import org.springframework.jdbc.core.JdbcTemplate; import org.springframework.scheduling.annotation.Scheduled; import org.springframework.stereotype.Component; import org.springframework.transaction.annotation.Transactional;
@Component public class OutboxWorker {
 private final JdbcTemplate db; private final EmailProvider provider; private final Clock clock; private final int batchSize;
 public OutboxWorker(JdbcTemplate db,EmailProvider provider,Clock clock,@Value("${newsletter.worker.batch-size:50}") int batchSize){this.db=db;this.provider=provider;this.clock=clock;this.batchSize=batchSize;}
 @Scheduled(fixedDelayString="${newsletter.worker.delay-ms:5000}") @Transactional public void runOnce(){
  Instant now=clock.instant(); List<Mail> rows=db.query("select id,recipient,subject,body,attempt_count,max_attempts,message_type from outbound_email where status in ('pending','retry') and next_attempt_at<=? order by created_at fetch first ? rows only",(r,n)->new Mail(r.getObject(1,UUID.class),r.getString(2),r.getString(3),r.getString(4),r.getInt(5),r.getInt(6),r.getString(7)),Timestamp.from(now),batchSize);
  for(Mail m:rows){
   Integer suppressed=db.queryForObject("select count(*) from email_suppression where email=? and (expires_at is null or expires_at>?)",Integer.class,m.to,Timestamp.from(now));
   if((suppressed!=null&&suppressed>0)&&!m.type.equals("crm_sync")){db.update("update outbound_email set status='suppressed',last_error='suppression list' where id=?",m.id);continue;}
   if(m.type.equals("crm_sync")){db.update("update outbound_email set status='sent',sent_at=? where id=?",Timestamp.from(now),m.id);continue;}
   try{var result=provider.send(new EmailProvider.EmailMessage(m.to,m.subject,m.body));db.update("update outbound_email set status='sent',attempt_count=attempt_count+1,provider_message_id=?,sent_at=?,last_error=null where id=?",result.providerMessageId(),Timestamp.from(now),m.id);}
   catch(Exception e){int attempt=m.attempts+1;String status=attempt>=m.max?"failed":"retry";long delay=Math.min(3600,30L*(1L<<Math.min(attempt-1,7)));db.update("update outbound_email set status=?,attempt_count=?,next_attempt_at=?,last_error=? where id=?",status,attempt,Timestamp.from(now.plusSeconds(delay)),safe(e),m.id);}
  }
 }
 private static String safe(Exception e){String s=e.getMessage()==null?e.getClass().getSimpleName():e.getMessage();return s.substring(0,Math.min(s.length(),2000));}
 private record Mail(UUID id,String to,String subject,String body,int attempts,int max,String type){}
}
