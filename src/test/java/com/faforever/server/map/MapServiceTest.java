package com.faforever.server.map;

import com.faforever.server.entity.MapFeatures;
import com.faforever.server.entity.MapVersion;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MapServiceTest {
  private static final String MAP_NAME = "SCMP_001";

  private MapService instance;

  @Mock
  private MapVersionRepository mapVersionRepository;

  @Mock
  private Ladder1v1MapRepository ladder1v1MapRepository;

  @Before
  public void setUp(){
    MapVersion map = new MapVersion();
    map.setFilename(MAP_NAME);
    map.setFeatures(new MapFeatures());
    when(mapVersionRepository.findByFilenameIgnoreCase(MAP_NAME)).thenReturn(Optional.of(map));

    instance = new MapService(mapVersionRepository, ladder1v1MapRepository);
  }

  @Test
  public void testIncreasePlayCount(){
    MapVersion foundMap = instance.findMap(MAP_NAME).get();

    assertThat(foundMap.getFeatures().getTimesPlayed(), is(0));

    instance.increaseTimesPlayed(MAP_NAME);

    assertThat(foundMap.getFeatures().getTimesPlayed(), is(1));
    // once for the findMap above, once when incrementing the times played
    verify(mapVersionRepository, times(2)).findByFilenameIgnoreCase(MAP_NAME);
    verify(mapVersionRepository).save(foundMap);
  }
}
