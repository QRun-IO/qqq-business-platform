package io.qrun.platform.identity;

import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.metadata.MetaDataProducerMultiOutput;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.qbits.QBitMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.qbits.QBitMetaDataProducer;
import com.kingsrook.qqq.backend.core.model.metadata.security.QSecurityKeyType;

public class IdentityQBitProducer implements QBitMetaDataProducer<IdentityQBitConfig>
{
   public static final String GROUP_ID = "io.qrun.platform";
   public static final String ARTIFACT_ID = "identity";
   public static final String VERSION = "0.1.0";
   public static final String USER_PROFILE_ID_SECURITY_KEY = "userProfileId";

   private IdentityQBitConfig qBitConfig;

   @Override
   public QBitMetaData getQBitMetaData()
   {
      return new QBitMetaData().withGroupId(GROUP_ID).withArtifactId(ARTIFACT_ID).withVersion(VERSION)
         .withNamespace(getNamespace()).withConfig(qBitConfig);
   }

   @Override
   public IdentityQBitConfig getQBitConfig()
   {
      return qBitConfig;
   }

   public IdentityQBitProducer withQBitConfig(IdentityQBitConfig value)
   {
      qBitConfig = value;
      return this;
   }

   @Override
   public void postProduceActions(MetaDataProducerMultiOutput output, QInstance instance) throws QException
   {
      instance.addSecurityKeyType(new QSecurityKeyType().withName(USER_PROFILE_ID_SECURITY_KEY)
         .withPossibleValueSourceName(io.qrun.platform.identity.model.UserProfileRecord.TABLE_NAME));
   }
}
