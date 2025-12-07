package com.holidaykeeper.api.v1.application.service;

import com.holidaykeeper.api.v1.Infrastructure.external.client.ApiClient;
import com.holidaykeeper.api.v1.Infrastructure.external.client.response.GetCountryResponse;
import com.holidaykeeper.api.v1.Infrastructure.external.client.response.GetHolidayResponse;
import com.holidaykeeper.api.v1.Infrastructure.respoitory.country.CountryRepository;
import com.holidaykeeper.api.v1.Infrastructure.respoitory.holiday.HolidayRepository;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 애플리케이션 최초 실행시 동작하는 서비스
 *
 * <p>애플리케이션 시작 시 필요한 초기 데이터를 외부 API로부터 조회하여
 * 데이터베이스에 저장하는 기능을 제공합니다.
 *
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AppInitService {

  private final CountryRepository countryRepository;
  private final HolidayRepository holidayRepository;
  private final ApiClient apiClient;

  private static final int FROM_YEAR = 2020;
  private static final int TO_YEAR = 2025;

  @Value("${app.batch.size}")
  private int batchSize;

  @Value("${app.api.retry_count}")
  private int retryCount;

  /**
   * 최근 연도의 모든 국가 공휴일 데이터를 조회하여 저장합니다.
   *
   * <p>다음 작업을 순차적으로 수행합니다:
   * <ol>
   *   <li>외부 API에서 지원하는 모든 국가 정보를 조회하여 저장</li>
   *   <li>각 국가별로 2020년부터 2025년까지의 공휴일 데이터를 병렬로 조회</li>
   *   <li>조회된 공휴일 데이터를 배치 단위로 데이터베이스에 저장</li>
   * </ol>
   *
   * <p>공휴일 조회는 멀티스레드 환경에서 병렬로 처리되어 성능을 최적화합니다.
   * 각 API 호출은 설정된 재시도 횟수만큼 재시도됩니다.
   *
   * @throws RuntimeException API 호출이 모든 재시도 후에도 실패한 경우
   * @since 1.0
   */
  @Transactional
  public void saveRecentHolidays() {
    long start = System.currentTimeMillis();
    // 1. 국가 조회 후 저장
    List<GetCountryResponse> countries = getCountriesWithRetry();
    countryRepository.bulkInsert(countries);
    // 2. 공휴일 조회 후 저장
    List<GetHolidayResponse> totalHolidays = getHolidays(countries);
    batchInsertHolidays(totalHolidays);
    log.info("{}년부터 {}년까지 모든 국가의 공휴일 정보 적재 완료 (소요시간 : {}ms)", FROM_YEAR, TO_YEAR, System.currentTimeMillis() - start);
  }

  /**
   * 여러 국가의 지정된 연도 범위 공휴일 데이터를 병렬로 조회합니다.
   *
   * <p>각 국가와 연도 조합에 대해 별도의 비동기 작업을 생성하고,
   * 시스템의 CPU 코어 수만큼 스레드 풀을 구성하여 병렬 처리합니다.
   * 모든 비동기 작업이 완료될 때까지 대기한 후 결과를 통합하여 반환합니다.
   *
   * @param countries 공휴일을 조회할 국가 목록
   * @return 모든 국가와 연도에 대한 공휴일 데이터 리스트
   */
  private List<GetHolidayResponse> getHolidays(List<GetCountryResponse> countries) {
    ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    try {
      List<CompletableFuture<List<GetHolidayResponse>>> futures = countries.stream()
          .flatMap(country -> IntStream.rangeClosed(FROM_YEAR, TO_YEAR)
              .mapToObj(year -> CompletableFuture.supplyAsync(
                  () -> getHolidaysWithRetry(year, country.countryCode()),
                  executor
              ))
          )
          .toList();

      return futures.stream()
          .map(CompletableFuture::join)
          .flatMap(List::stream)
          .toList();
    } finally {
      executor.shutdown();
    }
  }

  /**
   * 재시도 로직을 포함하여 외부 API에서 국가 정보를 조회합니다.
   *
   * <p>설정된 재시도 횟수만큼 API 호출을 시도하며,
   * 모든 시도가 실패하면 예외를 발생시킵니다.
   *
   * @return 국가 정보 리스트
   * @throws RuntimeException 모든 재시도가 실패한 경우
   */
  private List<GetCountryResponse> getCountriesWithRetry() {
    for (int attempt = 1; attempt <= retryCount; attempt++) {
      try {
        return apiClient.getCountries();
      } catch (Exception e) {
        if (attempt == retryCount) {
          // todo : 예외 정의하기
          throw new RuntimeException();
        }
      }
    }
    return Collections.emptyList();
  }

  /**
   * 재시도 로직을 포함하여 외부 API에서 특정 연도와 국가의 공휴일 데이터를 조회합니다.
   *
   * <p>설정된 재시도 횟수만큼 API 호출을 시도하며,
   * 모든 시도가 실패하면 예외를 발생시킵니다.
   *
   * @param year 조회할 연도
   * @param countryCode 조회할 국가 코드
   * @return 공휴일 데이터 리스트
   * @throws RuntimeException 모든 재시도가 실패한 경우
   */
  private List<GetHolidayResponse> getHolidaysWithRetry(int year, String countryCode) {
    for (int attempt = 1; attempt <= retryCount; attempt++) {
      try {
        return apiClient.getHolidays(year, countryCode);
      } catch (Exception e) {
        if (attempt == retryCount) {
          // todo : 예외 정의하기
          throw new RuntimeException();
        }
      }
    }
    return Collections.emptyList();
  }

  /**
   * 공휴일 데이터를 배치 단위로 나누어 데이터베이스에 삽입합니다.
   *
   * <p>대량의 데이터를 한 번에 삽입하는 것을 방지하기 위해,
   * 설정된 배치 크기만큼 데이터를 분할하여 순차적으로 삽입합니다.
   *
   * @param holidays 삽입할 공휴일 데이터 리스트
   */
  private void batchInsertHolidays(List<GetHolidayResponse> holidays) {
    if (holidays.isEmpty()) {
      return;
    }
    for (int i = 0; i < holidays.size(); i += batchSize) {
      int endIndex = Math.min(i + batchSize, holidays.size());
      List<GetHolidayResponse> batch = holidays.subList(i, endIndex);
      holidayRepository.bulkInsert(batch);
    }
  }
}
