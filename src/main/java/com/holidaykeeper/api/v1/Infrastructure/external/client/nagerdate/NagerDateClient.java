package com.holidaykeeper.api.v1.Infrastructure.external.client.nagerdate;

import com.holidaykeeper.api.v1.Infrastructure.external.client.ApiClient;
import com.holidaykeeper.api.v1.Infrastructure.external.client.response.GetCountryResponse;
import com.holidaykeeper.api.v1.Infrastructure.external.client.response.GetHolidayResponse;
import java.util.List;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class NagerDateClient implements ApiClient {

  private static final String BASE_URL = "https://date.nager.at/api/v3";
  private final RestClient restClient;

  public NagerDateClient() {
    this.restClient = RestClient.builder()
        .baseUrl(BASE_URL)
        .build();
  }

  @Override
  public List<GetCountryResponse> getCountries() {
    return restClient.get()
        .uri(uriBuilder -> uriBuilder
            .path("/AvailableCountries")
            .build())
        .retrieve()
        .body(new ParameterizedTypeReference<>() {});
  }

  @Override
  public List<GetHolidayResponse> getHolidays(int year, String countryCode) {
    return restClient.get()
        .uri(uriBuilder -> uriBuilder
            .path("/PublicHolidays/{year}/{countryCode}")
            .build(year, countryCode))
        .retrieve()
        .body(new ParameterizedTypeReference<>() {});
  }
}
