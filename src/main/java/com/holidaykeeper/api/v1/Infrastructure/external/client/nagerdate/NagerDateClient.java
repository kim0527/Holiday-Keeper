package com.holidaykeeper.api.v1.Infrastructure.external.client.nagerdate;

import com.holidaykeeper.api.v1.Infrastructure.external.client.ApiClient;
import com.holidaykeeper.api.v1.Infrastructure.external.client.response.GetCountryResponse;
import com.holidaykeeper.api.v1.Infrastructure.external.client.response.GetHolidayResponse;
import java.util.List;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Nager.Date API 클라이언트
 *
 * <p>Nager.Date 공개 API를 통해 전 세계 공휴일 정보를 조회하는 클라이언트입니다.
 * {@link ApiClient} 인터페이스의 구현체로, 국가 목록 조회와 특정 국가의 공휴일 조회 기능을 제공합니다.
 *
 * <p><strong>사용하는 외부 API:</strong>
 * <ul>
 *   <li>API 제공자: Nager.Date</li>
 *   <li>Base URL: https://date.nager.at/api/v3</li>
 *   <li>API 문서: <a href="https://date.nager.at/Api">https://date.nager.at/Api</a></li>
 *   <li>인증: 불필요 (공개 API)</li>
 *   <li>Rate Limit: 문서 참조</li>
 * </ul>
 *
 * <p><strong>주요 기능:</strong>
 * <ul>
 *   <li>전 세계 지원 국가 목록 조회</li>
 *   <li>특정 국가와 연도의 공휴일 정보 조회</li>
 * </ul>
 *
 * @see ApiClient
 * @since 1.0
 */
@Component
public class NagerDateClient implements ApiClient {

  private static final String BASE_URL = "https://date.nager.at/api/v3";
  private final RestClient restClient;

  public NagerDateClient() {
    this.restClient = RestClient.builder()
        .baseUrl(BASE_URL)
        .build();
  }

  /**
   * Nager.Date API에서 지원하는 모든 국가 목록을 조회합니다.
   *
   * <p><strong>API 엔드포인트:</strong> {@code GET /AvailableCountries}
   * <p>각 국가 정보에는 국가 코드와 국가명이 포함됩니다.
   *
   * @return 지원하는 모든 국가 정보 리스트
   * @throws org.springframework.web.client.RestClientException API 호출 실패 시
   * @since 1.0
   */
  @Override
  public List<GetCountryResponse> getCountries() {
    return restClient.get()
        .uri(uriBuilder -> uriBuilder
            .path("/AvailableCountries")
            .build())
        .retrieve()
        .body(new ParameterizedTypeReference<>() {});
  }

  /**
   * 특정 국가의 특정 연도 공휴일 정보를 조회합니다.
   *
   * <p><strong>API 엔드포인트:</strong> {@code GET /PublicHolidays/{year}/{countryCode}}
   *
   * <p><strong>파라미터 설명:</strong>
   * <ul>
   *   <li>{@code year}: 조회할 연도 (예: 2024)</li>
   *   <li>{@code countryCode}: 국가 코드 (예: "KR", "US", "JP")</li>
   * </ul>
   *
   * <p><strong>주의사항:</strong>
   * <ul>
   *   <li>유효하지 않은 국가 코드 입력 시 빈 리스트 또는 예외가 발생할 수 있습니다.</li>
   *   <li>과거 연도나 미래 연도에 대한 데이터는 제한적일 수 있습니다.</li>
   * </ul>
   *
   * @param year 조회할 연도
   * @param countryCode 국가 코드
   * @return 해당 국가와 연도의 공휴일 정보 리스트
   * @throws org.springframework.web.client.RestClientException API 호출 실패 시
   * @since 1.0
   */
  @Override
  public List<GetHolidayResponse> getHolidays(int year, String countryCode) {
    return restClient.get()
        .uri(uriBuilder -> uriBuilder
            .path("/PublicHolidays/{year}/{countryCode}")
            .build(year, countryCode))
        .retrieve()
        .body(new ParameterizedTypeReference<>() {});
  }
}
