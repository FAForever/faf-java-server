package com.faforever.server;

import com.faforever.server.map.MapFeaturesRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

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
}
