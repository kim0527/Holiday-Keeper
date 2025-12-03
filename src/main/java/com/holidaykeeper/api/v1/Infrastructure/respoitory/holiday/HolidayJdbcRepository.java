package com.holidaykeeper.api.v1.Infrastructure.respoitory.holiday;

import com.holidaykeeper.api.v1.Infrastructure.external.client.response.GetHolidayResponse;
import java.util.List;

public interface HolidayJdbcRepository {
  void save(List<GetHolidayResponse> holidays);
}
