package com.holidaykeeper.api.v1.Presentation.response;

import com.holidaykeeper.api.v1.domain.HolidayType;
import java.time.LocalDate;
import java.util.List;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "공휴일 정보 Response")
public record HolidayResponse(

    @Schema(description = "국가 코드 ", example = "KR")
    String countryCode,

    @Schema(description = "국가 이름", example = "Korea")
    String countryName,

    @Schema(description = "공휴일 날짜", example = "2025-01-01")
    LocalDate date,

    @Schema(description = "지역 언어 기준 명칭", example = "새해")
    String localName,

    @Schema(description = "영문 명칭", example = "New Year's Day")
    String name,

    @Schema(description = "최초 제정 연도", example = "1949")
    Integer launchYear,

    @Schema(description = "공휴일 타입 목록", example = "[\"PUBLIC\", \"NATIONAL\"]")
    List<HolidayType> type
) {
}
