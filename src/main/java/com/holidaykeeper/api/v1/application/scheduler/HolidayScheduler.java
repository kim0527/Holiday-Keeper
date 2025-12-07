package com.holidaykeeper.api.v1.application.scheduler;

import com.holidaykeeper.api.v1.Infrastructure.external.client.ApiClient;
import com.holidaykeeper.api.v1.Infrastructure.external.client.response.GetCountryResponse;
import com.holidaykeeper.api.v1.application.service.HolidayUsecase;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 공휴일 데이터 자동 동기화 스케줄러
 *
 * <p>매년 1월 2일 01:00 KST에 전년도와 금년도의
 * 모든 국가 공휴일 데이터를 자동으로 동기화합니다.
 *
 * <p><strong>병렬 처리:</strong>
 * 각 국가별 동기화 작업을 비동기로 처리하여 성능을 최적화합니다.
 * 스레드 풀 크기만큼 동시에 여러 국가를 처리할 수 있습니다.
 *
 * @since 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HolidayScheduler {

  private final HolidayUsecase holidayUsecase;
  private final ApiClient apiClient;

  /**
   * 매년 1월 2일 01:00 KST에 전년도와 금년도 공휴일 데이터를 자동 동기화합니다.
   *
   * <p><strong>실행 시점:</strong> 매년 1월 2일 01:00 (한국 시간)
   *
   * <p><strong>처리 대상:</strong>
   * <ul>
   *   <li>전년도 데이터: 최종 업데이트 확인</li>
   *   <li>금년도 데이터: 신규 공휴일 정보 수집</li>
   * </ul>
   *
   * <p><strong>처리 방식:</strong>
   * 각 국가의 전년도와 금년도 동기화를 비동기로 병렬 처리하여,
   * 전체 작업 시간을 대폭 단축합니다.
   */
  @Scheduled(cron = "0 0 1 2 1 ?", zone = "Asia/Seoul")
  public void autoRefreshHolidays() {
    long startTime = System.currentTimeMillis();
    try {
      LocalDate now = LocalDate.now();
      int currentYear = now.getYear();
      int previousYear = currentYear - 1;

      List<GetCountryResponse> countries = apiClient.getCountries();

      AtomicInteger successCount = new AtomicInteger(0);
      AtomicInteger failureCount = new AtomicInteger(0);

      List<CompletableFuture<Void>> futures = countries.stream()
          .flatMap(country -> List.of(
              asyncRefreshHolidays(country.countryCode(), previousYear, successCount, failureCount),
              asyncRefreshHolidays(country.countryCode(), currentYear, successCount, failureCount)
          ).stream())
          .toList();

      CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

      long elapsedTime = System.currentTimeMillis() - startTime;
      log.info("[스케줄러] 연간 공휴일 자동 동기화 완료 (성공: {}건, 실패: {}건, 소요시간: {}ms)", successCount.get(), failureCount.get(), elapsedTime);
    } catch (Exception e) {
      log.error("[스케줄러] 연간 공휴일 자동 동기화 중 오류 발생", e);
    }
  }

  /**
   * 특정 국가와 연도의 공휴일 데이터를 비동기로 동기화합니다.
   *
   * @param countryCode 국가 코드
   * @param year 대상 연도
   * @param successCount 성공 카운터
   * @param failureCount 실패 카운터
   * @return 비동기 작업 결과를 담은 CompletableFuture
   */
  @Async("holidayExecutor")
  public CompletableFuture<Void> asyncRefreshHolidays(
      String countryCode,
      int year,
      AtomicInteger successCount,
      AtomicInteger failureCount
  ) {
    return CompletableFuture.runAsync(() -> {
      try {
        holidayUsecase.refreshHolidays(countryCode, year);
        successCount.incrementAndGet();
      } catch (Exception e) {
        failureCount.incrementAndGet();
      }
    });
  }
}