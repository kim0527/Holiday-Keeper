package com.holidaykeeper.api.v1.Infrastructure.external.client.response;

public record GetCountryResponse(
    String countryCode,
    String name
) {
}
