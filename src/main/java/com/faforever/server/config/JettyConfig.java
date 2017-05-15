package com.faforever.server.config;

import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.springframework.boot.context.embedded.jetty.JettyEmbeddedServletContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JettyConfig {

  private final org.springframework.boot.autoconfigure.web.ServerProperties springServerProperties;
  private final ServerProperties fafServerProperties;

  public JettyConfig(org.springframework.boot.autoconfigure.web.ServerProperties springServerProperties,
                     ServerProperties fafServerProperties) {
    this.springServerProperties = springServerProperties;
    this.fafServerProperties = fafServerProperties;
  }

  @Bean
  public JettyEmbeddedServletContainerFactory jettyServletWebServerFactory() {
    final JettyEmbeddedServletContainerFactory factory = new JettyEmbeddedServletContainerFactory(springServerProperties.getPort());
    factory.addServerCustomizers(server -> {
      final QueuedThreadPool threadPool = server.getBean(QueuedThreadPool.class);
      threadPool.setMinThreads(fafServerProperties.getJetty().getMinThreads());
      threadPool.setMaxThreads(fafServerProperties.getJetty().getMaxThreads());
      threadPool.setIdleTimeout(fafServerProperties.getJetty().getIdleTimeoutMillis());
    });
    return factory;
  }
}
