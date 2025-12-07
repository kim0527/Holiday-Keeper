package com.holidaykeeper.api.v1.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "페이징 응답 정보")
public class Pagination<T> {

  @Schema(description = "현재 페이지", example = "0")
  private final int page;

  @Schema(description = "페이지 크기", example = "10")
  private final int size;

  @Schema(description = "전체 데이터 개수", example = "150")
  private final Long total;

  @Schema(description = "페이지 내 데이터 목록")
  private final List<T> content;

  public static <T> Pagination<T> of(int page, int size, Long total, List<T> content) {
    return new Pagination<>(page, size, total, content);
  }
}
