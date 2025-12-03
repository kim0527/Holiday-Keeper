package com.holidaykeeper.api.v1.Infrastructure.external.client.response;

import java.time.LocalDate;
import java.util.List;

public record GetHolidayResponse(
    LocalDate date,
    String localName,
    String name,
    String countryCode,
    Boolean fixed,
    Boolean global,
    List<String> counties,
    Integer launchYear,
    List<String> types
) {
}
