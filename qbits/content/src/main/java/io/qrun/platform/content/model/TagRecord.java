package io.qrun.platform.content.model;
import java.time.Instant; import com.kingsrook.qqq.backend.core.model.data.*; import com.kingsrook.qqq.backend.core.model.metadata.producers.annotations.QMetaDataProducingEntity;
@QMetaDataProducingEntity(produceTableMetaData=true,producePossibleValueSource=true) @lombok.Setter public class TagRecord extends QRecordEntity { public static final String TABLE_NAME=ContentTables.TAG;
 @QField(isPrimaryKey=true,isEditable=false) private String id; @QField(isRequired=true,possibleValueSourceName=SiteRecord.TABLE_NAME) private String siteId; @QField(isRequired=true,maxLength=150) private String name; @QField(isRequired=true,maxLength=150) private String slug; @QField private String description; @QField(isRequired=true,isEditable=false) private Instant createdAt; @QField(isRequired=true,isEditable=false) private Instant updatedAt;
 public String getId(){return id;} public String getSiteId(){return siteId;} public String getName(){return name;} public String getSlug(){return slug;} public String getDescription(){return description;} public Instant getCreatedAt(){return createdAt;} public Instant getUpdatedAt(){return updatedAt;}
}
