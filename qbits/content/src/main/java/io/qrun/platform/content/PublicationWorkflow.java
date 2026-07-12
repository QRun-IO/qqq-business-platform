package io.qrun.platform.content;

import java.time.Instant;
import java.util.Map;
import java.util.Set;

public final class PublicationWorkflow {
   private static final Map<PublicationState, Set<PublicationState>> TRANSITIONS = Map.of(
      PublicationState.DRAFT, Set.of(PublicationState.SCHEDULED, PublicationState.PUBLISHED, PublicationState.ARCHIVED),
      PublicationState.SCHEDULED, Set.of(PublicationState.DRAFT, PublicationState.PUBLISHED, PublicationState.ARCHIVED),
      PublicationState.PUBLISHED, Set.of(PublicationState.ARCHIVED),
      PublicationState.ARCHIVED, Set.of(PublicationState.DRAFT));

   public boolean canTransition(PublicationState from, PublicationState to) { return from == to || TRANSITIONS.getOrDefault(from, Set.of()).contains(to); }
   public void validateTransition(PublicationState from, PublicationState to, Instant scheduledAt, Instant now) {
      if(!canTransition(from, to)) throw new IllegalStateException("Invalid publication transition: " + from + " -> " + to);
      if(to == PublicationState.SCHEDULED && (scheduledAt == null || !scheduledAt.isAfter(now))) throw new IllegalArgumentException("Scheduled publication requires a future scheduledAt");
   }
}
