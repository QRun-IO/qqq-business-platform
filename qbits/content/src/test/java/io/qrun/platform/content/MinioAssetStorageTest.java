package io.qrun.platform.content;

import io.minio.MinioClient;
import org.junit.jupiter.api.Test;
import java.time.Duration;
import static org.assertj.core.api.Assertions.*;

class MinioAssetStorageTest {
   @Test void buildsTenantSafeObjectKeysAndPublicUrls() {
      MinioAssetStorage storage = new MinioAssetStorage(MinioClient.builder().endpoint("http://localhost:9000").credentials("minioadmin", "minioadmin").build(), "content", "https://cdn.example.test");
      String key = storage.objectKey("site/../one", "../Hero Image.PNG");
      assertThat(key).matches("site-one/[0-9a-f-]{36}/hero-image.png");
      assertThat(storage.publicUrl(key)).isEqualTo("https://cdn.example.test/content/" + key);
      assertThatThrownBy(() -> storage.presignUpload("site", "hero.png", "image/png", Duration.ofMinutes(10)))
         .isInstanceOf(IllegalStateException.class).hasMessageContaining("MinIO");
   }
}
