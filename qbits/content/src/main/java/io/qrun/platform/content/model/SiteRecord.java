package io.qrun.platform.content.model;
import java.time.Instant;
import com.kingsrook.qqq.backend.core.model.data.*; import com.kingsrook.qqq.backend.core.model.metadata.producers.annotations.QMetaDataProducingEntity;
@QMetaDataProducingEntity(produceTableMetaData=true, producePossibleValueSource=true)
@lombok.Setter public class SiteRecord extends QRecordEntity { public static final String TABLE_NAME=ContentTables.SITE;
 @QField(isPrimaryKey=true,isEditable=false) private String id; @QField(isRequired=true,maxLength=200) private String name; @QField(maxLength=255) private String domain; @QField(maxLength=10) private String defaultLocale; @QField(isRequired=true,isEditable=false) private Instant createdAt; @QField(isRequired=true,isEditable=false) private Instant updatedAt;
 public String getId(){return id;} public String getName(){return name;} public String getDomain(){return domain;} public String getDefaultLocale(){return defaultLocale;} public Instant getCreatedAt(){return createdAt;} public Instant getUpdatedAt(){return updatedAt;}
}
