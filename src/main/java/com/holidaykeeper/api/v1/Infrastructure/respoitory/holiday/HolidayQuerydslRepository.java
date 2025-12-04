package com.holidaykeeper.api.v1.Infrastructure.respoitory.holiday;

import com.holidaykeeper.api.v1.domain.Holiday;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface HolidayQuerydslRepository {
  Page<Holiday> searchHoliday(
      Optional<Integer> year,
      Optional<String> countryCode,
      Optional<String> holidayType,
      Pageable pageable
  );
}
