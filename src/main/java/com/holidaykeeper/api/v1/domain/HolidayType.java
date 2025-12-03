package com.holidaykeeper.api.v1.domain;

public enum HolidayType {
  PUBLIC("Public","공식 공휴일"),
  BANK("Bank","은행 휴무"),
  SCHOOL("School","학교 휴무"),
  AUTHORITIES("Authorities","공공기관 휴무"),
  OPTIONAL("Optional","기관에서의 선택적 휴일"),
  OBSERVANCE("Observance","기념일");

  private String type;
  private String info;

  HolidayType(String type, String info) {
    this.type = type;
    this.info = info;
  }
}
