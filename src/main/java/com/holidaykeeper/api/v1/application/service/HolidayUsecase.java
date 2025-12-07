package com.holidaykeeper.api.v1.application.service;

import com.holidaykeeper.api.v1.Presentation.response.HolidayResponse;
import com.holidaykeeper.api.v1.common.dto.Pagination;
import java.util.Optional;

public interface HolidayUsecase {
  Pagination<HolidayResponse> searchHoliday(
      Optional<Integer> year,
      Optional<String> countryCode,
      Optional<String> holidayType,
      Optional<String> sortType,
      Optional<String> sortOrder,
      Optional<Integer> page,
      Optional<Integer> size
  );
  void refreshHolidays(String countryCode, int year);
  void deleteHolidays(String countryCode, int year);
}
