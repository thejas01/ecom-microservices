package com.ecommerce.common.dto.product;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductDTO {
    
    private String id;
    
    @NotBlank(message = "Product name is required")
    @Size(max = 200, message = "Product name must not exceed 200 characters")
    private String name;
    
    @NotBlank(message = "Product description is required")
    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;
    
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private BigDecimal price;
    
    private BigDecimal compareAtPrice;
    
    @NotBlank(message = "SKU is required")
    @Size(max = 100, message = "SKU must not exceed 100 characters")
    private String sku;
    
    private String barcode;
    
    @NotNull(message = "Category ID is required")
    private String categoryId;
    
    private String categoryName;
    
    private List<String> imageUrls;
    
    private List<String> tags;
    
    private boolean active;
    
    private boolean featured;
    
    private ProductStatus status;
    
    private Integer stockQuantity;
    
    private BigDecimal weight;
    
    private DimensionsDTO dimensions;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    public enum ProductStatus {
        DRAFT, PUBLISHED, ARCHIVED
    }
}