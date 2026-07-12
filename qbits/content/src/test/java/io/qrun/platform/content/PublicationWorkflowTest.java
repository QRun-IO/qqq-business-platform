package io.qrun.platform.content;

import java.time.Instant;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class PublicationWorkflowTest {
   private final PublicationWorkflow workflow = new PublicationWorkflow();

   @Test void supportsOnlyDefinedTransitions() {
      assertThat(workflow.canTransition(PublicationState.DRAFT, PublicationState.SCHEDULED)).isTrue();
      assertThat(workflow.canTransition(PublicationState.DRAFT, PublicationState.PUBLISHED)).isTrue();
      assertThat(workflow.canTransition(PublicationState.SCHEDULED, PublicationState.PUBLISHED)).isTrue();
      assertThat(workflow.canTransition(PublicationState.PUBLISHED, PublicationState.ARCHIVED)).isTrue();
      assertThat(workflow.canTransition(PublicationState.ARCHIVED, PublicationState.PUBLISHED)).isFalse();
   }

   @Test void scheduledPublicationRequiresFutureTime() {
      assertThatThrownBy(() -> workflow.validateTransition(PublicationState.DRAFT, PublicationState.SCHEDULED, Instant.now().minusSeconds(1), Instant.now()))
         .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("future");
   }
}
