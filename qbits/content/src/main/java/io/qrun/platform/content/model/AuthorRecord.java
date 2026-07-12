package io.qrun.platform.content.model;
import java.time.Instant; import com.kingsrook.qqq.backend.core.model.data.*; import com.kingsrook.qqq.backend.core.model.metadata.producers.annotations.QMetaDataProducingEntity;
@QMetaDataProducingEntity(produceTableMetaData=true,producePossibleValueSource=true) @lombok.Setter public class AuthorRecord extends QRecordEntity { public static final String TABLE_NAME=ContentTables.AUTHOR;
 @QField(isPrimaryKey=true,isEditable=false) private String id; @QField(isRequired=true) private Long userProfileId; @QField(isRequired=true,maxLength=200) private String displayName; @QField private String bio; @QField(possibleValueSourceName=AssetRecord.TABLE_NAME) private String avatarAssetId; @QField(isRequired=true,isEditable=false) private Instant createdAt; @QField(isRequired=true,isEditable=false) private Instant updatedAt;
 public String getId(){return id;} public Long getUserProfileId(){return userProfileId;} public String getDisplayName(){return displayName;} public String getBio(){return bio;} public String getAvatarAssetId(){return avatarAssetId;} public Instant getCreatedAt(){return createdAt;} public Instant getUpdatedAt(){return updatedAt;}
}
