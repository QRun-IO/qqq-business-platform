package io.qrun.platform.content;

import java.text.Normalizer;
import java.util.Locale;
import java.util.Set;

public final class SlugService {
   public String slugify(String input) {
      String slug = Normalizer.normalize(input == null ? "" : input, Normalizer.Form.NFKD).replaceAll("\\p{M}", "")
         .toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
      if(slug.isBlank()) throw new IllegalArgumentException("A slug requires at least one letter or number");
      return slug;
   }
   public String uniqueSlug(String input, Set<String> existing) {
      String base = slugify(input); String candidate = base; int suffix = 2;
      while(existing.contains(candidate)) candidate = base + "-" + suffix++;
      return candidate;
   }
}
