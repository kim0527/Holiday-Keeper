package com.holidaykeeper.api.v1.Infrastructure.respoitory.country;

import com.holidaykeeper.api.v1.Infrastructure.external.client.response.GetCountryResponse;
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
public class CountryJdbcRepositoryImpl implements CountryJdbcRepository {

  private final JdbcTemplate jdbcTemplate;

  @Value("${app.batch.size}")
  private int batchSize;

  @Override
  public void bulkInsert(List<GetCountryResponse> countries) {
    if (countries == null || countries.isEmpty()) {
      log.warn("countries 데이터가 존재하지 않습니다.");
      return;
    }

    String query = """
        MERGE INTO country (
          country_id, 
          country_code, 
          country_name, 
          created_at, 
          modified_at,
          is_deleted,
          deleted_at
        )
        KEY(country_code)
        VALUES (?, ?, ?, ?, ?, ?, ?)
    """;

    LocalDateTime now = LocalDateTime.now();

    for (int i = 0; i < countries.size(); i += batchSize) {
      int end = Math.min(i + batchSize, countries.size());
      List<GetCountryResponse> batch = countries.subList(i, end);
      execute(query, batch, now);
    }

  }

  private void execute(String query, List<GetCountryResponse> batch, LocalDateTime now) {
    jdbcTemplate.batchUpdate(query, batch, batch.size(),
        (statement, country) -> {
          statement.setObject(1, UUID.randomUUID());
          statement.setString(2, country.countryCode());
          statement.setString(3, country.name());
          statement.setObject(4, now);
          statement.setObject(5, now);
          statement.setObject(6, false);
          statement.setObject(7, null);
        });
  }

}
