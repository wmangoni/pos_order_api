package com.allo.menuservice.service;

import com.allo.menuservice.dto.MenuItemRequestDTO;
import com.allo.menuservice.dto.MenuItemResponseDTO;
import com.allo.menuservice.dto.PaginatedMenuItemResponseDTO;
import com.allo.menuservice.model.MenuItem;
import com.allo.menuservice.repository.MenuItemRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MenuItemService {

    private final MenuItemRepository menuItemRepository;

    public MenuItemService(MenuItemRepository menuItemRepository) {
        this.menuItemRepository = menuItemRepository;
    }

    @Transactional
    public MenuItemResponseDTO createMenuItem(MenuItemRequestDTO requestDTO) {
        MenuItem menuItem = new MenuItem();
        BeanUtils.copyProperties(requestDTO, menuItem);
        menuItem.setCreatedAt(LocalDateTime.now());
        MenuItem savedItem = menuItemRepository.save(menuItem);
        return convertToResponseDTO(savedItem);
    }

    @Transactional
    public Optional<MenuItemResponseDTO> updateMenuItem(String id, MenuItemRequestDTO requestDTO) {
        Optional<MenuItem> existingItemOptional = menuItemRepository.findById(id);
        if (existingItemOptional.isPresent()) {
            MenuItem existingItem = existingItemOptional.get();
            existingItem.setName(requestDTO.getName());
            existingItem.setDescription(requestDTO.getDescription());
            existingItem.setPrice(requestDTO.getPrice());
            existingItem.setUpdatedAt(LocalDateTime.now());
            MenuItem updatedItem = menuItemRepository.save(existingItem);
            return Optional.of(convertToResponseDTO(updatedItem));
        }
        return Optional.empty(); // Item not found
    }

    @Transactional
    public boolean deleteMenuItem(String id) {
        if (menuItemRepository.existsById(id)) {
            menuItemRepository.deleteById(id);
            return true;
        }
        return false; // Item not found
    }

    public PaginatedMenuItemResponseDTO getAllMenuItems(int limit, int offset) {
        if (limit <= 0) limit = 10; // Default limit
        if (offset < 0) offset = 0; // Default offset

        Pageable pageable = PageRequest.of(offset / limit, limit);
        Page<MenuItem> menuItemPage = menuItemRepository.findAll(pageable);

        List<MenuItemResponseDTO> dtoList = menuItemPage.getContent().stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());

        return new PaginatedMenuItemResponseDTO(dtoList, menuItemPage.getTotalElements());
    }

    public Optional<MenuItemResponseDTO> getMenuItemById(String id) {
        return menuItemRepository.findById(id).map(this::convertToResponseDTO);
    }

    private MenuItemResponseDTO convertToResponseDTO(MenuItem menuItem) {
        MenuItemResponseDTO dto = new MenuItemResponseDTO();
        BeanUtils.copyProperties(menuItem, dto);
        return dto;
    }
}