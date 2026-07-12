package com.qrunio.platform.newsletter;

import static org.assertj.core.api.Assertions.*;
import java.time.*;
import java.sql.Timestamp;
import java.util.*;
import org.junit.jupiter.api.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.*;

class NewsletterServiceTest {
  JdbcTemplate db; NewsletterService service; MutableClock clock;
  @BeforeEach void setup() {
    db = new JdbcTemplate(new EmbeddedDatabaseBuilder().generateUniqueName(true).setType(EmbeddedDatabaseType.H2).addScript("schema-h2.sql").build());
    clock = new MutableClock(Instant.parse("2026-07-12T12:00:00Z"));
    service = new NewsletterService(db, clock, "https://site.test/api/public/v1/newsletter/confirm");
  }
  @Test void subscribeCreatesPendingSubscriptionTokenAndConfirmationOutbox() {
    var result = service.subscribe(" Person@Example.COM ", "{\"topics\":[\"news\"]}", "127.0.0.1");
    assertThat(result.status()).isEqualTo("pending");
    assertThat(db.queryForObject("select email from newsletter_subscription", String.class)).isEqualTo("person@example.com");
    assertThat(db.queryForObject("select count(*) from opt_in_token where consumed_at is null", Integer.class)).isEqualTo(1);
    assertThat(db.queryForObject("select status from outbound_email", String.class)).isEqualTo("pending");
  }
  @Test void validTokenConfirmsExactlyOnceAndSyncsCrm() {
    var result = service.subscribe("person@example.com", "{}", "127.0.0.1");
    assertThat(service.confirm(result.rawToken())).isTrue();
    assertThat(service.confirm(result.rawToken())).isFalse();
    assertThat(db.queryForObject("select status from newsletter_subscription", String.class)).isEqualTo("confirmed");
    assertThat(db.queryForObject("select count(*) from outbound_email where message_type='crm_sync'", Integer.class)).isEqualTo(1);
  }
  @Test void expiredTokenIsRejected() {
    var result = service.subscribe("person@example.com", "{}", "127.0.0.1");
    clock.instant = clock.instant.plus(Duration.ofDays(3));
    assertThat(service.confirm(result.rawToken())).isFalse();
  }
  @Test void suppressedAddressCannotSubscribe() {
    db.update("insert into email_suppression(id,email,reason,source,created_at) values(?,?,?,?,?)", UUID.randomUUID(), "bad@example.com", "hard_bounce", "provider", Timestamp.from(clock.instant()));
    assertThatThrownBy(() -> service.subscribe("bad@example.com", "{}", "ip")).isInstanceOf(IllegalStateException.class);
  }
  static final class MutableClock extends Clock {
    Instant instant; MutableClock(Instant i){instant=i;} public ZoneId getZone(){return ZoneOffset.UTC;} public Clock withZone(ZoneId z){return this;} public Instant instant(){return instant;}
  }
}
