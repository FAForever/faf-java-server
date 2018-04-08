package com.faforever.server;

import com.faforever.server.config.ServerProperties;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
@EnableConfigurationProperties({ServerProperties.class})
public class FafServerApplication {

  public static void main(String[] args) {
    ConfigurableApplicationContext context = new SpringApplicationBuilder(FafServerApplication.class)
      .registerShutdownHook(false)
      .run(args);

    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      context.publishEvent(ApplicationShutdownEvent.INSTANCE);
      context.close();
    }));
  }

  public enum ApplicationShutdownEvent {
    INSTANCE
  }
}
