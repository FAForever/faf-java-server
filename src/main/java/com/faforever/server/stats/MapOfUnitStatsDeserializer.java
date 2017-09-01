package com.faforever.server.stats;

import com.faforever.server.stats.ArmyStatistics.UnitStats;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.util.Map;

import static java.util.Collections.emptyMap;

/**
 * Since the LUA plugin used in FA has no chance knowing whether to serialize into an empty array {@code []} or table
 * (@copy {}), and chooses the empty array, this deserializer transforms such empty arrays into empty maps.
 */
public class MapOfUnitStatsDeserializer extends JsonDeserializer<Map<String, UnitStats>> {
  @Override
  public Map<String, UnitStats> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
    if (p.currentToken() == JsonToken.START_ARRAY) {
      p.skipChildren();
      return emptyMap();
    }
    return p.readValueAs(new TypeReference<Map<String, UnitStats>>() {
    });
  }
}
