package com.holidaykeeper.api.v1.Infrastructure.respoitory.holiday;

import com.holidaykeeper.api.v1.Infrastructure.external.client.response.GetHolidayResponse;
import com.holidaykeeper.api.v1.domain.Holiday;
import java.util.List;
import java.util.Map;

public interface HolidayJdbcRepository {
  void bulkInsert(List<GetHolidayResponse> holidays);
  void bulkUpdate(Map<Holiday,GetHolidayResponse> holidays);
  void bulkDelete(List<Holiday> holidays);
}
