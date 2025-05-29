package com.allo.menuservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaginatedMenuItemResponseDTO {
    private List<MenuItemResponseDTO> items;
    private long totalRecords;
}