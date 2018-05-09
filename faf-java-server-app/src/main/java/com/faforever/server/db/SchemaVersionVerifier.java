package com.faforever.server.db;

import com.faforever.server.config.ServerProperties;
import org.springframework.core.PriorityOrdered;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import java.util.Objects;

@Component
public class SchemaVersionVerifier implements PriorityOrdered {

  private final SchemaVersionRepository schemaVersionRepository;
  private final ServerProperties properties;

  public SchemaVersionVerifier(SchemaVersionRepository schemaVersionRepository, ServerProperties properties) {
    this.schemaVersionRepository = schemaVersionRepository;
    this.properties = properties;
  }

  @PostConstruct
  public void postConstruct() {
    String requiredVersion = properties.getDatabase().getSchemaVersion();
    String actualVersion = schemaVersionRepository.findMaxVersion()
      .orElseThrow(() -> new IllegalStateException("No database version is available"));

    Assert.state(Objects.equals(requiredVersion, actualVersion),
      String.format("Database version is '%s' but this software requires '%s'", actualVersion, requiredVersion));
  }

  @Override
  public int getOrder() {
    return HIGHEST_PRECEDENCE;
  }
}
