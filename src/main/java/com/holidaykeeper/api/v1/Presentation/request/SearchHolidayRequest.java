package com.holidaykeeper.api.v1.Presentation.request;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Optional;

@Schema(description = "공휴일 정보 조회 결과 Response")
public record SearchHolidayRequest(

    @Schema(description = "페이지 번호 (0부터 시작)", example = "0")
    Optional<Integer> page,

    @Schema(description = "페이지 크기", example = "20")
    Optional<Integer> size,

    @Schema(description = "정렬 기준", example = "date")
    Optional<String> sortType,

    @Schema(description = "정렬 순서 (ASC 또는 DESC)", example = "ASC")
    Optional<String> sortOrder,

    @Schema(description = "조회할 연도", example = "2025")
    Optional<Integer> year,

    @Schema(description = "국가 코드", example = "KR")
    Optional<String> countryCode,

    @Schema(description = "공휴일 타입", example = "Public")
    Optional<String> holidayType
) {
}
