package com.allo.orderservice.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaginatedOrderResponseDTO {
    private List<OrderResponseDTO> orders;
    private int limit;
    private int offset;
    private long totalRecords;
}