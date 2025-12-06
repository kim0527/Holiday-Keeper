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
