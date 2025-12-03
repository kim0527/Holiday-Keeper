package com.holidaykeeper.api.v1.Infrastructure.respoitory.holiday;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.holidaykeeper.api.v1.Infrastructure.external.client.response.GetHolidayResponse;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@RequiredArgsConstructor
public class HolidayJdbcRepositoryImpl implements HolidayJdbcRepository {

  private final JdbcTemplate jdbcTemplate;
  private final ObjectMapper objectMapper;

  @Value("${app.batch.size}")
  private int batchSize;

  @Override
  public void save(List<GetHolidayResponse> holidays) {
    if (holidays == null || holidays.isEmpty()) {
      log.warn("holiday 데이터가 존재하지 않습니다.");
      return;
    }

    String query = """
        MERGE INTO holiday (
          holiday_id, 
          country_id, 
          date, 
          local_name, 
          name, 
          fixed, 
          global,
          counties_json, 
          launch_year,
          types_json, 
          created_at,
          modified_at
        )
        KEY(country_id, date, name)
        VALUES (?, (SELECT country_id FROM country WHERE country_code = ?), ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    """;

    LocalDateTime now = LocalDateTime.now();

    for (int i = 0; i < holidays.size(); i += batchSize) {
      int end = Math.min(i + batchSize, holidays.size());
      List<GetHolidayResponse> batch = holidays.subList(i, end);
      execute(query, batch, now);
    }

  }

  private void execute(String query, List<GetHolidayResponse> batch, LocalDateTime now) {
    jdbcTemplate.batchUpdate(query, batch, batch.size(),
        (statement, holiday) -> {
          statement.setObject(1, UUID.randomUUID());
          statement.setString(2, holiday.countryCode());
          statement.setObject(3, holiday.date());
          statement.setString(4, holiday.localName());
          statement.setString(5, holiday.name());
          statement.setBoolean(6, holiday.fixed());
          statement.setBoolean(7, holiday.global());
          statement.setString(8, toJson(holiday.counties()));
          statement.setObject(9, holiday.launchYear());
          statement.setString(10, toJson(holiday.types()));
          statement.setObject(11, now);
          statement.setObject(12, now);
        });
  }

  private String toJson(List<?> li) {
    try {
      return objectMapper.writeValueAsString(li);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("JSON 변환 중 문제가 발생했습니다.", e);
    }
  }

}
