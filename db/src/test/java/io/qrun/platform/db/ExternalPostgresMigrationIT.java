package io.qrun.platform.db;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Set;
import java.util.TreeSet;
import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

@EnabledIfEnvironmentVariable(named = "QQQ_EXTERNAL_DB_URL", matches = ".+")
class ExternalPostgresMigrationIT {
  private static final Set<String> TABLES = Set.of(
      "newsletter_subscription", "newsletter_segment", "newsletter_campaign",
      "outbound_email", "email_suppression", "opt_in_token");

  @Test
  void emptyDatabaseMigratesValidatesAndSeedsDeterministically() throws Exception {
    try (Connection connection = DriverManager.getConnection(
        System.getenv("QQQ_EXTERNAL_DB_URL"),
        System.getenv().getOrDefault("QQQ_EXTERNAL_DB_USER", "qqq"),
        System.getenv().getOrDefault("QQQ_EXTERNAL_DB_PASSWORD", "qqq"))) {
      var database = DatabaseFactory.getInstance()
          .findCorrectDatabaseImplementation(new JdbcConnection(connection));
      try (Liquibase liquibase = new Liquibase("db/changelog/db.changelog-master.yaml",
          new ClassLoaderResourceAccessor(), database)) {
        liquibase.update(new Contexts("baseline"), new LabelExpression());
        liquibase.validate();
        assertEquals(TABLES, tables(connection));
        assertEquals(0, count(connection, "newsletter_subscription"));
        liquibase.update(new Contexts("baseline,demo"), new LabelExpression());
        liquibase.update(new Contexts("baseline,demo"), new LabelExpression());
        assertEquals(1, count(connection, "newsletter_subscription"));
        assertEquals(3, count(connection, "databasechangelog"));
      }
    }
  }

  private static Set<String> tables(Connection connection) throws Exception {
    Set<String> tables = new TreeSet<>();
    try (var statement = connection.prepareStatement("""
        SELECT table_name FROM information_schema.tables
        WHERE table_schema = 'public'
          AND table_name NOT IN ('databasechangelog', 'databasechangeloglock')
        """); var rows = statement.executeQuery()) {
      while (rows.next()) tables.add(rows.getString(1));
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
}