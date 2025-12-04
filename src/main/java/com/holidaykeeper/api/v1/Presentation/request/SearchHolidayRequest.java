package com.holidaykeeper.api.v1.Presentation.request;

import java.util.Optional;

public record SearchHolidayRequest(
    Optional<Integer> page,
    Optional<Integer> size,
    Optional<String> sortType,
    Optional<String> sortOrder,
    Optional<Integer> year,
    Optional<String> countryCode,
    Optional<String> holidayType
) {
}
