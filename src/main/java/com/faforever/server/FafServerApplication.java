package com.faforever.server;

import com.faforever.server.config.ServerProperties;
import io.prometheus.client.spring.boot.EnablePrometheusEndpoint;
import io.prometheus.client.spring.boot.EnableSpringBootMetricsCollector;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
@EnablePrometheusEndpoint
@EnableSpringBootMetricsCollector
@EnableConfigurationProperties({ServerProperties.class})
public class FafServerApplication {

  public static void main(String[] args) {
    ConfigurableApplicationContext context = new SpringApplicationBuilder(FafServerApplication.class)
      .registerShutdownHook(false)
      .run(args);

    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      context.publishEvent(new ApplicationShutdownEvent());
      context.close();
    }));
  }

  public static class ApplicationShutdownEvent {
  }
}
