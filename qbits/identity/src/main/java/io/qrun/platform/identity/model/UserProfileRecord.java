package io.qrun.platform.identity.model;

import java.time.Instant;
import java.util.List;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.data.QField;
import com.kingsrook.qqq.backend.core.model.data.QRecordEntity;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.producers.MetaDataCustomizerInterface;
import com.kingsrook.qqq.backend.core.model.metadata.producers.annotations.QMetaDataProducingEntity;
import com.kingsrook.qqq.backend.core.model.metadata.security.RecordSecurityLock;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QFieldSection;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.Tier;
import io.qrun.platform.identity.IdentityQBitProducer;

@QMetaDataProducingEntity(producePossibleValueSource = true, produceTableMetaData = true,
   tableMetaDataCustomizer = UserProfileRecord.TableCustomizer.class)
public class UserProfileRecord extends QRecordEntity
{
   public static final String TABLE_NAME = "userProfile";

   public static class TableCustomizer implements MetaDataCustomizerInterface<QTableMetaData>
   {
      @Override
      public QTableMetaData customizeMetaData(QInstance instance, QTableMetaData table) throws QException
      {
         return table.withRecordLabelFormat("%s").withRecordLabelFields("displayName")
            .withSection(new QFieldSection("identity", null, Tier.T1, List.of("id", "oidcSub", "email", "displayName")))
            .withSection(new QFieldSection("timestamps", null, Tier.T3, List.of("createdAt", "updatedAt")))
            .withRecordSecurityLock(new RecordSecurityLock().withFieldName("id")
               .withSecurityKeyType(IdentityQBitProducer.USER_PROFILE_ID_SECURITY_KEY)
               .withLockScope(RecordSecurityLock.LockScope.READ_AND_WRITE)
               .withNullValueBehavior(RecordSecurityLock.NullValueBehavior.DENY));
      }
   }

   @QField(isPrimaryKey = true, isEditable = false)
   private Long id;
   @QField(isRequired = true, maxLength = 255)
   private String oidcSub;
   @QField(maxLength = 320)
   private String email;
   @QField(isRequired = true, maxLength = 255)
   private String displayName;
   @QField(isRequired = true, isEditable = false)
   private Instant createdAt;
   @QField(isRequired = true, isEditable = false)
   private Instant updatedAt;

   public Long getId() { return id; }
   public void setId(Long value) { id = value; }
   public String getOidcSub() { return oidcSub; }
   public void setOidcSub(String value) { oidcSub = value; }
   public String getEmail() { return email; }
   public void setEmail(String value) { email = value; }
   public String getDisplayName() { return displayName; }
   public void setDisplayName(String value) { displayName = value; }
   public Instant getCreatedAt() { return createdAt; }
   public void setCreatedAt(Instant value) { createdAt = value; }
   public Instant getUpdatedAt() { return updatedAt; }
   public void setUpdatedAt(Instant value) { updatedAt = value; }
}
