package io.qrun.qqq.platform.app;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class UserService {
  private final JdbcTemplate db;

  UserService(JdbcTemplate db) { this.db = db; }

  UserView get(long id) {
    List<UserView> users = db.query("select id,oidc_sub,email,display_name,created_at,updated_at from user_profile where id=?",
        (rs, n) -> map(rs), id);
    if (users.isEmpty()) throw ApiException.notFound("user");
    return users.getFirst();
  }

  ContentService.Page<UserView> list(int page, int size) {
    int safePage = Math.max(0, page), safeSize = Math.min(100, Math.max(1, size));
    long total = db.queryForObject("select count(*) from user_profile", Long.class);
    List<UserView> users = db.query("select id,oidc_sub,email,display_name,created_at,updated_at from user_profile order by id limit ? offset ?",
        (rs, n) -> map(rs), safeSize, safePage * safeSize);
    return new ContentService.Page<>(users, safePage, safeSize, total, (total + safeSize - 1) / safeSize);
  }

  @Transactional
  UserView update(long id, ApiModels.UserUpdateRequest request) {
    if (db.update("update user_profile set email=?,display_name=?,updated_at=current_timestamp where id=?",
        request.email(), request.displayName(), id) == 0) throw ApiException.notFound("user");
    return get(id);
  }

  @Transactional
  void delete(long id, long currentUserId) {
    if (id == currentUserId) throw ApiException.conflict("Administrators cannot delete their own active profile");
    if (db.update("delete from user_profile where id=?", id) == 0) throw ApiException.notFound("user");
  }

  private UserView map(ResultSet rs) throws SQLException {
    return new UserView(rs.getLong("id"), rs.getString("oidc_sub"), rs.getString("email"),
        rs.getString("display_name"), instant(rs, "created_at"), instant(rs, "updated_at"));
  }

  private Instant instant(ResultSet rs, String field) throws SQLException {
    Timestamp timestamp = rs.getTimestamp(field);
    return timestamp == null ? null : timestamp.toInstant();
  }

  record UserView(long id, String subject, String email, String displayName, Instant createdAt, Instant updatedAt) { }
}
