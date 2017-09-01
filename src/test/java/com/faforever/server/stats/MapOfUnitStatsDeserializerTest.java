package com.faforever.server.stats;

import com.faforever.server.stats.ArmyStatistics.UnitStats;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MapOfUnitStatsDeserializerTest {
  private MapOfUnitStatsDeserializer instance;

  @Mock
  private JsonParser parser;
  @Mock
  private com.fasterxml.jackson.databind.DeserializationContext context;

  @Before
  public void setUp() throws Exception {
    instance = new MapOfUnitStatsDeserializer();
  }

  @Test
  public void deserializeObjectCallsParser() throws Exception {
    when(parser.currentToken()).thenReturn(JsonToken.START_OBJECT);
    when(parser.readValueAs(any(TypeReference.class))).thenReturn(Collections.emptyMap());

    Map<String, UnitStats> deserialize = instance.deserialize(parser, context);

    verify(parser).readValueAs(any(TypeReference.class));

    assertThat(deserialize, is(Collections.emptyMap()));
  }

  @Test
  public void deserializeEmptyArrayResultsInEmptyMap() throws Exception {
    when(parser.currentToken()).thenReturn(JsonToken.START_ARRAY);

    Map<String, UnitStats> deserialize = instance.deserialize(parser, context);

    assertThat(deserialize, is(Collections.emptyMap()));
  }
}
