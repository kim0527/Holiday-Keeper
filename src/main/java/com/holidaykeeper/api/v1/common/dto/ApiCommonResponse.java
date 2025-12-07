package com.holidaykeeper.api.v1.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "공통 API 응답 형식")
public class ApiCommonResponse<T> {

  @Schema(description = "응답 메시지")
  private String message;

  @Schema(description = "실제 응답 데이터")
  private T data;

  public static <T> ApiCommonResponse<T> success(T data, String message) {
    return ApiCommonResponse.<T>builder()
        .message(message)
        .data(data)
        .build();
  }

  public static <T> ApiCommonResponse<T> failure(String message) {
    return ApiCommonResponse.<T>builder()
        .message(message)
        .data(null)
        .build();
  }

}
