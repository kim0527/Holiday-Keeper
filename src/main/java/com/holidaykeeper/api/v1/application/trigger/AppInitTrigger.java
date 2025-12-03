package com.holidaykeeper.api.v1.application.trigger;

import com.holidaykeeper.api.v1.application.service.AppInitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AppInitTrigger implements ApplicationListener<ApplicationReadyEvent> {

  private final AppInitService appInitService;

  @Override
  public void onApplicationEvent(ApplicationReadyEvent event) {
    appInitService.saveRecentHolidays();
  }
}
