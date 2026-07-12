package io.qrun.platform.content;

import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.metadata.MetaDataProducerMultiOutput;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.qbits.QBitMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.qbits.QBitMetaDataProducer;
import com.kingsrook.qqq.backend.core.model.metadata.security.QSecurityKeyType;

public final class ContentQBitProducer implements QBitMetaDataProducer<ContentQBitConfig> {
   public static final String GROUP_ID="io.qrun.platform", ARTIFACT_ID="content", VERSION="0.1.0";
   public static final String PUBLICATION_STATE_SECURITY_KEY="publicationState";
   private ContentQBitConfig config;
   @Override public QBitMetaData getQBitMetaData() { return new QBitMetaData().withGroupId(GROUP_ID).withArtifactId(ARTIFACT_ID).withVersion(VERSION).withNamespace(getNamespace()).withConfig(config); }
   @Override public ContentQBitConfig getQBitConfig() { return config; }
   public ContentQBitProducer withQBitConfig(ContentQBitConfig value) { config=value; return this; }
   @Override public void postProduceActions(MetaDataProducerMultiOutput output, QInstance instance) throws QException {
      instance.addSecurityKeyType(new QSecurityKeyType().withName(PUBLICATION_STATE_SECURITY_KEY).withPossibleValueSourceName(PublicationState.class.getSimpleName()));
   }
}
