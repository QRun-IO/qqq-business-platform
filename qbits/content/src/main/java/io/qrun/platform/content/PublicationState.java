package io.qrun.platform.content;

import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.PossibleValueEnum;
import com.kingsrook.qqq.backend.core.model.metadata.producers.annotations.QMetaDataProducingPossibleValueEnum;

@QMetaDataProducingPossibleValueEnum(producePossibleValueSource = true)
public enum PublicationState implements PossibleValueEnum<String> {
   DRAFT("draft"), SCHEDULED("scheduled"), PUBLISHED("published"), ARCHIVED("archived");
   private final String id;
   PublicationState(String id) { this.id = id; }
   @Override public String getPossibleValueId() { return id; }
   @Override public String getPossibleValueLabel() { return name().charAt(0) + name().substring(1).toLowerCase(); }
}
