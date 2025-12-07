package com.holidaykeeper.api.v1.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.holidaykeeper.api.v1.Infrastructure.external.client.ApiClient;
import com.holidaykeeper.api.v1.Infrastructure.external.client.response.GetHolidayResponse;
import com.holidaykeeper.api.v1.Infrastructure.respoitory.country.CountryRepository;
import com.holidaykeeper.api.v1.Infrastructure.respoitory.holiday.HolidayRepository;
import com.holidaykeeper.api.v1.Presentation.response.HolidayResponse;
import com.holidaykeeper.api.v1.application.util.JsonUtil;
import com.holidaykeeper.api.v1.common.dto.Pagination;
import com.holidaykeeper.api.v1.domain.Country;
import com.holidaykeeper.api.v1.domain.Holiday;
import com.holidaykeeper.api.v1.domain.HolidayType;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest
@DisplayName("HolidayService 통합 테스트")
public class HolidayServiceTest {

  @Autowired
  private HolidayService holidayService;

  @Autowired
  private HolidayRepository holidayRepository;

  @Autowired
  private CountryRepository countryRepository;

  @Autowired
  private EntityManager entityManager;

  @MockitoBean
  private ApiClient apiClient;

  private Country kr;
  private Country us;

  @BeforeEach
  void setUp() {
    holidayRepository.deleteAllInBatch();
    countryRepository.deleteAllInBatch();

    kr = countryRepository.save(Country.of("KR", "South Korea"));
    us = countryRepository.save(Country.of("US", "United States"));

    holidayRepository.save(Holiday.builder()
        .country(kr)
        .date(LocalDate.of(2025, 1, 1))
        .localName("설날")
        .name("New Year's Day")
        .fixed(true)
        .global(true)
        .launchYear(null)
        .typesJson("[\"Public\"]")
        .build());

    holidayRepository.save(Holiday.builder()
        .country(kr)
        .date(LocalDate.of(2025, 5, 1))
        .localName("근로자의 날")
        .name("Workers Day")
        .fixed(true)
        .global(true)
        .launchYear(null)
        .typesJson("[\"Optional\"]")
        .build());

    holidayRepository.save(Holiday.builder()
        .country(us)
        .date(LocalDate.of(2025, 7, 4))
        .localName("Independence Day")
        .name("Independence Day")
        .fixed(true)
        .global(true)
        .launchYear(null)
        .typesJson("[\"Public\"]")
        .build());

    holidayRepository.save(Holiday.builder()
        .country(kr)
        .date(LocalDate.of(2024, 1, 1))
        .localName("설날")
        .name("New Year's Day")
        .fixed(true)
        .global(true)
        .launchYear(null)
        .typesJson("[\"Public\"]")
        .build());

    holidayRepository.save(Holiday.builder()
        .country(us)
        .date(LocalDate.of(2023, 11, 11))
        .localName("Test1 Day")
        .name("Test1 Day")
        .fixed(true)
        .global(true)
        .launchYear(null)
        .typesJson("[\"Optional\"]")
        .build());

    holidayRepository.save(Holiday.builder()
        .country(us)
        .date(LocalDate.of(2023, 10, 10))
        .localName("Test2 Day")
        .name("Test2 Day")
        .fixed(true)
        .global(true)
        .launchYear(null)
        .typesJson("[\"Optional\"]")
        .build());

    holidayRepository.save(Holiday.builder()
        .country(us)
        .date(LocalDate.of(2023, 9, 9))
        .localName("Test3 Day")
        .name("Test3 Day")
        .fixed(true)
        .global(true)
        .launchYear(null)
        .typesJson("[\"Optional\"]")
        .build());
  }

  @Test
  @DisplayName("필터 조건 없이 데이터를 조회할 수 있다. (default: page=0, size=10, sortType=date, sortOrder=DESC)")
  void searchHolidayByDefault() {
    // when
    Pagination<HolidayResponse> result = holidayService.searchHoliday(
        Optional.empty(),
        Optional.empty(),
        Optional.empty(),
        Optional.empty(),
        Optional.empty(),
        Optional.empty(),
        Optional.empty()
    );

    // then
    assertThat(result.getContent()).hasSize(7);
    assertThat(result.getPage()).isEqualTo(0);
    assertThat(result.getSize()).isEqualTo(10);
    assertThat(result.getContent()).isSortedAccordingTo(Comparator.comparing(HolidayResponse::date).reversed());
  }

