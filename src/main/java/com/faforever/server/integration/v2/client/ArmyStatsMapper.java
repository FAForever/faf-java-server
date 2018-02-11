package com.faforever.server.integration.v2.client;

import com.faforever.server.stats.ArmyStatistics;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static com.github.nocatch.NoCatch.noCatch;

@Mapper(componentModel = "spring", uses = ObjectMapper.class)
abstract class ArmyStatsMapper {

  @Autowired
  ObjectMapper objectMapper;

  public List<ArmyStatistics> map(String string) {
    return noCatch(() -> {
      JsonNode node = objectMapper.readTree(string);
      JsonNode stats = node.get("stats");
      TypeReference<List<ArmyStatistics>> typeReference = new TypeReference<List<ArmyStatistics>>() {
      };
      JsonParser jsonParser = stats.traverse();
      jsonParser.setCodec(objectMapper);
      return jsonParser.readValueAs(typeReference);
    });
  }
}
