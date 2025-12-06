package com.holidaykeeper.api.v1.Infrastructure.respoitory.holiday;

import com.holidaykeeper.api.v1.Infrastructure.external.client.response.GetHolidayResponse;
import com.holidaykeeper.api.v1.application.util.JsonUtil;
import com.holidaykeeper.api.v1.domain.Holiday;
import java.sql.PreparedStatement;
import java.sql.SQLException;
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

  @Value("${app.batch.size}")
  private int batchSize;

  @Override
  public void bulkInsert(List<GetHolidayResponse> holidays) {
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
          modified_at,
          is_deleted,
          deleted_at
        )
        KEY(country_id, date, name)
        VALUES (?, (SELECT country_id FROM country WHERE country_code = ?), ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    """;

    LocalDateTime now = LocalDateTime.now();

    for (int i = 0; i < holidays.size(); i += batchSize) {
      int endIndex = Math.min(i + batchSize, holidays.size());
      List<GetHolidayResponse> batch = holidays.subList(i, endIndex);

      jdbcTemplate.batchUpdate(query, batch, batch.size(),
          (statement, holiday) -> setInsertParameters(statement, holiday, now));
    }
  }

  @Override
  public void bulkUpdate(List<Holiday> holidays) {
    if (holidays == null || holidays.isEmpty()) {
      log.warn("업데이트할 holiday 데이터가 존재하지 않습니다.");
      return;
    }

    String query = """
        UPDATE holiday 
        SET 
          date = ?,
          local_name = ?,
          name = ?,
          fixed = ?,
          global = ?,
          counties_json = ?,
          launch_year = ?,
          types_json = ?,
          modified_at = ?
        WHERE holiday_id = ?
    """;

    LocalDateTime now = LocalDateTime.now();

    for (int i = 0; i < holidays.size(); i += batchSize) {
      int endIndex = Math.min(i + batchSize, holidays.size());
      List<Holiday> batch = holidays.subList(i, endIndex);

      jdbcTemplate.batchUpdate(query, batch, batch.size(),
          (statement, holiday) -> setUpdateParameters(statement, holiday, now));
    }
  }

  @Override
  public void bulkDelete(List<Holiday> holidays) {
    if (holidays == null || holidays.isEmpty()) {
      log.warn("삭제할 holiday 데이터가 존재하지 않습니다.");
      return;
    }

    String query = """
        UPDATE holiday 
        SET 
          is_deleted = true,
          deleted_at = ?,
          modified_at = ?
        WHERE holiday_id = ?
    """;

    LocalDateTime now = LocalDateTime.now();

    for (int i = 0; i < holidays.size(); i += batchSize) {
      int endIndex = Math.min(i + batchSize, holidays.size());
      List<Holiday> batch = holidays.subList(i, endIndex);

      jdbcTemplate.batchUpdate(query, batch, batch.size(),
          (statement, holiday) -> setDeleteParameters(statement, holiday, now));
    }
  }

  private void setInsertParameters(
      PreparedStatement statement,
      GetHolidayResponse holiday,
      LocalDateTime now
  ) throws SQLException {
    statement.setObject(1, UUID.randomUUID());
    statement.setString(2, holiday.countryCode());
    statement.setObject(3, holiday.date());
    statement.setString(4, holiday.localName());
    statement.setString(5, holiday.name());
    statement.setBoolean(6, holiday.fixed());
    statement.setBoolean(7, holiday.global());
    statement.setString(8, JsonUtil.toJson(holiday.counties()));
    statement.setObject(9, holiday.launchYear());
    statement.setString(10, JsonUtil.toJson(holiday.types()));
    statement.setObject(11, now);
    statement.setObject(12, now);
    statement.setBoolean(13, false);
    statement.setObject(14, null);
  }

  private void setUpdateParameters(
      PreparedStatement statement,
      Holiday holiday,
      LocalDateTime now
  ) throws SQLException {
    statement.setObject(1, holiday.getDate());
    statement.setString(2, holiday.getLocalName());
    statement.setString(3, holiday.getName());
    statement.setBoolean(4, holiday.getFixed());
    statement.setBoolean(5, holiday.getGlobal());
    statement.setString(6, holiday.getCountiesJson());
    statement.setObject(7, holiday.getLaunchYear());
    statement.setString(8, holiday.getTypesJson());
    statement.setObject(9, now);
    statement.setObject(10, holiday.getId());
  }

  private void setDeleteParameters(
      PreparedStatement statement,
      Holiday holiday,
      LocalDateTime now
  ) throws SQLException {
    statement.setObject(1, now);
    statement.setObject(2, now);
    statement.setObject(3, holiday.getId());
  }

}