  @Test
  @DisplayName("year 필터로 조회할 수 있다.")
  void searchHolidayByYear() {
    // when
    int year = 2025;
    Pagination<HolidayResponse> result = holidayService.searchHoliday(
        Optional.of(year),
        Optional.empty(),
        Optional.empty(),
        Optional.empty(),
        Optional.empty(),
        Optional.empty(),
        Optional.empty()
    );

    // then
    assertThat(result.getContent()).hasSize(3);
    assertThat(result.getContent()).allMatch(holiday -> holiday.date().getYear() == 2025);
  }

  @Test
  @DisplayName("countryCode 필터로 조회할 수 있다.")
  void searchHolidayByCountryCode() {
    // when
    String countryCode = "KR";
    Pagination<HolidayResponse> result = holidayService.searchHoliday(
        Optional.empty(),
        Optional.of(countryCode),
        Optional.empty(),
        Optional.empty(),
        Optional.empty(),
        Optional.empty(),
        Optional.empty()
    );
    // then
    assertThat(result.getContent()).hasSize(3);
    assertThat(result.getContent()).allMatch(holiday -> holiday.countryCode().equals("KR"));
  }

  @Test
  @DisplayName("holidayType 필터로 조회할 수 있다.")
  void searchHolidayByHolidayType() {
    // when
    String holidayType = "Optional";
    Pagination<HolidayResponse> result = holidayService.searchHoliday(
        Optional.empty(),
        Optional.empty(),
        Optional.of(holidayType),
        Optional.empty(),
        Optional.empty(),
        Optional.empty(),
        Optional.empty()
    );
    // then
    assertThat(result.getContent()).hasSize(4);
    assertThat(result.getContent()).allMatch(holiday -> holiday.type().contains(HolidayType.OPTIONAL));
  }

  @Test
  @DisplayName("sortType, sortOrder 기준으로 정렬된 데이터를 조회할 수 있다.")
  void searchHolidayBySortTypeAndSortOrder() {
    // when
    String sortType = "date";
    String sortOrder = "ASC";
    Pagination<HolidayResponse> result = holidayService.searchHoliday(
        Optional.empty(),
        Optional.empty(),
        Optional.empty(),
        Optional.of(sortType),
        Optional.of(sortOrder),
        Optional.empty(),
        Optional.empty()
    );

    // then
    assertThat(result.getContent()).hasSize(7);
    assertThat(result.getContent()).isSortedAccordingTo(Comparator.comparing(HolidayResponse::date));
  }

  @Test
  @DisplayName("page, size 기준으로 Page처리된 데이터를 조회할 수 있다.")
  void searchHolidayByPageAndSize() {
    // when
    int page = 1;
    int size = 2;
    Pagination<HolidayResponse> result = holidayService.searchHoliday(
        Optional.empty(),
        Optional.empty(),
        Optional.empty(),
        Optional.empty(),
        Optional.empty(),
        Optional.of(page),
        Optional.of(size)
    );

    // then
    assertThat(result.getTotal()).isEqualTo(7);
    assertThat(result.getContent()).hasSize(2);
    assertThat(result.getPage()).isEqualTo(1);
  }

  @Test
  @DisplayName("여러 필드 데이터를 조회할 수 있다.")
  void searchHolidayByMultipleFilter() {
    // when
    int year = 2023;
    String countryCode = "US";
    String holidayType = "Optional";
    String sortType = "date";
    String sortOrder = "ASC";
    int page = 0;
    int size = 2;

    Pagination<HolidayResponse> result = holidayService.searchHoliday(
        Optional.of(year),
        Optional.of(countryCode),
        Optional.of(holidayType),
        Optional.of(sortType),
        Optional.of(sortOrder),
        Optional.of(page),
        Optional.of(size)
    );

    // then
    assertThat(result.getTotal()).isEqualTo(3);
    assertThat(result.getContent()).hasSize(2);
    assertThat(result.getPage()).isEqualTo(0);
    assertThat(result.getContent()).isSortedAccordingTo(Comparator.comparing(HolidayResponse::date));
    assertThat(result.getContent()).allMatch(holiday -> holiday.date().getYear() == 2023);
    assertThat(result.getContent()).allMatch(holiday -> holiday.countryCode().equals("US"));
    assertThat(result.getContent()).allMatch(holiday -> holiday.type().contains(HolidayType.OPTIONAL));
  }

