package com.faforever.server;

import com.faforever.server.entity.MapFeatures;
import com.faforever.server.map.MapFeaturesRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("dev")
public class FafServerApplicationTests {
  @Autowired
  private MapFeaturesRepository mapFeaturesRepository;

  @Test
  public void contextLoads() {
    // Spring Boot test
  }

  @Test
  public void mappingOfMapFeaturesWorks(){
    MapFeatures features = new MapFeatures()
      .setId(1)
      .setTimesPlayed(3)
      .setRating(0.5)
      .setDownloads(44)
      .setDraws(2)
      .setVoters("What is this?");

    mapFeaturesRepository.save(features);
    MapFeatures featuresFromRepo = mapFeaturesRepository.findOne(1);

    assertThat(featuresFromRepo.getId(), is(1));
    assertThat(featuresFromRepo.getTimesPlayed(), is(3));
    assertThat(featuresFromRepo.getRating(), is(0.5));
    assertThat(featuresFromRepo.getDownloads(), is(44));
    assertThat(featuresFromRepo.getDraws(), is(2));
    assertThat(featuresFromRepo.getVoters(), is("What is this?"));
  }
}
