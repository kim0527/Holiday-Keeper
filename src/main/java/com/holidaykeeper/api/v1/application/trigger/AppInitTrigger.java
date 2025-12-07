package com.holidaykeeper.api.v1.application.trigger;

import com.holidaykeeper.api.v1.application.service.AppInitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * 애플리케이션 초기화 트리거
 *
 * <p>Spring Boot 애플리케이션이 완전히 시작된 후 자동으로 실행되는 이벤트 리스너입니다.
 * {@link ApplicationReadyEvent}를 감지하여 초기 데이터 로딩 작업을 수행합니다.
 *
 * <p><strong>실행 시점:</strong>
 * ApplicationReadyEvent는 애플리케이션 컨텍스트가 완전히 준비되고,
 * 모든 빈이 초기화되며, 애플리케이션이 요청을 받을 준비가 완료된 시점에 발생합니다.
 *
 * @see AppInitService
 * @see ApplicationReadyEvent
 * @since 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AppInitTrigger implements ApplicationListener<ApplicationReadyEvent> {

  private final AppInitService appInitService;

  /**
   * 애플리케이션이 완전히 준비된 후 초기 데이터 로딩을 수행합니다.
   *
   * <p>이 메서드는 Spring Boot 애플리케이션이 시작을 완료하고
   * {@link ApplicationReadyEvent}가 발생할 때 자동으로 호출됩니다.
   *
   * <p><strong>수행 작업:</strong>
   * <ul>
   *   <li>외부 API로부터 모든 국가 정보 조회 및 저장</li>
   *   <li>2020년부터 2025년까지의 모든 국가 공휴일 데이터 조회 및 저장</li>
   * </ul>
   *
   * <p><strong>주의사항:</strong>
   * 이 작업은 애플리케이션 시작 시 한 번만 실행되며,
   * 대량의 데이터를 처리하므로 완료까지 수십 초가 소요될 수 있습니다.
   * 작업이 완료되기 전까지는 관련 API 엔드포인트에서 데이터가 조회되지 않을 수 있습니다.
   *
   * @param event 애플리케이션 준비 완료 이벤트
   * @see AppInitService#saveRecentHolidays()
   * @since 1.0
   */
  @Override
  public void onApplicationEvent(ApplicationReadyEvent event) {
    appInitService.saveRecentHolidays();
  }
}
