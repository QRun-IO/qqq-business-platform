package com.qrunio.platform.newsletter;
import static org.assertj.core.api.Assertions.*;
import java.sql.Timestamp; import java.time.*; import java.util.*;
import org.junit.jupiter.api.*; import org.springframework.jdbc.core.JdbcTemplate; import org.springframework.jdbc.datasource.embedded.*;
class OutboxWorkerTest {
 JdbcTemplate db; NewsletterServiceTest.MutableClock clock;
 @BeforeEach void setup(){db=new JdbcTemplate(new EmbeddedDatabaseBuilder().generateUniqueName(true).setType(EmbeddedDatabaseType.H2).addScript("schema-h2.sql").build());clock=new NewsletterServiceTest.MutableClock(Instant.parse("2026-01-01T00:00:00Z"));}
 @Test void successfulDeliveryMarksSent(){insert("pending",0);new OutboxWorker(db,m->new EmailProvider.DeliveryResult("id-1"),clock,10).runOnce();assertThat(db.queryForObject("select status from outbound_email",String.class)).isEqualTo("sent");}
 @Test void failedDeliverySchedulesExponentialRetry(){insert("pending",0);new OutboxWorker(db,m->{throw new RuntimeException("down");},clock,10).runOnce();assertThat(db.queryForObject("select status from outbound_email",String.class)).isEqualTo("retry");assertThat(db.queryForObject("select attempt_count from outbound_email",Integer.class)).isEqualTo(1);}
 @Test void suppressionSkipsDelivery(){db.update("insert into email_suppression(id,email,reason,source,created_at) values(?,?,?,?,?)",UUID.randomUUID(),"a@b.com","bounce","provider",Timestamp.from(clock.instant()));insert("pending",0);new OutboxWorker(db,m->{throw new AssertionError();},clock,10).runOnce();assertThat(db.queryForObject("select status from outbound_email",String.class)).isEqualTo("suppressed");}
 void insert(String status,int attempts){db.update("insert into outbound_email(id,message_type,recipient,subject,body,status,attempt_count,max_attempts,next_attempt_at,created_at) values(?,?,?,?,?,?,?,?,?,?)",UUID.randomUUID(),"campaign","a@b.com","s","b",status,attempts,5,Timestamp.from(clock.instant()),Timestamp.from(clock.instant()));}
}
