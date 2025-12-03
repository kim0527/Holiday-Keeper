package com.holidaykeeper.api.v1.Infrastructure.external.client;

import com.holidaykeeper.api.v1.Infrastructure.external.client.response.GetCountryResponse;
import com.holidaykeeper.api.v1.Infrastructure.external.client.response.GetHolidayResponse;
import java.util.List;

public interface ApiClient {
  List<GetCountryResponse> getCountries();
  List<GetHolidayResponse> getHolidays(int year, String countryCode);
}
