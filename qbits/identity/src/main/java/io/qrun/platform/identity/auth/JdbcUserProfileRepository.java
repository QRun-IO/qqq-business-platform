package io.qrun.platform.identity.auth;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import javax.sql.DataSource;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;

public class JdbcUserProfileRepository implements UserProfileRepository
{
   private final JdbcTemplate jdbc;

   public JdbcUserProfileRepository(DataSource dataSource)
   {
      this.jdbc = new JdbcTemplate(dataSource);
   }

   @Override
   public UserProfile provisionOrUpdate(OidcClaims claims)
   {
      Instant now = Instant.now();
      int updated = jdbc.update("update user_profile set email = ?, display_name = ?, updated_at = ? where oidc_sub = ?",
         claims.email(), claims.displayName(), Timestamp.from(now), claims.subject());
      if(updated == 0)
      {
         try
         {
            jdbc.update("insert into user_profile (oidc_sub, email, display_name, created_at, updated_at) values (?, ?, ?, ?, ?)",
               claims.subject(), claims.email(), claims.displayName(), Timestamp.from(now), Timestamp.from(now));
         }
         catch(DuplicateKeyException race)
         {
            jdbc.update("update user_profile set email = ?, display_name = ?, updated_at = ? where oidc_sub = ?",
               claims.email(), claims.displayName(), Timestamp.from(now), claims.subject());
         }
      }
      return jdbc.queryForObject("select id, oidc_sub, email, display_name, created_at, updated_at from user_profile where oidc_sub = ?",
         this::map, claims.subject());
   }

   private UserProfile map(ResultSet rs, int row) throws SQLException
   {
      return new UserProfile(rs.getLong("id"), rs.getString("oidc_sub"), rs.getString("email"), rs.getString("display_name"),
         rs.getTimestamp("created_at").toInstant(), rs.getTimestamp("updated_at").toInstant());
   }
}
