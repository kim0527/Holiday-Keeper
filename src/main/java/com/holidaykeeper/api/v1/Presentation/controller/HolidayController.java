package com.holidaykeeper.api.v1.Presentation.controller;

import com.holidaykeeper.api.v1.Presentation.request.SearchHolidayRequest;
import com.holidaykeeper.api.v1.Presentation.response.HolidayResponse;
import com.holidaykeeper.api.v1.application.service.HolidayUsecase;
import com.holidaykeeper.api.v1.common.dto.ApiCommonResponse;
import com.holidaykeeper.api.v1.common.dto.Pagination;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/holidays")
@Tag(name = "공휴일(Holidays)", description = "공휴일 관련 API")
public class HolidayController {

  private final HolidayUsecase holidayUsecase;

  @GetMapping
  @Operation(summary = "Search Holiday", description = "등록되어있는 공휴일 조회")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "성공"),
      @ApiResponse(responseCode = "4xx", description = "잘못된 요청"),
  })
  public ResponseEntity<ApiCommonResponse<Pagination<HolidayResponse>>> searchHoliday(@ModelAttribute SearchHolidayRequest request) {
    return ResponseEntity.status(HttpStatus.OK)
        .body(ApiCommonResponse.success(
            holidayUsecase.searchHoliday(
                request.year(),
                request.countryCode(),
                request.holidayType(),
                request.sortType(),
                request.sortOrder(),
                request.page(),
                request.size()),
            "조회 성공"
            ));
  }

  @PostMapping("/{countryCode}/{year}")
  @Operation(summary = "Refresh Holiday", description = "특정 연도·국가 데이터를 재호출하여 Upsert(덮어쓰기)")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "성공"),
      @ApiResponse(responseCode = "4xx", description = "잘못된 요청"),
  })
  @Parameters({
      @Parameter(name = "countryCode", description = "국가 코드", example = "KR"),
      @Parameter(name = "year", description = "대상 연도", example = "2025")
  })
  public ResponseEntity<ApiCommonResponse> refreshHolidays(
      @PathVariable String countryCode,
      @PathVariable int year
  ) {
    holidayUsecase.refreshHolidays(countryCode, year);
    return ResponseEntity.status(HttpStatus.OK)
        .body(ApiCommonResponse.success(null,"공휴일 재동기화 완료 ( countryCode: %s, year: %d )".formatted(countryCode,year)));
  }

  @DeleteMapping("/{countryCode}/{year}")
  @Operation(summary = "Delete Holiday", description = "특정 연도·국가의 공휴일 레코드 전체 삭제")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "성공"),
      @ApiResponse(responseCode = "4xx", description = "잘못된 요청"),
  })
  @Parameters({
      @Parameter(name = "countryCode", description = "국가 코드", example = "KR"),
      @Parameter(name = "year", description = "대상 연도", example = "2025")
  })
  public ResponseEntity<ApiCommonResponse> deleteHolidays(
      @PathVariable String countryCode,
      @PathVariable int year
  ) {
    holidayUsecase.deleteHolidays(countryCode, year);
    return ResponseEntity.status(HttpStatus.OK)
        .body(ApiCommonResponse.success(null,"공휴일 일괄 삭제 완료 ( countryCode: %s, year: %d )".formatted(countryCode,year)));
  }
}
