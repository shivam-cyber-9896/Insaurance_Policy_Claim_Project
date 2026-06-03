package com.example.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagedResponseDto<T> {
    private List<T> records;
    private int currentPageNumber;
    private int pageSize;
    private long totalRecords;
    private int totalPages;
    private boolean lastPage;
    private String sortField;
    private String sortDirection;
}
