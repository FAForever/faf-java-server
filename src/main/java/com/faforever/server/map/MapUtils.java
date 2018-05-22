package com.faforever.server.map;

import lombok.experimental.UtilityClass;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@UtilityClass
public class MapUtils {
  private static final Pattern MAP_PATTERN = Pattern.compile("maps/(.*)\\.zip");

  /**
   * Extracts and returns the folder name from the filename. E.g. {@code maps/something.zip} (as stored in the database)
   * is converted to {@code something}.
   */
  public String extractMapName(String mapPath) {
    Matcher matcher = MAP_PATTERN.matcher(mapPath);
    if (!matcher.matches()) {
      throw new IllegalArgumentException("Not a map path: " + mapPath);
    }
    return matcher.group(1);
  }
}
