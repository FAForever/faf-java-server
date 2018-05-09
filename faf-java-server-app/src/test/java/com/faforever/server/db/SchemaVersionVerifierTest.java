package com.faforever.server.db;

import com.faforever.server.config.ServerProperties;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SchemaVersionVerifierTest {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Mock
  private SchemaVersionRepository schemaVersionRepository;
  private SchemaVersionVerifier instance;

  @Before
  public void setUp() throws Exception {
    ServerProperties properties = new ServerProperties();
    properties.getDatabase().setSchemaVersion("1337");

    instance = new SchemaVersionVerifier(schemaVersionRepository, properties);
  }

  @Test
  public void versionMatch() throws Exception {
    when(schemaVersionRepository.findMaxVersion()).thenReturn(Optional.of("1337"));

    instance.postConstruct();

    verify(schemaVersionRepository).findMaxVersion();
  }

  @Test
  public void versionMismatch() throws Exception {
    when(schemaVersionRepository.findMaxVersion()).thenReturn(Optional.of("7331"));
    expectedException.expect(IllegalStateException.class);
    expectedException.expectMessage("Database version is '7331' but this software requires '1337'");

    instance.postConstruct();
  }
}
