package io.qrun.platform.content;

import com.kingsrook.qqq.backend.core.model.metadata.MetaDataProducerMultiOutput;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.security.RecordSecurityLock;
import io.qrun.platform.content.model.ContentTables;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ContentQBitProducerTest {
   @Test void producesAllTenCmsTablesAndPublicationSecurity() throws Exception {
      QInstance instance = new QInstance();
      ContentQBitConfig config = new ContentQBitConfig().withDefaultBackendNameForTables("rdbms")
         .withAssetBucket("content").withAssetPublicBaseUrl("https://cdn.example.test");
      MetaDataProducerMultiOutput output = new ContentQBitProducer().withQBitConfig(config).produce(instance);
      output.addSelfToInstance(instance);

      assertThat(ContentTables.ALL).hasSize(10).allSatisfy(name -> assertThat(instance.getTable(name)).isNotNull());
      assertThat(instance.getTable("cmsPage").getFields().keySet())
         .contains("siteId", "slug", "content", "publicationState", "seoTitle", "structuredData");
      assertThat(instance.getTable("cmsBlogPost").getRecordSecurityLocks()).singleElement().satisfies(lock -> {
         assertThat(lock.getFieldName()).isEqualTo("publicationState");
         assertThat(lock.getSecurityKeyType()).isEqualTo(ContentQBitProducer.PUBLICATION_STATE_SECURITY_KEY);
         assertThat(lock.getLockScope()).isEqualTo(RecordSecurityLock.LockScope.READ);
      });
      assertThat(instance.getSecurityKeyType(ContentQBitProducer.PUBLICATION_STATE_SECURITY_KEY)).isNotNull();
   }

   @Test void validatesRequiredConfiguration() {
      assertThat(new ContentQBitConfig().validationErrors(new QInstance()))
         .contains("ContentQBitConfig.defaultBackendNameForTables is required", "ContentQBitConfig.assetBucket is required");
   }
}
