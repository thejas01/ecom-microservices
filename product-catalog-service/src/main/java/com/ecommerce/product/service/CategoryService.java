package com.ecommerce.product.service;

import com.ecommerce.common.dto.product.CategoryDTO;
import com.ecommerce.common.utils.exception.BusinessException;
import com.ecommerce.common.utils.exception.ResourceNotFoundException;
import com.ecommerce.product.entity.Category;
import com.ecommerce.product.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CategoryService {
    
    private final CategoryRepository categoryRepository;
    
    @Transactional
    public CategoryDTO createCategory(CategoryDTO categoryDTO) {
        log.debug("Creating category: {}", categoryDTO.getName());
        
        if (categoryRepository.existsByName(categoryDTO.getName())) {
            throw new BusinessException("CATEGORY_EXISTS", "Category with name '" + categoryDTO.getName() + "' already exists");
        }
        
        Category category = mapToEntity(categoryDTO);
        
        if (categoryDTO.getParentId() != null) {
            Category parent = categoryRepository.findById(UUID.fromString(categoryDTO.getParentId()))
                    .orElseThrow(() -> new ResourceNotFoundException("Parent category not found"));
            category.setParent(parent);
        }
        
        category = categoryRepository.save(category);
        log.info("Created category with ID: {}", category.getId());
        
        return mapToDTO(category);
    }
    
    @Transactional
    public CategoryDTO updateCategory(String id, CategoryDTO categoryDTO) {
        log.debug("Updating category with ID: {}", id);
        
        Category category = categoryRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + id));
        
        if (!category.getName().equals(categoryDTO.getName()) && 
            categoryRepository.existsByName(categoryDTO.getName())) {
            throw new BusinessException("CATEGORY_EXISTS", "Category with name '" + categoryDTO.getName() + "' already exists");
        }
        
        category.setName(categoryDTO.getName());
        category.setSlug(generateSlug(categoryDTO.getName()));
        category.setDescription(categoryDTO.getDescription());
        category.setImageUrl(categoryDTO.getImageUrl());
        category.setDisplayOrder(categoryDTO.getDisplayOrder());
        category.setIsActive(categoryDTO.isActive());
        
        if (categoryDTO.getParentId() != null) {
            Category currentParent = category.getParent();
            String currentParentId = currentParent != null && currentParent.getId() != null 
                    ? currentParent.getId().toString() : null;
            
            if (!categoryDTO.getParentId().equals(currentParentId)) {
                Category parent = categoryRepository.findById(UUID.fromString(categoryDTO.getParentId()))
                        .orElseThrow(() -> new ResourceNotFoundException("Parent category not found"));
                category.setParent(parent);
            }
        } else {
            category.setParent(null);
        }
        
        category = categoryRepository.save(category);
        log.info("Updated category with ID: {}", category.getId());
        
        return mapToDTO(category);
    }
    
    public CategoryDTO getCategoryById(String id) {
        log.debug("Fetching category with ID: {}", id);
        
        Category category = categoryRepository.findByIdWithChildren(UUID.fromString(id))
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + id));
        
        return mapToDTO(category);
    }
    
    public CategoryDTO getCategoryBySlug(String slug) {
        log.debug("Fetching category with slug: {}", slug);
        
        Category category = categoryRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with slug: " + slug));
        
        return mapToDTO(category);
    }
    
    public Page<CategoryDTO> getAllCategories(Pageable pageable) {
        log.debug("Fetching all categories");
        return categoryRepository.findAll(pageable).map(this::mapToDTO);
    }
    
    public Page<CategoryDTO> getActiveCategories(Pageable pageable) {
        log.debug("Fetching active categories");
        return categoryRepository.findAllActive(pageable).map(this::mapToDTO);
    }
    
    public List<CategoryDTO> getRootCategories() {
        log.debug("Fetching root categories");
        return categoryRepository.findRootCategories().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    public List<CategoryDTO> getSubCategories(String parentId) {
        log.debug("Fetching subcategories for parent ID: {}", parentId);
        return categoryRepository.findByParentId(UUID.fromString(parentId)).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    public Page<CategoryDTO> searchCategories(String query, Pageable pageable) {
        log.debug("Searching categories with query: {}", query);
        return categoryRepository.searchCategories(query, pageable).map(this::mapToDTO);
    }
    
    @Transactional
    public void deleteCategory(String id) {
        log.debug("Deleting category with ID: {}", id);
        
        Category category = categoryRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + id));
        
        long productCount = categoryRepository.countProductsByCategoryId(category.getId());
        if (productCount > 0) {
            throw new BusinessException("CATEGORY_HAS_PRODUCTS", "Cannot delete category with existing products. Found " + productCount + " products.");
        }
        
        categoryRepository.delete(category);
        log.info("Deleted category with ID: {}", id);
    }
    
    private CategoryDTO mapToDTO(Category category) {
        return CategoryDTO.builder()
                .id(category.getId().toString())
                .name(category.getName())
                .description(category.getDescription())
                .imageUrl(category.getImageUrl())
                .parentId(category.getParent() != null ? category.getParent().getId().toString() : null)
                .displayOrder(category.getDisplayOrder())
                .active(category.getIsActive())
                .build();
    }
    
    private Category mapToEntity(CategoryDTO dto) {
        return Category.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .slug(generateSlug(dto.getName()))
                .imageUrl(dto.getImageUrl())
                .displayOrder(dto.getDisplayOrder())
                .isActive(dto.isActive())
                .build();
    }
    
    private String generateSlug(String name) {
        return name.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
    }
}