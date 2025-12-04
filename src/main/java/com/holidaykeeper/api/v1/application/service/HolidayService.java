package com.holidaykeeper.api.v1.application.service;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.holidaykeeper.api.v1.Infrastructure.respoitory.holiday.HolidayRepository;
import com.holidaykeeper.api.v1.Presentation.response.HolidayResponse;
import com.holidaykeeper.api.v1.common.Pagination;
import com.holidaykeeper.api.v1.domain.Holiday;
import com.holidaykeeper.api.v1.domain.HolidayType;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

  private final ObjectMapper objectMapper;
  private final HolidayRepository holidayRepository;

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
            toHolidayType(holiday.getTypesJson())
        ))
        .toList();

    return Pagination.of(
        holidays.getNumber(),
        holidays.getSize(),
        holidays.getTotalElements(),
        holidayRes
    );
  }

  private List<HolidayType> toHolidayType(String typesJson) {
    if (typesJson == null || typesJson.isEmpty()) {
      return List.of();
    }
    try {
      objectMapper.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS);
      return objectMapper.readValue(
          typesJson,
          objectMapper.getTypeFactory().constructCollectionType(List.class, HolidayType.class)
      );
    } catch (Exception e) {
      // todo: 예외 처리
      e.printStackTrace();
      return List.of();
    }
  }
}
