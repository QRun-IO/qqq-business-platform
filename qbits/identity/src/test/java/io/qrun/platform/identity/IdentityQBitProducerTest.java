package io.qrun.platform.identity;

import com.kingsrook.qqq.backend.core.model.metadata.MetaDataProducerMultiOutput;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.security.RecordSecurityLock;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import io.qrun.platform.identity.model.UserProfileRecord;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class IdentityQBitProducerTest
{
   @Test
   void producesProfileTableAndSelfOwnershipSecurity() throws Exception
   {
      QInstance instance = new QInstance();
      IdentityQBitConfig config = new IdentityQBitConfig().withDefaultBackendNameForTables("rdbms");
      MetaDataProducerMultiOutput output = new IdentityQBitProducer().withQBitConfig(config).produce(instance);
      output.addSelfToInstance(instance);

      QTableMetaData table = instance.getTable(UserProfileRecord.TABLE_NAME);
      assertThat(table.getFields().keySet()).contains("id", "oidcSub", "email", "displayName", "createdAt", "updatedAt");
      assertThat(table.getRecordSecurityLocks()).singleElement().satisfies(lock -> {
         assertThat(lock.getFieldName()).isEqualTo("id");
         assertThat(lock.getSecurityKeyType()).isEqualTo("userProfileId");
         assertThat(lock.getLockScope()).isEqualTo(RecordSecurityLock.LockScope.READ_AND_WRITE);
         assertThat(lock.getNullValueBehavior()).isEqualTo(RecordSecurityLock.NullValueBehavior.DENY);
      });
      assertThat(instance.getSecurityKeyType("userProfileId")).isNotNull();
   }
}
