package com.holidaykeeper.api.v1.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.holidaykeeper.api.v1.Infrastructure.external.client.ApiClient;
import com.holidaykeeper.api.v1.Infrastructure.external.client.response.GetCountryResponse;
import com.holidaykeeper.api.v1.Infrastructure.external.client.response.GetHolidayResponse;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest
@TestPropertySource(properties = {
    "app.batch.size=5",
    "app.api.retry_count=3"
})
@DisplayName("AppInitService 통합 테스트")
public class AppInitServiceTest {

  @Autowired
  private AppInitService appInitService;

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @MockitoBean
  private ApiClient apiClient;

  @BeforeEach
  void setUp() {
    jdbcTemplate.execute("DELETE FROM holiday");
    jdbcTemplate.execute("DELETE FROM country");
  }

  @Test
  @DisplayName("최근 5년의 모든 국가의 공휴일이 저장된다.(2020년 ~ 2025년)")
  void saveRecentHolidays() {
    // given
    List<GetCountryResponse> mockCountries = List.of(
        new GetCountryResponse("KR", "South Korea"),
        new GetCountryResponse("US", "United States")
    );
    when(apiClient.getCountries()).thenReturn(mockCountries);

    for (int year = 2020; year <= 2025; year++) {
      int y = year;

      List<GetHolidayResponse> krHolidays = List.of(
          new GetHolidayResponse(LocalDate.of(y, 1, 1), "새해", "New Year's Day", "KR",
              false, false, Collections.emptyList(), null, Collections.emptyList()),
          new GetHolidayResponse(LocalDate.of(y, 3, 1), "3-1절", "Independence Movement Day", "KR",
              false, false, Collections.emptyList(), null, Collections.emptyList())
      );

      List<GetHolidayResponse> usHolidays = List.of(
          new GetHolidayResponse(LocalDate.of(y, 1, 20), "Martin Luther King, Jr. Day", "Martin Luther King, Jr. Day", "US",
              false, false, Collections.emptyList(), null, Collections.emptyList()),
          new GetHolidayResponse(LocalDate.of(y, 2, 12), "Lincoln's Birthday", "Lincoln's Birthday", "US",
              false, false, Collections.emptyList(), null, Collections.emptyList()),
          new GetHolidayResponse(LocalDate.of(y, 2, 17), "Washington's Birthday", "Washington's Birthday", "US",
              false, false, Collections.emptyList(), null, Collections.emptyList())
      );

      when(apiClient.getHolidays(eq(y), eq("KR"))).thenReturn(krHolidays);
      when(apiClient.getHolidays(eq(y), eq("US"))).thenReturn(usHolidays);
    }

    // when
    appInitService.saveRecentHolidays();

    // then
    Integer totalCountryCnt = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM country", Integer.class);
    Integer totalHolidayCnt = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM holiday", Integer.class);
    Integer KrHolidayCnt = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM holiday h JOIN country c ON h.country_id = c.country_id WHERE c.country_code = 'KR'", Integer.class);
    Integer UsHolidayCnt = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM holiday h JOIN country c ON h.country_id = c.country_id WHERE c.country_code = 'US'", Integer.class);

    // [KR] 6 x 2 = 12 , [US] 6 x 3 = 18
    assertThat(totalCountryCnt).isEqualTo(2);
    assertThat(totalHolidayCnt).isEqualTo(30);
    assertThat(KrHolidayCnt).isEqualTo(12);
    assertThat(UsHolidayCnt).isEqualTo(18);
  }

}
