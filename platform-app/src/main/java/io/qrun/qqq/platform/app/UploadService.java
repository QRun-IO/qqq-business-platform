package io.qrun.qqq.platform.app;

import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import java.io.InputStream;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
class UploadService {
  private static final Set<String> ALLOWED_TYPES = Set.of("image/jpeg", "image/png", "image/gif", "image/webp", "application/pdf", "text/plain");
  private final JdbcTemplate db;
  private final MinioClient minio;
  private final String bucket;
  private final long maxBytes;

  UploadService(JdbcTemplate db, MinioClient minio,
      @Value("${minio.bucket:qqq-assets}") String bucket,
      @Value("${minio.max-file-size:10485760}") long maxBytes) {
    this.db = db;
    this.minio = minio;
    this.bucket = bucket;
    this.maxBytes = maxBytes;
  }

  @Transactional
  UploadView upload(long userId, MultipartFile file) {
    if (file == null || file.isEmpty()) throw new IllegalArgumentException("file is required");
    if (file.getSize() > maxBytes) throw new IllegalArgumentException("file exceeds the allowed size");
    String type = file.getContentType() == null ? "application/octet-stream" : file.getContentType().toLowerCase(Locale.ROOT);
    if (!ALLOWED_TYPES.contains(type)) throw new IllegalArgumentException("unsupported file type");
    String name = safeName(file.getOriginalFilename());
    UUID siteId = ensureSite();
    UUID authorId = ensureAuthor(userId);
    UUID id = UUID.randomUUID();
    String objectKey = siteId + "/" + userId + "/" + id + "/" + name;
    try (InputStream input = file.getInputStream()) {
      ensureBucket();
      minio.putObject(PutObjectArgs.builder().bucket(bucket).object(objectKey).contentType(type)
          .stream(input, file.getSize(), -1).build());
      String checksum = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(file.getBytes()));
      Instant now = Instant.now();
      db.update("insert into cms_asset(id,site_id,uploader_id,file_name,object_key,mime_type,file_size,checksum_sha256,upload_status,created_at,updated_at) values(?,?,?,?,?,?,?,?, 'completed',?,?)",
          id, siteId, authorId, name, objectKey, type, file.getSize(), checksum, Timestamp.from(now), Timestamp.from(now));
      return new UploadView(id, name, type, file.getSize(), Instant.now());
    } catch (Exception e) {
      throw new IllegalStateException("Unable to store upload", e);
    }
  }

  ResponseEntity<InputStreamResource> download(UUID id, long userId) {
    List<Asset> assets = db.query("select a.object_key,a.file_name,a.mime_type,a.file_size from cms_asset a join cms_author u on u.id=a.uploader_id where a.id=? and u.user_profile_id=? and a.upload_status='completed'",
        (rs, n) -> map(rs), id, userId);
    if (assets.isEmpty()) throw ApiException.notFound("upload");
    Asset asset = assets.getFirst();
    try {
      InputStream stream = minio.getObject(GetObjectArgs.builder().bucket(bucket).object(asset.key()).build());
      return ResponseEntity.ok().contentType(MediaType.parseMediaType(asset.type())).contentLength(asset.size())
          .header("Content-Disposition", "attachment; filename*=UTF-8''" + java.net.URLEncoder.encode(asset.name(), java.nio.charset.StandardCharsets.UTF_8))
          .body(new InputStreamResource(stream));
    } catch (Exception e) {
      throw new IllegalStateException("Unable to retrieve upload", e);
    }
  }

  ResponseEntity<InputStreamResource> publicDownload(UUID id) {
    List<Asset> assets = db.query("select object_key,file_name,mime_type,file_size from cms_asset where id=? and upload_status='completed'", (rs,n) -> map(rs), id);
    if (assets.isEmpty()) throw ApiException.notFound("upload");
    Asset asset = assets.getFirst();
    try {
      InputStream stream = minio.getObject(GetObjectArgs.builder().bucket(bucket).object(asset.key()).build());
      return ResponseEntity.ok().contentType(MediaType.parseMediaType(asset.type())).contentLength(asset.size())
          .header("Content-Disposition", "inline; filename*=UTF-8''" + java.net.URLEncoder.encode(asset.name(), java.nio.charset.StandardCharsets.UTF_8)).body(new InputStreamResource(stream));
    } catch (Exception e) { throw new IllegalStateException("Unable to retrieve upload", e); }
  }

  List<UploadView> list() {
    return db.query("select id,file_name,mime_type,file_size,created_at from cms_asset where upload_status='completed' order by created_at desc",
        (rs,n) -> new UploadView(rs.getObject("id", UUID.class), rs.getString("file_name"), rs.getString("mime_type"), rs.getLong("file_size"), rs.getTimestamp("created_at").toInstant()));
  }

  @Transactional void delete(UUID id) {
    if (db.update("delete from cms_asset where id=?", id) == 0) throw ApiException.notFound("asset");
  }

  private void ensureBucket() throws Exception {
    if (!minio.bucketExists(BucketExistsArgs.builder().bucket(bucket).build())) {
      minio.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
    }
  }

  private UUID ensureSite() {
    UUID site = UUID.nameUUIDFromBytes("qqq-default-site".getBytes());
    db.update("insert into cms_site(id,name,default_locale,created_at,updated_at) values(?,?,'en',current_timestamp,current_timestamp) on conflict(id) do nothing", site, "Default Site");
    return site;
  }

  private UUID ensureAuthor(long userId) {
    List<UUID> ids = db.query("select id from cms_author where user_profile_id=?", (rs, n) -> rs.getObject(1, UUID.class), userId);
    if (!ids.isEmpty()) return ids.getFirst();
    String name = db.queryForObject("select display_name from user_profile where id=?", String.class, userId);
    UUID id = UUID.randomUUID();
    db.update("insert into cms_author(id,user_profile_id,display_name,created_at,updated_at) values(?,?,?,current_timestamp,current_timestamp)", id, userId, name);
    return id;
  }

  private String safeName(String supplied) {
    try {
      String value = supplied == null ? "upload" : Path.of(supplied).getFileName().toString();
      value = value.replaceAll("[^A-Za-z0-9._-]", "_");
      if (value.isBlank() || value.equals(".") || value.equals("..")) return "upload";
      return value.length() > 200 ? value.substring(value.length() - 200) : value;
    } catch (InvalidPathException e) {
      return "upload";
    }
  }

  private Asset map(ResultSet rs) throws SQLException {
    return new Asset(rs.getString("object_key"), rs.getString("file_name"), rs.getString("mime_type"), rs.getLong("file_size"));
  }

  record UploadView(UUID id, String fileName, String contentType, long size, Instant createdAt) { }
  private record Asset(String key, String name, String type, long size) { }
}
