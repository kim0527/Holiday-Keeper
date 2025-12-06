package com.holidaykeeper.api.v1.Presentation.controller;

import com.holidaykeeper.api.v1.Presentation.request.SearchHolidayRequest;
import com.holidaykeeper.api.v1.Presentation.response.HolidayResponse;
import com.holidaykeeper.api.v1.application.service.HolidayUsecase;
import com.holidaykeeper.api.v1.common.ApiResponse;
import com.holidaykeeper.api.v1.common.Pagination;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/holiday")
public class HolidayController {

  private final HolidayUsecase holidayUsecase;

  @GetMapping
  public ResponseEntity<ApiResponse<Pagination<HolidayResponse>>> searchHoliday(@ModelAttribute SearchHolidayRequest request) {
    return ResponseEntity.status(HttpStatus.OK)
        .body(ApiResponse.success(
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
  public ResponseEntity<ApiResponse> refreshHolidays(
      @PathVariable String countryCode,
      @PathVariable int year
  ) {
    holidayUsecase.refreshHolidays(countryCode, year);
    return ResponseEntity.status(HttpStatus.OK)
        .body(ApiResponse.success(null,"갱신 성공"));
  }
}
