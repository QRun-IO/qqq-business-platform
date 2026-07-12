package io.qrun.platform.content;

import java.time.Duration;
import java.util.Locale;
import java.util.UUID;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.http.Method;

public final class MinioAssetStorage {
   private final MinioClient client; private final String bucket; private final String publicBaseUrl;
   public MinioAssetStorage(MinioClient client, String bucket, String publicBaseUrl) { this.client = client; this.bucket = bucket; this.publicBaseUrl = publicBaseUrl; }
   public String objectKey(String siteKey, String fileName) {
      String safeSite = sanitize(siteKey); String safeFile = sanitize(fileName);
      if(safeFile.isBlank()) safeFile = "asset";
      return safeSite + "/" + UUID.randomUUID() + "/" + safeFile;
   }
   public String presignUpload(String siteKey, String fileName, String contentType, Duration expiry) {
      try {
         String key = objectKey(siteKey, fileName);
         return client.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder().method(Method.PUT).bucket(bucket).object(key).expiry((int) expiry.toSeconds()).build());
      } catch(Exception e) { throw new IllegalStateException("Unable to create MinIO upload URL", e); }
   }
   public String publicUrl(String key) { return publicBaseUrl.replaceAll("/$", "") + "/" + bucket + "/" + key; }
   private String sanitize(String value) { return (value == null ? "" : value).toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9._-]+", "-").replaceAll("\\.{2,}", "-").replaceAll("-+", "-").replaceAll("(^[-._]+|[-._]+$)", ""); }
}
