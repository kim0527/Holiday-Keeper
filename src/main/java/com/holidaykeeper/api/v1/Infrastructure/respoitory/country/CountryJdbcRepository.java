package com.holidaykeeper.api.v1.Infrastructure.respoitory.country;

import com.holidaykeeper.api.v1.Infrastructure.external.client.response.GetCountryResponse;
import java.util.List;

public interface CountryJdbcRepository {
  void save(List<GetCountryResponse> countries);
}
