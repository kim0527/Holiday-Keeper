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

    log.info("toInsert : {}개", toInsert.size());
    log.info("toUpdate : {}개", toUpdate.size());
    log.info("toDelete : {}개", toDelete.size());

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

  private Map<HolidayKey, GetHolidayResponse> fetchHolidaysAsMap(String countryCode, int year) {
    return getHolidaysWithRetry(year, countryCode)
        .stream()
        .collect(Collectors.toMap(
            holiday -> new HolidayKey(holiday.date(), holiday.name()),
            Function.identity()
        ));
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

  private Map<HolidayKey, Holiday> getPersistHolidaysAsMap(String countryCode, int year) {
    return holidayRepository.findByCountryCodeAndYear(countryCode, year)
        .stream()
        .collect(Collectors.toMap(
            holiday -> new HolidayKey(holiday.getDate(), holiday.getName()),
            Function.identity()
        ));
  }

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

  private boolean isHolidayInPersist(Holiday holiday) {
    return holiday != null;
  }

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

  @Override
  @Transactional
  public void deleteHolidays(String countryCode, int year) {
    List<Holiday> holidays = holidayRepository.findByCountryCodeAndYear(countryCode, year);
    holidayRepository.bulkDelete(holidays);
  }
}
