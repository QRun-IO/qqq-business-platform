package io.qrun.platform.content;

import java.util.ArrayList;
import java.util.List;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.producers.MetaDataCustomizerInterface;
import com.kingsrook.qqq.backend.core.model.metadata.qbits.QBitConfig;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;

public class ContentQBitConfig implements QBitConfig {
   private String defaultBackendNameForTables;
   private String assetBucket;
   private String assetPublicBaseUrl;
   private MetaDataCustomizerInterface<QTableMetaData> tableMetaDataCustomizer;

   @Override public void validate(QInstance instance, List<String> errors) {
      assertCondition(defaultBackendNameForTables != null && !defaultBackendNameForTables.isBlank(), "ContentQBitConfig.defaultBackendNameForTables is required", errors);
      assertCondition(assetBucket != null && !assetBucket.isBlank(), "ContentQBitConfig.assetBucket is required", errors);
   }
   public List<String> validationErrors(QInstance instance) { List<String> errors = new ArrayList<>(); validate(instance, errors); return errors; }
   @Override public String getDefaultBackendNameForTables() { return defaultBackendNameForTables; }
   public ContentQBitConfig withDefaultBackendNameForTables(String value) { defaultBackendNameForTables = value; return this; }
   public String getAssetBucket() { return assetBucket; }
   public ContentQBitConfig withAssetBucket(String value) { assetBucket = value; return this; }
   public String getAssetPublicBaseUrl() { return assetPublicBaseUrl; }
   public ContentQBitConfig withAssetPublicBaseUrl(String value) { assetPublicBaseUrl = value; return this; }
   @Override public MetaDataCustomizerInterface<QTableMetaData> getTableMetaDataCustomizer() { return tableMetaDataCustomizer; }
   public ContentQBitConfig withTableMetaDataCustomizer(MetaDataCustomizerInterface<QTableMetaData> value) { tableMetaDataCustomizer = value; return this; }
}
