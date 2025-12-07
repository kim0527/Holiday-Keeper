package com.holidaykeeper.api.v1.domain;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "공휴일 타입")
public enum HolidayType {

  @Schema(description = "공식 공휴일")
  PUBLIC("Public", "공식 공휴일"),

  @Schema(description = "은행 휴무")
  BANK("Bank", "은행 휴무"),

  @Schema(description = "학교 휴무")
  SCHOOL("School", "학교 휴무"),

  @Schema(description = "공공기관 휴무")
  AUTHORITIES("Authorities", "공공기관 휴무"),

  @Schema(description = "기관에서의 선택적 휴일")
  OPTIONAL("Optional", "기관에서의 선택적 휴일"),

  @Schema(description = "기념일")
  OBSERVANCE("Observance", "기념일");

  private final String type;
  private final String info;

  HolidayType(String type, String info) {
    this.type = type;
    this.info = info;
  }
}