  @Test
  @DisplayName("공휴일 재동기화시, 외부 API에 새로운 공휴일이 존재한다면 DB에 저장한다.")
  void refreshHolidaysInsert() {
    String countryCode = "KR";
    int year = 2025;

    // given
    List<GetHolidayResponse> apiHolidays = List.of(
        GetHolidayResponse.of(LocalDate.of(year, 10, 8), "추석", "Chuseok", countryCode, false, true, null, null, List.of("Public")),
        GetHolidayResponse.of(LocalDate.of(year, 10, 9), "한글날", "Hangul Day", countryCode, false, true, null, null, List.of("Public"))
    );
    when(apiClient.getHolidays(anyInt(), anyString())).thenReturn(apiHolidays);

    // when
    holidayService.refreshHolidays(countryCode,year);

    entityManager.flush();
    entityManager.clear();

    // then
    List<Holiday> savedHolidays = holidayRepository.findByCountryCodeAndYear(countryCode, year);

    assertThat(savedHolidays).hasSize(2);
    assertThat(savedHolidays)
        .extracting(Holiday::getName)
        .contains("Chuseok", "Hangul Day");
  }

  @Test
  @DisplayName("공휴일 재동기화시, 기존 공휴일 정보에 변경이 존재한다면 업데이트 한다.")
  void refreshHolidaysUpdate() {
    String countryCode = "KR";
    int year = 2025;
    Boolean newFixed = false;
    Boolean newGlobal = false;
    int newLaunchYear = 1520;
    List<String> newHolidayTypes = List.of("Optional");

    // given
    List<GetHolidayResponse> apiNewHoliday = List.of(
        GetHolidayResponse.of(LocalDate.of(year, 1, 1), "설날", "New Year's Day", countryCode, newFixed, newGlobal, null, newLaunchYear, newHolidayTypes)
    );
    when(apiClient.getHolidays(anyInt(), anyString())).thenReturn(apiNewHoliday);

    // when
    holidayService.refreshHolidays(countryCode,year);

    entityManager.flush();
    entityManager.clear();

    // then
    List<Holiday> updatedHolidays = holidayRepository.findByCountryCodeAndYear(countryCode, year);
    assertThat(updatedHolidays).hasSize(1);

    Holiday updated = updatedHolidays.stream()
        .filter(h -> h.getDate().isEqual(LocalDate.of(year, 1, 1)))
        .findFirst()
        .orElseThrow();

    assertThat(updated.getFixed()).isEqualTo(newFixed);
    assertThat(updated.getGlobal()).isEqualTo(newGlobal);
    assertThat(updated.getLaunchYear()).isEqualTo(newLaunchYear);
    assertThat(updated.getTypesJson()).isEqualTo(JsonUtil.toJson(newHolidayTypes));
  }

  @Test
  @DisplayName("공휴일 재동기화시, API에 없는 공휴일 데이터는 삭제된다.")
  void refreshHolidaysDelete() {
    String countryCode = "KR";
    int year = 2025;

    // given
    List<GetHolidayResponse> apiNewHoliday = List.of(
        GetHolidayResponse.of(LocalDate.of(year, 1, 1), "설날", "New Year's Day", countryCode, false, false, null, null, List.of("Public"))
    );
    when(apiClient.getHolidays(anyInt(), anyString())).thenReturn(apiNewHoliday);

    // when
    holidayService.refreshHolidays(countryCode,year);

    entityManager.flush();
    entityManager.clear();

    // then
    List<Holiday> updatedHolidays = holidayRepository.findByCountryCodeAndYear(countryCode, year);
    assertThat(updatedHolidays).hasSize(1);  // 기존에 25년 KR의 공휴일은 2개(설날, 근로자의 날)였으나, 근로자의 날이 삭제되어 1개만 남음.
  }

  @Test
  @DisplayName("특정 연도·국가의 공휴일을 전체 삭제할 수 있다.")
  void deleteHolidays() {
    // given
    String countryCode = "KR";
    int year = 2025;

    // when
    holidayService.deleteHolidays(countryCode,year);

    entityManager.flush();
    entityManager.clear();

    // then
    List<Holiday> updatedHolidays = holidayRepository.findByCountryCodeAndYear(countryCode, year);
    assertThat(updatedHolidays).hasSize(0);
  }

}
