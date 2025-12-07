package com.holidaykeeper.api.v1.common.dto;

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
public class ApiResponse<T> {

  private String message;
  private T data;

  public static <T> ApiResponse<T> success(T data, String message) {
    return ApiResponse.<T>builder()
        .message(message)
        .data(data)
        .build();
  }

  public static <T> ApiResponse<T> failure(String message) {
    return ApiResponse.<T>builder()
        .message(message)
        .data(null)
        .build();
  }

}
