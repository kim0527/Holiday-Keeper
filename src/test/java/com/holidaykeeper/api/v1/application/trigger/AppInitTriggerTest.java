package com.holidaykeeper.api.v1.application.trigger;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

import com.holidaykeeper.api.v1.application.service.AppInitService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@DisplayName("AppInitTrigger 테스트")
public class AppInitTriggerTest {

  @MockitoBean
  AppInitService appInitService;

  @Autowired
  ApplicationEventPublisher publisher;

  @Test
  @DisplayName("최초 실행시, 특정 범위의 모든 국가의 공휴일을 저장하는 saveRecentHolidays()가 호출되는지 확인하는 테스트")
  void success_trigger() {
    // when
    publisher.publishEvent(new ApplicationReadyEvent(new SpringApplication(), null, null, null));

    // then
    verify(appInitService, atLeastOnce()).saveRecentHolidays();
  }

}
