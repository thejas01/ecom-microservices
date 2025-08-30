package com.ecommerce.product.service;

import com.ecommerce.common.dto.product.ProductDTO;
import com.ecommerce.common.utils.exception.BusinessException;
import com.ecommerce.common.utils.exception.ResourceNotFoundException;
import com.ecommerce.product.entity.Category;
import com.ecommerce.product.entity.Product;
import com.ecommerce.product.repository.CategoryRepository;
import com.ecommerce.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ProductService {
    
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    
    @Transactional
    public ProductDTO createProduct(ProductDTO productDTO) {
        log.debug("Creating product: {}", productDTO.getName());
        
        if (productRepository.existsBySku(productDTO.getSku())) {
            throw new BusinessException("PRODUCT_SKU_EXISTS", "Product with SKU '" + productDTO.getSku() + "' already exists");
        }
        
        Product product = mapToEntity(productDTO);
        
        if (productDTO.getCategoryId() != null) {
            Category category = resolveCategoryByIdOrSlug(productDTO.getCategoryId());
            product.setCategory(category);
        }
        
        product = productRepository.save(product);
        log.info("Created product with ID: {}", product.getId());
        
        return mapToDTO(product);
    }
    
    @Transactional
    public ProductDTO updateProduct(String id, ProductDTO productDTO) {
        log.debug("Updating product with ID: {}", id);
        
        Product product = productRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));
        
        if (!product.getSku().equals(productDTO.getSku()) && 
            productRepository.existsBySku(productDTO.getSku())) {
            throw new BusinessException("PRODUCT_SKU_EXISTS", "Product with SKU '" + productDTO.getSku() + "' already exists");
        }
        
        product.setSku(productDTO.getSku());
        product.setName(productDTO.getName());
        product.setDescription(productDTO.getDescription());
        product.setSlug(generateSlug(productDTO.getName()));
        product.setPrice(productDTO.getPrice());
        product.setCompareAtPrice(productDTO.getCompareAtPrice());
        
        if (productDTO.getCategoryId() != null) {
            Category category = resolveCategoryByIdOrSlug(productDTO.getCategoryId());
            product.setCategory(category);
        }
        
        product = productRepository.save(product);
        log.info("Updated product with ID: {}", product.getId());
        
        return mapToDTO(product);
    }
    
    public ProductDTO getProductById(String id) {
        log.debug("Fetching product with ID: {}", id);
        
        Product product = productRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));
        
        return mapToDTO(product);
    }
    
    public ProductDTO getProductBySku(String sku) {
        log.debug("Fetching product with SKU: {}", sku);
        
        Product product = productRepository.findBySku(sku)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with SKU: " + sku));
        
        return mapToDTO(product);
    }
    
    public ProductDTO getProductBySlug(String slug) {
        log.debug("Fetching product with slug: {}", slug);
        
        Product product = productRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with slug: " + slug));
        
        return mapToDTO(product);
    }
    
    public Page<ProductDTO> getAllProducts(Pageable pageable) {
        log.debug("Fetching all products");
        return productRepository.findAll(pageable).map(this::mapToDTO);
    }
    
    public Page<ProductDTO> getActiveProducts(Pageable pageable) {
        log.debug("Fetching active products");
        return productRepository.findByStatus(Product.ProductStatus.ACTIVE, pageable).map(this::mapToDTO);
    }
    
    public Page<ProductDTO> getProductsByCategory(String categoryId, Pageable pageable) {
        log.debug("Fetching products for category ID: {}", categoryId);
        return productRepository.findByCategoryId(UUID.fromString(categoryId), pageable)
                .map(this::mapToDTO);
    }
    
    public Page<ProductDTO> getProductsByBrand(String brand, Pageable pageable) {
        log.debug("Fetching products for brand: {}", brand);
        return productRepository.findByBrand(brand, pageable)
                .map(this::mapToDTO);
    }
    
    public Page<ProductDTO> getProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        log.debug("Fetching products in price range: {} - {}", minPrice, maxPrice);
        return productRepository.findByPriceBetween(minPrice, maxPrice, pageable)
                .map(this::mapToDTO);
    }
    
    public List<ProductDTO> getFeaturedProducts(int limit) {
        log.debug("Fetching featured products, limit: {}", limit);
        return productRepository.findFeaturedProducts(Pageable.ofSize(limit)).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    public Page<ProductDTO> searchProducts(String query, Pageable pageable) {
        log.debug("Searching products with query: {}", query);
        return productRepository.searchProducts(query, pageable).map(this::mapToDTO);
    }
    
    public Page<ProductDTO> getProductsByTags(List<String> tags, Pageable pageable) {
        log.debug("Fetching products with tags: {}", tags);
        return productRepository.findByTags(tags, tags.size(), pageable).map(this::mapToDTO);
    }
    
    public List<String> getAllBrands() {
        log.debug("Fetching all brands");
        return productRepository.findAllBrands();
    }
    
    @Transactional
    public void updateProductStatus(String id, String status) {
        log.debug("Updating product status for ID: {} to {}", id, status);
        
        Product product = productRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));
        
        product.setStatus(Product.ProductStatus.valueOf(status.toUpperCase()));
        productRepository.save(product);
        
        log.info("Updated product status for ID: {} to {}", id, status);
    }
    
    @Transactional
    public void deleteProduct(String id) {
        log.debug("Deleting product with ID: {}", id);
        
        Product product = productRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));
        
        productRepository.delete(product);
        log.info("Deleted product with ID: {}", id);
    }
    
    private ProductDTO mapToDTO(Product product) {
        return ProductDTO.builder()
                .id(product.getId().toString())
                .sku(product.getSku())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .compareAtPrice(product.getCompareAtPrice())
                .categoryId(product.getCategory() != null ? product.getCategory().getId().toString() : null)
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                .active(product.getStatus() == Product.ProductStatus.ACTIVE)
                .status(ProductDTO.ProductStatus.valueOf(product.getStatus().name()))
                .build();
    }
    
    private Product mapToEntity(ProductDTO dto) {
        return Product.builder()
                .sku(dto.getSku())
                .name(dto.getName())
                .description(dto.getDescription())
                .slug(generateSlug(dto.getName()))
                .price(dto.getPrice())
                .compareAtPrice(dto.getCompareAtPrice())
                .status(Product.ProductStatus.DRAFT)
                .build();
    }
    
    private String generateSlug(String name) {
        return name.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
    }
    
    private Category resolveCategoryByIdOrSlug(String identifier) {
        if (identifier == null || identifier.trim().isEmpty()) {
            throw new BusinessException("INVALID_CATEGORY", "Category identifier cannot be null or empty");
        }
        
        // First, try to parse as UUID
        try {
            UUID categoryId = UUID.fromString(identifier);
            return categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Category not found with ID: " + identifier));
        } catch (IllegalArgumentException e) {
            // Not a valid UUID, try to find by slug
            log.debug("Category identifier '{}' is not a valid UUID, searching by slug", identifier);
            return categoryRepository.findBySlug(identifier)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Category not found with slug: " + identifier + 
                            ". Please use a valid UUID or an existing category slug."));
        }
    }
}