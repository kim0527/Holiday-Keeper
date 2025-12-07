package com.holidaykeeper.api.v1.application.service;

import com.holidaykeeper.api.v1.Infrastructure.external.client.ApiClient;
import com.holidaykeeper.api.v1.Infrastructure.external.client.response.GetHolidayResponse;
import com.holidaykeeper.api.v1.Infrastructure.respoitory.holiday.HolidayRepository;
import com.holidaykeeper.api.v1.Presentation.response.HolidayResponse;
import com.holidaykeeper.api.v1.application.util.JsonUtil;
import com.holidaykeeper.api.v1.common.dto.Pagination;
import com.holidaykeeper.api.v1.domain.Holiday;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 공휴일(Holiday)와 관련된 서비스 로직
 *
 * <p> HolidayUsecase의 구현체로, 조회, 재동기화, 삭제 기능을 제공합니다.
 *
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HolidayService implements HolidayUsecase {

  private final HolidayRepository holidayRepository;
  private final ApiClient apiClient;

  @Value("${app.api.retry_count}")
  private int retryCount;

  @Value("${app.batch.size}")
  private int batchSize;

  /**
   * 여러 필터링 조건을 기반으로 공휴일 데이터를 조회합니다.
   *
   * <p>연도, 국가 코드, 공휴일 타입 등의 조건으로 필터링하며,
   * 정렬 기준과 페이징 옵션을 지원합니다.
   *
   * @param year 조회할 연도 (Optional)
   * @param countryCode 국가 코드 (Optional, 예: "KR", "US")
   * @param holidayType 공휴일 타입 (Optional)
   * @param sortType 정렬 기준 필드 (Optional, 기본값: "date")
   * @param sortOrder 정렬 순서 (Optional, "ASC" 또는 "DESC", 기본값: "DESC")
   * @param page 페이지 번호 (Optional, 0부터 시작, 기본값: 0)
   * @param size 페이지 크기 (Optional, 기본값: 10)
   * @return 페이징 처리된 공휴일 응답 데이터
   * @since 1.0
   */
  @Override
  public Pagination<HolidayResponse> searchHoliday(
      Optional<Integer> year,
      Optional<String> countryCode,
      Optional<String> holidayType,
      Optional<String> sortType,
      Optional<String> sortOrder,
      Optional<Integer> page,
      Optional<Integer> size
  ) {
    Pageable pageable = PageRequest.of(
        page.orElse(0),
        size.orElse(10),
        sortOrder.map(dir -> Sort.Direction.fromString(dir))
            .orElse(Direction.DESC),
        sortType.orElse("date")
    );

    Page<Holiday> holidays = holidayRepository.searchHoliday(
        year,
        countryCode,
        holidayType,
        pageable
    );

    List<HolidayResponse> holidayRes = holidays.getContent().stream()
        .map(holiday -> new HolidayResponse(
            holiday.getCountry().getCode(),
            holiday.getCountry().getName(),
            holiday.getDate(),
            holiday.getLocalName(),
            holiday.getName(),
            holiday.getLaunchYear(),
            JsonUtil.toHolidayType(holiday.getTypesJson())
        ))
        .toList();

    return Pagination.of(
        holidays.getNumber(),
        holidays.getSize(),
        holidays.getTotalElements(),
        holidayRes
    );
  }

  /**
   * 외부 API로부터 공휴일 데이터를 가져와 데이터베이스와 동기화합니다.
   *
   * <p>API에서 조회한 데이터와 기존 데이터를 비교하여:
   * <ul>
   *   <li>신규 데이터는 삽입(INSERT)</li>
   *   <li>변경된 데이터는 업데이트(UPDATE)</li>
   *   <li>API에 없는 기존 데이터는 삭제(DELETE)</li>
   * </ul>
   *
   * @param countryCode 동기화할 국가 코드 (예: "KR")
   * @param year 동기화할 연도
   * @throws RuntimeException API 호출이 재시도 횟수를 초과하여 실패한 경우
   * @since 1.0
   */
  @Override
  @Transactional
  public void refreshHolidays(String countryCode, int year) {
    Map<HolidayKey, GetHolidayResponse> apiHolidays = fetchHolidaysAsMap(countryCode, year);
    Map<HolidayKey, Holiday> persistHolidays = getPersistHolidaysAsMap(countryCode, year);

    List<GetHolidayResponse> toInsert = new ArrayList<>();
    Map<Holiday,GetHolidayResponse> toUpdate = new HashMap<>();
    List<Holiday> toDelete = new ArrayList<>();

    categorizeHolidaysToInsertAndUpdate(apiHolidays, persistHolidays, toInsert, toUpdate);
    findHolidaysToDelete(apiHolidays, persistHolidays, toDelete);

    if (!toInsert.isEmpty()) {
      holidayRepository.bulkInsert(toInsert);
    }

    if (!toUpdate.isEmpty()) {
      holidayRepository.bulkUpdate(toUpdate);
    }

    if (!toDelete.isEmpty()) {
      holidayRepository.bulkDelete(toDelete);
    }
  }

  /**
   * 외부 API에서 공휴일 데이터를 조회하여 Map으로 변환합니다.
   *
   * @param countryCode 조회할 국가 코드
   * @param year 조회할 연도
   * @return 공휴일 키(날짜+이름)를 기준으로 한 공휴일 응답 데이터 맵
   */
  private Map<HolidayKey, GetHolidayResponse> fetchHolidaysAsMap(String countryCode, int year) {
    return getHolidaysWithRetry(year, countryCode)
        .stream()
        .collect(Collectors.toMap(
            holiday -> new HolidayKey(holiday.date(), holiday.name()),
            Function.identity()
        ));
  }

  /**
   * 재시도 로직을 포함하여 외부 API에서 공휴일 데이터를 조회합니다.
   *
   * <p>설정된 재시도 횟수만큼 API 호출을 시도하며, 모두 실패 시 예외를 발생시킵니다.
   *
   * @param year 조회할 연도
   * @param countryCode 조회할 국가 코드
   * @return 공휴일 응답 데이터 리스트
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
   * 데이터베이스에서 특정 국가와 연도의 공휴일 데이터를 조회하여 Map으로 변환합니다.
   *
   * @param countryCode 조회할 국가 코드
   * @param year 조회할 연도
   * @return 공휴일 키(날짜+이름)를 기준으로 한 공휴일 엔티티 맵
   */
  private Map<HolidayKey, Holiday> getPersistHolidaysAsMap(String countryCode, int year) {
    return holidayRepository.findByCountryCodeAndYear(countryCode, year)
        .stream()
        .collect(Collectors.toMap(
            holiday -> new HolidayKey(holiday.getDate(), holiday.getName()),
            Function.identity()
        ));
  }

  /**
   * API 데이터와 기존 데이터를 비교하여 삽입 및 업데이트 대상을 분류합니다.
   *
   * <p>API 데이터를 순회하면서:
   * <ul>
   *   <li>기존 데이터에 없으면 삽입 대상으로 분류</li>
   *   <li>기존 데이터가 있고 변경사항이 있으면 업데이트 대상으로 분류</li>
   * </ul>
   *
   * @param apiHolidays API에서 조회한 공휴일 데이터 맵
   * @param persistHolidays 데이터베이스에 저장된 공휴일 데이터 맵
   * @param toInsert 삽입할 공휴일 리스트 (출력 파라미터)
   * @param toUpdate 업데이트할 공휴일 맵 (출력 파라미터)
   */
  private void categorizeHolidaysToInsertAndUpdate(
      Map<HolidayKey, GetHolidayResponse> apiHolidays,
      Map<HolidayKey, Holiday> persistHolidays,
      List<GetHolidayResponse> toInsert,
      Map<Holiday,GetHolidayResponse> toUpdate
  ) {
    apiHolidays.forEach((key, apiHoliday) -> {
      Holiday holiday = persistHolidays.get(key);
      if (isHolidayInPersist(holiday)) {
        if (holiday.hasChanges(
            apiHoliday.date(),
            apiHoliday.localName(),
            apiHoliday.name(),
            apiHoliday.fixed(),
            apiHoliday.global(),
            apiHoliday.counties(),
            apiHoliday.launchYear(),
            apiHoliday.types())
        ) {
          toUpdate.put(holiday, apiHoliday);
        }
      } else {
        toInsert.add(apiHoliday);
      }
    });
  }

  /**
   * 공휴일이 데이터베이스에 존재하는지 확인합니다.
   *
   * @param holiday 확인할 공휴일 엔티티
   * @return 존재하면 true, 그렇지 않으면 false
   */
  private boolean isHolidayInPersist(Holiday holiday) {
    return holiday != null;
  }

  /**
   * API에 없는 기존 데이터를 삭제 대상으로 분류합니다.
   *
   * @param apiHolidays API에서 조회한 공휴일 데이터 맵
   * @param existingHolidays 데이터베이스에 저장된 공휴일 데이터 맵
   * @param toDelete 삭제할 공휴일 리스트 (출력 파라미터)
   */
  private void findHolidaysToDelete(
      Map<HolidayKey, GetHolidayResponse> apiHolidays,
      Map<HolidayKey, Holiday> existingHolidays,
      List<Holiday> toDelete
  ) {
    existingHolidays.forEach((key, holiday) -> {
      if (!apiHolidays.containsKey(key)) {
        toDelete.add(holiday);
      }
    });
  }

  /**
   * 공휴일의 고유 키를 나타내는 레코드 클래스
   *
   * <p>날짜(date)와 이름(name)의 조합으로 공휴일을 식별합니다.
   *
   * @param date 공휴일 날짜
   * @param name 공휴일 이름
   */
  private record HolidayKey(LocalDate date, String name) {
    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      HolidayKey that = (HolidayKey) o;
      return Objects.equals(date, that.date) && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
      return Objects.hash(date, name);
    }
  }

  /**
   * 특정 국가와 연도의 공휴일 데이터를 삭제합니다.
   *
   * @param countryCode 삭제할 국가 코드
   * @param year 삭제할 연도
   * @since 1.0
   */
  @Override
  @Transactional
  public void deleteHolidays(String countryCode, int year) {
    List<Holiday> holidays = holidayRepository.findByCountryCodeAndYear(countryCode, year);
    holidayRepository.bulkDelete(holidays);
  }
}
