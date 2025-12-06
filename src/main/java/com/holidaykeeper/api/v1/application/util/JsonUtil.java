package com.holidaykeeper.api.v1.application.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.holidaykeeper.api.v1.domain.HolidayType;
import java.util.List;

public class JsonUtil {
  private static final ObjectMapper objectMapper = new ObjectMapper();

  public static String toJson(List<?> li) {
    try {
      return objectMapper.writeValueAsString(li);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("JSON 변환 중 문제가 발생했습니다.", e);
    }
  }

  public static List<HolidayType> toHolidayType(String typesJson) {
    if (typesJson == null || typesJson.isEmpty()) {
      return List.of();
    }
    try {
      objectMapper.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS);
      return objectMapper.readValue(
          typesJson,
          objectMapper.getTypeFactory().constructCollectionType(List.class, HolidayType.class)
      );
    } catch (Exception e) {
      // todo: 예외 처리
      e.printStackTrace();
      return List.of();
    }
  }
}
