package com.holidaykeeper.api.v1.common.config;

import java.util.concurrent.Executor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Slf4j
@EnableAsync
@Configuration
public class AsyncConfig implements AsyncConfigurer {

  @Bean(name = "holidayExecutor")
  public Executor holidayExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

    // 기본 스레드 수
    executor.setCorePoolSize(10);
    // 최대 스레드 수
    executor.setMaxPoolSize(10);
    // 큐 용량
    executor.setQueueCapacity(100);
    // 스레드 이름 접두사
    executor.setThreadNamePrefix("holiday-async-");
    // 애플리케이션 종료 시 모든 작업이 완료될 때까지 대기
    executor.setWaitForTasksToCompleteOnShutdown(true);
    // 종료 대기 시간 (초)
    executor.setAwaitTerminationSeconds(10);

    executor.initialize();

    return executor;
  }
}