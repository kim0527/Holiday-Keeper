package com.holidaykeeper.api.v1.Presentation.response;

import com.holidaykeeper.api.v1.domain.HolidayType;
import java.time.LocalDate;
import java.util.List;

public record HolidayResponse(
    String countryCode,
    String countryName,
    LocalDate date,
    String localName,
    String name,
    Integer launchYear,
    List<HolidayType> type
) {
}
