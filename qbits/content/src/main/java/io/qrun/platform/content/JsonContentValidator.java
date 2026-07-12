package io.qrun.platform.content;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class JsonContentValidator {
   private static final Set<String> TYPES = Set.of("paragraph", "heading", "image", "quote", "list", "code", "link", "embed");
   private final ObjectMapper mapper = new ObjectMapper();
   public List<String> validate(String json) {
      List<String> errors = new ArrayList<>();
      try {
         JsonNode root = mapper.readTree(json);
         if(!root.isArray()) return List.of("Content must be a JSON array of blocks");
         for(int i = 0; i < root.size(); i++) {
            JsonNode block = root.get(i); String type = block.path("type").asText();
            if(!TYPES.contains(type)) errors.add("Unsupported block type at index " + i + ": " + type);
            if(!block.path("data").isObject()) errors.add("Block data must be an object at index " + i);
            if("heading".equals(type) && (block.path("data").path("level").asInt() < 1 || block.path("data").path("level").asInt() > 6)) errors.add("Heading level must be 1-6 at index " + i);
            if("link".equals(type)) {
               String href = block.path("data").path("href").asText();
               if(!(href.startsWith("/") || href.startsWith("https://") || href.startsWith("http://") || href.startsWith("mailto:"))) errors.add("Invalid link href at index " + i);
            }
         }
      } catch(Exception e) { errors.add("Invalid JSON: " + e.getMessage()); }
      return errors;
   }
   public void requireValid(String json) { List<String> errors = validate(json); if(!errors.isEmpty()) throw new IllegalArgumentException(String.join("; ", errors)); }
}
