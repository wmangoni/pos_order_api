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
    // The prompt also shows limit and offset in the order service's paginated response.
    // For consistency, we can add them here too, or stick to the prompt's specific example for menu items.
    // Let's stick to the prompt for menu items: {"items": [], "totalRecords": N}
}