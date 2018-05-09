package com.faforever.server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

@EnableScheduling
@Configuration
public class SchedulerConfig implements SchedulingConfigurer {

  @Override
  public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
    taskRegistrar.setTaskScheduler(taskScheduler());
  }

  // Name is important in order to overwrite Spring's default taskScheduler
  @Bean
  public ThreadPoolTaskScheduler taskScheduler() {
    ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
    taskScheduler.setPoolSize(1);
    taskScheduler.initialize();
    return taskScheduler;
  }

  // Name is important in order to overwrite Spring's default taskExecutors
  @Bean
  public ThreadPoolTaskExecutor taskExecutor() {
    ThreadPoolTaskExecutor taskScheduler = new ThreadPoolTaskExecutor();
    taskScheduler.setCorePoolSize(0);
    taskScheduler.setMaxPoolSize(4);
    taskScheduler.initialize();
    return taskScheduler;
  }
}
