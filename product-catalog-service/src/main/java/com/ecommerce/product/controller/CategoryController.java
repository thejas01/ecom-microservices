package com.ecommerce.product.controller;

import com.ecommerce.common.dto.product.CategoryDTO;
import com.ecommerce.product.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Slf4j
public class CategoryController {
    
    private final CategoryService categoryService;
    
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<CategoryDTO> createCategory(@Valid @RequestBody CategoryDTO categoryDTO) {
        log.info("Creating category: {}", categoryDTO.getName());
        CategoryDTO createdCategory = categoryService.createCategory(categoryDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCategory);
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<CategoryDTO> updateCategory(
            @PathVariable String id,
            @Valid @RequestBody CategoryDTO categoryDTO) {
        log.info("Updating category with ID: {}", id);
        CategoryDTO updatedCategory = categoryService.updateCategory(id, categoryDTO);
        return ResponseEntity.ok(updatedCategory);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<CategoryDTO> getCategoryById(@PathVariable String id) {
        log.info("Fetching category with ID: {}", id);
        CategoryDTO category = categoryService.getCategoryById(id);
        return ResponseEntity.ok(category);
    }
    
    @GetMapping("/slug/{slug}")
    public ResponseEntity<CategoryDTO> getCategoryBySlug(@PathVariable String slug) {
        log.info("Fetching category with slug: {}", slug);
        CategoryDTO category = categoryService.getCategoryBySlug(slug);
        return ResponseEntity.ok(category);
    }
    
    @GetMapping
    public ResponseEntity<Page<CategoryDTO>> getAllCategories(
            @PageableDefault(size = 20, sort = "displayOrder", direction = Sort.Direction.ASC) Pageable pageable,
            @RequestParam(required = false, defaultValue = "true") boolean activeOnly) {
        log.info("Fetching categories, page: {}, size: {}, activeOnly: {}", 
                pageable.getPageNumber(), pageable.getPageSize(), activeOnly);
        
        Page<CategoryDTO> categories = activeOnly 
                ? categoryService.getActiveCategories(pageable)
                : categoryService.getAllCategories(pageable);
        
        return ResponseEntity.ok(categories);
    }
    
    @GetMapping("/root")
    public ResponseEntity<List<CategoryDTO>> getRootCategories() {
        log.info("Fetching root categories");
        List<CategoryDTO> categories = categoryService.getRootCategories();
        return ResponseEntity.ok(categories);
    }
    
    @GetMapping("/{parentId}/subcategories")
    public ResponseEntity<List<CategoryDTO>> getSubCategories(@PathVariable String parentId) {
        log.info("Fetching subcategories for parent ID: {}", parentId);
        List<CategoryDTO> categories = categoryService.getSubCategories(parentId);
        return ResponseEntity.ok(categories);
    }
    
    @GetMapping("/search")
    public ResponseEntity<Page<CategoryDTO>> searchCategories(
            @RequestParam String query,
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("Searching categories with query: {}", query);
        Page<CategoryDTO> categories = categoryService.searchCategories(query, pageable);
        return ResponseEntity.ok(categories);
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCategory(@PathVariable String id) {
        log.info("Deleting category with ID: {}", id);
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}