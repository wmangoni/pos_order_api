package com.allo.menuservice.controller;

import com.allo.menuservice.dto.MenuItemRequestDTO;
import com.allo.menuservice.dto.MenuItemResponseDTO;
import com.allo.menuservice.service.MenuItemService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MenuItemController.class) // Test only the controller layer
public class MenuItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean // Mocks the MenuItemService dependency
    private MenuItemService menuItemService;

    @Autowired
    private ObjectMapper objectMapper; // For converting objects to JSON strings

    private MenuItemRequestDTO menuItemRequestDTO;
    private MenuItemResponseDTO menuItemResponseDTO;

    @BeforeEach
    void setUp() {
        menuItemRequestDTO = new MenuItemRequestDTO();
        menuItemRequestDTO.setName("Test Pizza");
        menuItemRequestDTO.setDescription("Delicious test pizza");
        menuItemRequestDTO.setPrice(10.99);

        menuItemResponseDTO = new MenuItemResponseDTO();
        menuItemResponseDTO.setId("testId123");
        menuItemResponseDTO.setName("Test Pizza");
        menuItemResponseDTO.setDescription("Delicious test pizza");
        menuItemResponseDTO.setPrice(10.99);
        menuItemResponseDTO.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void createMenuItem_shouldReturnCreatedItem() throws Exception {
        when(menuItemService.createMenuItem(any(MenuItemRequestDTO.class))).thenReturn(menuItemResponseDTO);

        mockMvc.perform(post("/menu-items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(menuItemRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("testId123"))
                .andExpect(jsonPath("$.name").value("Test Pizza"));
    }

    @Test
    void getMenuItemById_whenItemExists_shouldReturnItem() throws Exception {
        when(menuItemService.getMenuItemById("testId123")).thenReturn(Optional.of(menuItemResponseDTO));

        mockMvc.perform(get("/menu-items/testId123")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("testId123"))
                .andExpect(jsonPath("$.name").value("Test Pizza"));
    }

    @Test
    void getMenuItemById_whenItemDoesNotExist_shouldReturnNotFound() throws Exception {
        when(menuItemService.getMenuItemById("nonExistentId")).thenReturn(Optional.empty());

        mockMvc.perform(get("/menu-items/nonExistentId")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateMenuItem_whenItemExists_shouldReturnUpdatedItem() throws Exception {
        MenuItemResponseDTO updatedResponseDTO = new MenuItemResponseDTO();
        updatedResponseDTO.setId("testId123");
        updatedResponseDTO.setName("Updated Test Pizza");
        updatedResponseDTO.setDescription("Even more delicious");
        updatedResponseDTO.setPrice(12.99);
        updatedResponseDTO.setCreatedAt(menuItemResponseDTO.getCreatedAt()); // CreatedAt should not change
        updatedResponseDTO.setUpdatedAt(LocalDateTime.now());


        MenuItemRequestDTO updateRequest = new MenuItemRequestDTO();
        updateRequest.setName("Updated Test Pizza");
        updateRequest.setDescription("Even more delicious");
        updateRequest.setPrice(12.99);


        when(menuItemService.updateMenuItem(eq("testId123"), any(MenuItemRequestDTO.class)))
                .thenReturn(Optional.of(updatedResponseDTO));

        mockMvc.perform(put("/menu-items/testId123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Test Pizza"))
                .andExpect(jsonPath("$.price").value(12.99));
    }

     @Test
    void deleteMenuItem_whenItemExists_shouldReturnOk() throws Exception {
        when(menuItemService.deleteMenuItem("testId123")).thenReturn(true);

        mockMvc.perform(delete("/menu-items/testId123")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Menu item deleted successfully"))
                .andExpect(jsonPath("$.id").value("testId123"));
    }

    @Test
    void deleteMenuItem_whenItemDoesNotExist_shouldReturnNotFound() throws Exception {
        when(menuItemService.deleteMenuItem("nonExistentId")).thenReturn(false);

        mockMvc.perform(delete("/menu-items/nonExistentId")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}
