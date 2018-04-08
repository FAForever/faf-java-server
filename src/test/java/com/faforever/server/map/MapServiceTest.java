package com.faforever.server.map;

import com.faforever.server.entity.Map;
import com.faforever.server.entity.MapStats;
import com.faforever.server.entity.MapVersion;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.isOneOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MapServiceTest {
  private static final String MAP_NAME = "SCMP_001";

  private MapService instance;

  @Mock
  private MapVersionRepository mapVersionRepository;

  @Mock
  private Ladder1v1MapRepository ladder1v1MapRepository;

  @Mock
  private MapStatsRepository mapStatsRepository;

  private MapVersion mapVersion;

  @Before
  public void setUp() {
    mapVersion = new MapVersion().setId(1).setFilename(MAP_NAME);
    when(mapVersionRepository.findByFilenameIgnoreCase(MAP_NAME)).thenReturn(Optional.of(mapVersion));

    MapStats features = new MapStats().setId(1).setTimesPlayed(41);
    when(mapStatsRepository.findById(1)).thenReturn(Optional.ofNullable(features));
    when(mapStatsRepository.save(any(MapStats.class))).thenAnswer(context -> context.getArguments()[0]);

    instance = new MapService(mapVersionRepository, ladder1v1MapRepository, mapStatsRepository);
  }

  @Test
  public void timesPlayedIsIncreasedCorrectly() {
    Map map = new Map().setId(1);

    MapStats features = instance.getMapStats(map);
    assertThat(features.getId(), is(1));
    assertThat(features.getTimesPlayed(), is(41));

    instance.incrementTimesPlayed(map);

    verify(mapStatsRepository).save(features);

    features = instance.getMapStats(map);
    assertThat(features.getId(), is(1));
    assertThat(features.getTimesPlayed(), is(42));

    verifyZeroInteractions(mapVersionRepository);
    verifyZeroInteractions(ladder1v1MapRepository);
  }

  @Test
  public void timesPlayedIsInitializedWithZero() {
    int newId = 1342342;

    Map map = new Map().setId(newId);
    MapStats features = instance.getMapStats(map);

    assertThat(features.getId(), is(newId));
    assertThat(features.getTimesPlayed(), is(0));

    verify(mapStatsRepository).save(features);

    verifyZeroInteractions(mapVersionRepository);
    verifyZeroInteractions(ladder1v1MapRepository);
  }

  @Test
  public void mapIsFoundByName() {
    assertThat(instance.findMap(MAP_NAME).get(), is(mapVersion));
  }

  @Test
  public void getRandomLadderMap() {
    MapVersion oneMap = new MapVersion().setId(2).setRanked(true);
    MapVersion otherMap = new MapVersion().setId(3).setRanked(true);
    when(ladder1v1MapRepository.findAll()).thenReturn(Arrays.asList(oneMap, otherMap));

    assertThat(instance.getRandomLadderMap(), isOneOf(oneMap, otherMap));
  }
}
