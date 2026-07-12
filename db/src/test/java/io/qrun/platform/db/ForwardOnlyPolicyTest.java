package io.qrun.platform.db;

import static org.junit.jupiter.api.Assertions.assertFalse;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class ForwardOnlyPolicyTest {

  @Test
  void committedChangesAreForwardOnlyAndChecksummed() throws Exception {
    Path changelogRoot = Path.of("src/main/resources/db/changelog");
    try (var paths = Files.walk(changelogRoot)) {
      for (Path changelog : paths.filter(path -> path.toString().endsWith(".yaml")).toList()) {
        String contents = Files.readString(changelog);
        assertFalse(contents.contains("validCheckSum: ANY"), changelog + " disables checksum validation");
        assertFalse(contents.matches("(?s).*\\brollback\\s*:.*"), changelog + " defines rollback in a forward-only project");
        assertFalse(contents.contains("runOnChange: true"), changelog + " mutates a released changeset");
        assertFalse(contents.contains("runAlways: true"), changelog + " is not deterministic");
      }
    }
  }
}
