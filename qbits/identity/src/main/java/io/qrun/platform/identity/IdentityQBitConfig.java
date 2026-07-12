package io.qrun.platform.identity;

import java.util.List;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.producers.MetaDataCustomizerInterface;
import com.kingsrook.qqq.backend.core.model.metadata.qbits.QBitConfig;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;

public class IdentityQBitConfig implements QBitConfig
{
   private String defaultBackendNameForTables;
   private MetaDataCustomizerInterface<QTableMetaData> tableMetaDataCustomizer;

   @Override
   public void validate(QInstance qInstance, List<String> errors)
   {
      assertCondition(defaultBackendNameForTables != null && !defaultBackendNameForTables.isBlank(),
         "IdentityQBitConfig.defaultBackendNameForTables is required", errors);
   }

   @Override
   public String getDefaultBackendNameForTables()
   {
      return defaultBackendNameForTables;
   }

   public IdentityQBitConfig withDefaultBackendNameForTables(String value)
   {
      defaultBackendNameForTables = value;
      return this;
   }

   @Override
   public MetaDataCustomizerInterface<QTableMetaData> getTableMetaDataCustomizer()
   {
      return tableMetaDataCustomizer;
   }

   public IdentityQBitConfig withTableMetaDataCustomizer(MetaDataCustomizerInterface<QTableMetaData> value)
   {
      tableMetaDataCustomizer = value;
      return this;
   }
}
