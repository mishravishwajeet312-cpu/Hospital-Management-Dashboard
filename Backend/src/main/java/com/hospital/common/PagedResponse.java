package com.hospital.common;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PagedResponse<T> {
  private List<T> data;
  private int currentPage;
  private long totalItems;
  private int totalPages;
}
