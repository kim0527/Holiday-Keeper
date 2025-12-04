package com.holidaykeeper.api.v1.application.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.holidaykeeper.api.v1.Infrastructure.external.client.ApiClient;
import com.holidaykeeper.api.v1.Infrastructure.external.client.response.GetCountryResponse;
import com.holidaykeeper.api.v1.Infrastructure.external.client.response.GetHolidayResponse;
import com.holidaykeeper.api.v1.Infrastructure.respoitory.country.CountryJdbcRepository;
import com.holidaykeeper.api.v1.Infrastructure.respoitory.holiday.HolidayJdbcRepository;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@DisplayName("AppInitService Mock 테스트")
public class AppInitServiceMockTest {

  @Mock
  private CountryJdbcRepository countryJdbcRepository;

  @Mock
  private HolidayJdbcRepository holidayJdbcRepository;

  @Mock
  private ApiClient apiClient;

  @InjectMocks
  private AppInitService appInitService;

  @Captor
  private ArgumentCaptor<List<GetHolidayResponse>> holidayCaptor;

  @BeforeEach
  void setUp() {
    // batchSize : DB bulk insert 배치 사이즈 , retryCount : 외부 API 재시도 횟수
    ReflectionTestUtils.setField(appInitService, "batchSize", 3);
    ReflectionTestUtils.setField(appInitService, "retryCount", 3);
  }

  @Test
  @DisplayName("배치 사이즈 맞춰서 공휴일을 분할 저장한다.")
  void saveRecentHolidays() {
    List<GetCountryResponse> mockCountries = List.of(
        new GetCountryResponse("KR", "South Korea")
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
      when(apiClient.getHolidays(eq(y), eq("KR"))).thenReturn(krHolidays);
    }

    // when
    appInitService.saveRecentHolidays();

    // then
    // [2020년 ~ 2025년] 6 x 2 = 12개, 배치 사이즈 = 3, 총 토탈 호출 횟수 = 12 / 3 = 4
    verify(holidayJdbcRepository, atLeast(4));
  }

  @Test
  @DisplayName("외부 API 조회 실패시 재시도 횟수만큼 재시도한다.")
  void retriesOnRetryCount() {
    // given
    when(apiClient.getCountries())
        .thenThrow(new RuntimeException("API Error"))
        .thenThrow(new RuntimeException("API Error"))
        .thenReturn(List.of(new GetCountryResponse("KR", "South Korea")));

    when(apiClient.getHolidays(anyInt(), anyString())).thenReturn(List.of());

    // when
    appInitService.saveRecentHolidays();

    // then
    verify(apiClient, times(3)).getCountries();
  }

  @Test
  @DisplayName("재시도 횟수를 초과하면 예외를 발생시킨다")
  void throwsExceptionAfterMaxRetries() {
    // given
    when(apiClient.getCountries())
        .thenThrow(new RuntimeException("API Error"));

    // when & then
    assertThatThrownBy(() -> appInitService.saveRecentHolidays()).isInstanceOf(RuntimeException.class);
    verify(apiClient, times(3)).getCountries();
  }

}
