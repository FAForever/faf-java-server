package com.faforever.server.legacyadapter;

import com.faforever.server.response.ServerResponse;
import org.springframework.integration.transformer.GenericTransformer;

import java.util.Map;

public class LegacyResponseTransformer implements GenericTransformer<ServerResponse, Map<String, Object>> {

  @Override
  @SuppressWarnings("unchecked")
  public Map<String, Object> transform(ServerResponse source) {
    if (source instanceof Map) {
      return (Map<String, Object>) source;
    }
    if(source instanceof )
    return null;
  }
}
