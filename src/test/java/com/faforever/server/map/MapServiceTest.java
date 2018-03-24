package com.faforever.server.map;

import com.faforever.server.config.ServerProperties;
import com.faforever.server.entity.FeaturedMod;
import com.faforever.server.entity.Map;
import com.faforever.server.entity.MapStats;
import com.faforever.server.entity.MapVersion;
import com.faforever.server.entity.Player;
import com.faforever.server.mod.ModService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.data.domain.Page;

import java.util.Arrays;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.isOneOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
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

  private static final int LADDER_1V1_MOD_Id = 1;

  private MapVersion mapVersion;
  @Mock
  private ModService modService;

  @Before
  public void setUp() {
    mapVersion = new MapVersion().setId(1).setFilename(MAP_NAME);
    when(mapVersionRepository.findByFilenameIgnoreCase(MAP_NAME)).thenReturn(Optional.of(mapVersion));

    FeaturedMod ladder = new FeaturedMod();
    ladder.setId(LADDER_1V1_MOD_Id);
    when(modService.getLadder1v1()).thenReturn(Optional.of(ladder));

    MapStats features = new MapStats().setId(1).setTimesPlayed(41);
    when(mapStatsRepository.findById(1)).thenReturn(Optional.ofNullable(features));
    when(mapStatsRepository.save(any(MapStats.class))).thenAnswer(context -> context.getArguments()[0]);

    ServerProperties serverProperties = new ServerProperties();
    instance = new MapService(mapVersionRepository, ladder1v1MapRepository, mapStatsRepository, serverProperties, modService);
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
  public void getRandomLadderMapForSmallLadderMapPool() {
    MapVersion oneMap = new MapVersion().setId(2).setRanked(true);
    MapVersion otherMap = new MapVersion().setId(3).setRanked(true);
    when(ladder1v1MapRepository.findAll()).thenReturn(Arrays.asList(oneMap, otherMap));
    Page pagePlayerOne = mock(Page.class);
    when(pagePlayerOne.getContent()).thenReturn(Arrays.asList(oneMap, otherMap));
    Page pagePlayerTwo = mock(Page.class);
    when(pagePlayerTwo.getContent()).thenReturn(Arrays.asList(oneMap, otherMap));
    doReturn(pagePlayerOne).when(ladder1v1MapRepository).findRecentlyPlayedLadderMapVersions(any(), eq(1), eq(LADDER_1V1_MOD_Id));
    doReturn(pagePlayerTwo).when(ladder1v1MapRepository).findRecentlyPlayedLadderMapVersions(any(), eq(2), eq(LADDER_1V1_MOD_Id));

    Player host = new Player();
    host.setId(1);
    Player opponent = new Player();
    opponent.setId(2);

    assertThat(instance.getRandomLadderMap(host, opponent), isOneOf(oneMap, otherMap));
  }

  @Test
  public void getRandomLadderMapForBiggerLadderMapPool() {
    MapVersion oneMap = new MapVersion().setId(2).setRanked(true);
    MapVersion otherMap = new MapVersion().setId(3).setRanked(true);
    MapVersion firstPlayerFirstPlayedMap = new MapVersion().setId(4).setRanked(true);
    MapVersion firstPlayerSecondPlayedMap = new MapVersion().setId(5).setRanked(true);
    MapVersion secondPlayerFirstPlayedMap = new MapVersion().setId(6).setRanked(true);
    MapVersion secondPlayerSecondPlayedMap = new MapVersion().setId(7).setRanked(true);

    when(ladder1v1MapRepository.findAll()).thenReturn(Arrays.asList(oneMap, otherMap, firstPlayerFirstPlayedMap, firstPlayerSecondPlayedMap, secondPlayerFirstPlayedMap, secondPlayerSecondPlayedMap));

    Page pagePlayerOne = mock(Page.class);
    when(pagePlayerOne.getContent()).thenReturn(Arrays.asList(firstPlayerFirstPlayedMap, firstPlayerSecondPlayedMap));
    Page pagePlayerTwo = mock(Page.class);
    when(pagePlayerTwo.getContent()).thenReturn(Arrays.asList(secondPlayerFirstPlayedMap, secondPlayerSecondPlayedMap));
    doReturn(pagePlayerOne).when(ladder1v1MapRepository).findRecentlyPlayedLadderMapVersions(any(), eq(1), eq(LADDER_1V1_MOD_Id));
    doReturn(pagePlayerTwo).when(ladder1v1MapRepository).findRecentlyPlayedLadderMapVersions(any(), eq(2), eq(LADDER_1V1_MOD_Id));

    Player host = new Player();
    host.setId(1);
    Player opponent = new Player();
    opponent.setId(2);

    assertThat(instance.getRandomLadderMap(host, opponent), isOneOf(oneMap, otherMap));
  }
}
