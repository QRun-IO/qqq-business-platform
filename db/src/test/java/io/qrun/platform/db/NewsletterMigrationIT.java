package io.qrun.platform.db;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Set;
import java.util.TreeSet;
import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@DisabledIfEnvironmentVariable(named = "QQQ_EXTERNAL_DB_URL", matches = ".+")
class NewsletterMigrationIT {

  private static final String CHANGELOG = "db/changelog/db.changelog-master.yaml";
  private static final Set<String> PLATFORM_TABLES = Set.of(
      "newsletter_subscription", "newsletter_segment", "newsletter_campaign",
      "outbound_email", "email_suppression", "opt_in_token",
      "user_profile", "local_registration", "cms_site", "cms_asset", "cms_author",
      "cms_page", "cms_blog_post", "cms_tag", "cms_category", "cms_navigation",
      "cms_redirect", "cms_content_revision", "cms_blog_post_category", "cms_blog_post_tag");

  @Container
  final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
      .withDatabaseName("qqq")
      .withUsername("qqq")
      .withPassword("qqq");

  @Test
  void emptyDatabaseMigratesAndValidatesWithBaselineContext() throws Exception {
    try (Connection connection = connect(); Liquibase liquibase = liquibase(connection)) {
      assertTrue(applicationTables(connection).isEmpty());
      liquibase.update(new Contexts("baseline"), new LabelExpression());
      liquibase.validate();

      assertEquals(PLATFORM_TABLES, applicationTables(connection));
      assertEquals(0, count(connection, "newsletter_subscription"));
      assertEquals(0, count(connection, "newsletter_campaign"));
      assertEquals(6, appliedChangeSets(connection));
    }
  }

  @Test
  void demoContextIsDeterministicAndDoesNotRunByDefault() throws Exception {
    try (Connection connection = connect(); Liquibase liquibase = liquibase(connection)) {
      liquibase.update(new Contexts("baseline"), new LabelExpression());
      assertEquals(0, count(connection, "newsletter_subscription"));

      liquibase.update(new Contexts("baseline,demo"), new LabelExpression());
      assertEquals(1, count(connection, "newsletter_subscription"));
      assertEquals(1, count(connection, "newsletter_segment"));
      assertEquals(1, count(connection, "newsletter_campaign"));
      assertEquals(7, appliedChangeSets(connection));

      liquibase.update(new Contexts("baseline,demo"), new LabelExpression());
      assertEquals(1, count(connection, "newsletter_subscription"));
      assertEquals(7, appliedChangeSets(connection));
    }
  }

  private Connection connect() throws Exception {
    return DriverManager.getConnection(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
  }

  private static Liquibase liquibase(Connection connection) throws Exception {
    Database database = DatabaseFactory.getInstance()
        .findCorrectDatabaseImplementation(new JdbcConnection(connection));
    return new Liquibase(CHANGELOG, new ClassLoaderResourceAccessor(), database);
  }

  private static Set<String> applicationTables(Connection connection) throws Exception {
    Set<String> tables = new TreeSet<>();
    try (var statement = connection.prepareStatement("""
        SELECT table_name FROM information_schema.tables
        WHERE table_schema = 'public'
          AND table_name NOT IN ('databasechangelog', 'databasechangeloglock')
        """); var rows = statement.executeQuery()) {
      while (rows.next()) {
        tables.add(rows.getString(1));
      }
    }
    return tables;
  }

  private static int count(Connection connection, String table) throws Exception {
    try (var statement = connection.createStatement();
         var rows = statement.executeQuery("SELECT count(*) FROM " + table)) {
      rows.next();
      return rows.getInt(1);
    }
  }

  private static int appliedChangeSets(Connection connection) throws Exception {
    try (var statement = connection.createStatement();
         var rows = statement.executeQuery("SELECT count(*) FROM databasechangelog")) {
      rows.next();
      return rows.getInt(1);
    }
  }
}
