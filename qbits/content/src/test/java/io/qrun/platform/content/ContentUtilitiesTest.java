package io.qrun.platform.content;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class ContentUtilitiesTest {
   @Test void validatesTypedJsonBlocksAndRejectsUnsafeLinks() {
      JsonContentValidator validator = new JsonContentValidator();
      assertThat(validator.validate("[{\"type\":\"heading\",\"data\":{\"text\":\"Hello\",\"level\":2}}]")).isEmpty();
      assertThat(validator.validate("[{\"type\":\"link\",\"data\":{\"href\":\"javascript:alert(1)\"}}]")).anyMatch(e -> e.contains("href"));
      assertThat(validator.validate("[{\"type\":\"unknown\",\"data\":{}}]")).anyMatch(e -> e.contains("Unsupported"));
   }

   @Test void resolvesSlugCollisionsDeterministically() {
      SlugService slugs = new SlugService();
      assertThat(slugs.uniqueSlug("  Hello, World! ", Set.of("hello-world", "hello-world-2"))).isEqualTo("hello-world-3");
   }

   @Test void generatesEscapedSeoAndPublishedOnlySitemap() {
      SeoService seo = new SeoService();
      assertThat(seo.metaTags("A <title>", "x & y", "https://example.test/post"))
         .contains("A &lt;title&gt;", "x &amp; y", "canonical");
      String sitemap = seo.sitemap("https://example.test", List.of(
         new SeoService.SitemapEntry("draft", PublicationState.DRAFT, Instant.EPOCH),
         new SeoService.SitemapEntry("live", PublicationState.PUBLISHED, Instant.EPOCH)));
      assertThat(sitemap).contains("/live").doesNotContain("/draft");
   }
}
