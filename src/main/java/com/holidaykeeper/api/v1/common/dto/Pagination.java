package com.holidaykeeper.api.v1.common.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Pagination<T> {
  private final int page;
  private final int size;
  private final Long total;
  private final List<T> content;

  public static <T> Pagination<T> of(int page, int size, Long total, List<T> content) {
    return new Pagination<>(page, size, total, content);
  }
}
