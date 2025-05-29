package com.allo.menuservice.controller;

import com.allo.menuservice.dto.MenuItemRequestDTO;
import com.allo.menuservice.dto.MenuItemResponseDTO;
import com.allo.menuservice.dto.PaginatedMenuItemResponseDTO;
import com.allo.menuservice.service.MenuItemService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/menu-items")
public class MenuItemController {

    private final MenuItemService menuItemService;

    public MenuItemController(MenuItemService menuItemService) {
        this.menuItemService = menuItemService;
    }

    @PostMapping
    public ResponseEntity<MenuItemResponseDTO> createMenuItem(@Valid @RequestBody MenuItemRequestDTO requestDTO) {
        MenuItemResponseDTO createdItem = menuItemService.createMenuItem(requestDTO);
        return new ResponseEntity<>(createdItem, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MenuItemResponseDTO> updateMenuItem(@PathVariable String id, @Valid @RequestBody MenuItemRequestDTO requestDTO) {
        return menuItemService.updateMenuItem(id, requestDTO)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteMenuItem(@PathVariable String id) {
        boolean deleted = menuItemService.deleteMenuItem(id);
        if (deleted) {
            // As per spec: { "message": "Menu item deleted successfully", "id": "abc123" }
            Map<String, String> response = Map.of(
                "message", "Menu item deleted successfully",
                "id", id
            );
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping
    public ResponseEntity<PaginatedMenuItemResponseDTO> getAllMenuItems(
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "0") int offset) {
        PaginatedMenuItemResponseDTO response = menuItemService.getAllMenuItems(limit, offset);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MenuItemResponseDTO> getMenuItemById(@PathVariable String id) {
        return menuItemService.getMenuItemById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Global Exception Handler (Basic)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericException(Exception ex) {
        // Log the exception
        // ex.printStackTrace(); // For debugging, don't use in prod directly
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                             .body(Collections.singletonMap("error", "An unexpected error occurred: " + ex.getMessage()));
    }

    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(org.springframework.web.bind.MethodArgumentNotValidException ex) {
        Map<String, String> errors = new java.util.HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((org.springframework.validation.FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        Map<String, Object> responseBody = new java.util.HashMap<>();
        responseBody.put("message", "Validation failed");
        responseBody.put("errors", errors);
        return new ResponseEntity<>(responseBody, HttpStatus.BAD_REQUEST);
    }
}
