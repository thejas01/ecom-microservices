package com.ecommerce.product.controller;

import com.ecommerce.common.dto.product.ProductDTO;
import com.ecommerce.product.service.ProductService;
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


import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController {
    
    private final ProductService productService;
    
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ProductDTO> createProduct(@Valid @RequestBody ProductDTO productDTO) {
        log.info("Creating product: {}", productDTO.getName());
        ProductDTO createdProduct = productService.createProduct(productDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProduct);
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ProductDTO> updateProduct(
            @PathVariable String id,
            @Valid @RequestBody ProductDTO productDTO) {
        log.info("Updating product with ID: {}", id);
        ProductDTO updatedProduct = productService.updateProduct(id, productDTO);
        return ResponseEntity.ok(updatedProduct);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable String id) {
        log.info("Fetching product with ID: {}", id);
        ProductDTO product = productService.getProductById(id);
        return ResponseEntity.ok(product);
    }
    
    @GetMapping("/sku/{sku}")
    public ResponseEntity<ProductDTO> getProductBySku(@PathVariable String sku) {
        log.info("Fetching product with SKU: {}", sku);
        ProductDTO product = productService.getProductBySku(sku);
        return ResponseEntity.ok(product);
    }
    
    @GetMapping("/slug/{slug}")
    public ResponseEntity<ProductDTO> getProductBySlug(@PathVariable String slug) {
        log.info("Fetching product with slug: {}", slug);
        ProductDTO product = productService.getProductBySlug(slug);
        return ResponseEntity.ok(product);
    }
    
    @GetMapping
    public ResponseEntity<Page<ProductDTO>> getAllProducts(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false, defaultValue = "true") boolean activeOnly) {
        log.info("Fetching products, page: {}, size: {}, activeOnly: {}", 
                pageable.getPageNumber(), pageable.getPageSize(), activeOnly);
        
        Page<ProductDTO> products = activeOnly 
                ? productService.getActiveProducts(pageable)
                : productService.getAllProducts(pageable);
        
        return ResponseEntity.ok(products);
    }
    
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<Page<ProductDTO>> getProductsByCategory(
            @PathVariable String categoryId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("Fetching products for category ID: {}", categoryId);
        Page<ProductDTO> products = productService.getProductsByCategory(categoryId, pageable);
        return ResponseEntity.ok(products);
    }
    
    @GetMapping("/brand/{brand}")
    public ResponseEntity<Page<ProductDTO>> getProductsByBrand(
            @PathVariable String brand,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("Fetching products for brand: {}", brand);
        Page<ProductDTO> products = productService.getProductsByBrand(brand, pageable);
        return ResponseEntity.ok(products);
    }
    
    @GetMapping("/price-range")
    public ResponseEntity<Page<ProductDTO>> getProductsByPriceRange(
            @RequestParam BigDecimal minPrice,
            @RequestParam BigDecimal maxPrice,
            @PageableDefault(size = 20, sort = "price", direction = Sort.Direction.ASC) Pageable pageable) {
        log.info("Fetching products in price range: {} - {}", minPrice, maxPrice);
        Page<ProductDTO> products = productService.getProductsByPriceRange(minPrice, maxPrice, pageable);
        return ResponseEntity.ok(products);
    }
    
    @GetMapping("/featured")
    public ResponseEntity<List<ProductDTO>> getFeaturedProducts(
            @RequestParam(defaultValue = "10") int limit) {
        log.info("Fetching featured products, limit: {}", limit);
        List<ProductDTO> products = productService.getFeaturedProducts(limit);
        return ResponseEntity.ok(products);
    }
    
    @GetMapping("/search")
    public ResponseEntity<Page<ProductDTO>> searchProducts(
            @RequestParam String query,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("Searching products with query: {}", query);
        Page<ProductDTO> products = productService.searchProducts(query, pageable);
        return ResponseEntity.ok(products);
    }
    
    @GetMapping("/tags")
    public ResponseEntity<Page<ProductDTO>> getProductsByTags(
            @RequestParam List<String> tags,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("Fetching products with tags: {}", tags);
        Page<ProductDTO> products = productService.getProductsByTags(tags, pageable);
        return ResponseEntity.ok(products);
    }
    
    @GetMapping("/brands")
    public ResponseEntity<List<String>> getAllBrands() {
        log.info("Fetching all brands");
        List<String> brands = productService.getAllBrands();
        return ResponseEntity.ok(brands);
    }
    
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Void> updateProductStatus(
            @PathVariable String id,
            @RequestParam String status) {
        log.info("Updating product status for ID: {} to {}", id, status);
        productService.updateProductStatus(id, status);
        return ResponseEntity.ok().build();
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteProduct(@PathVariable String id) {
        log.info("Deleting product with ID: {}", id);
        productService.deleteProduct(id);
        return ResponseEntity.ok().build();
    }
}