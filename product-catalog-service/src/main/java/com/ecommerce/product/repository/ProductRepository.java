package com.ecommerce.product.repository;

import com.ecommerce.product.entity.Product;
import com.ecommerce.product.entity.Product.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID>, JpaSpecificationExecutor<Product> {
    
    Optional<Product> findBySku(String sku);
    
    Optional<Product> findBySlug(String slug);
    
    boolean existsBySku(String sku);
    
    boolean existsBySlug(String slug);
    
    @Query("SELECT p FROM Product p WHERE p.status = :status ORDER BY p.createdAt DESC")
    Page<Product> findByStatus(@Param("status") ProductStatus status, Pageable pageable);
    
    @Query("SELECT p FROM Product p WHERE p.category.id = :categoryId AND p.status = 'ACTIVE'")
    Page<Product> findByCategoryId(@Param("categoryId") UUID categoryId, Pageable pageable);
    
    @Query("SELECT p FROM Product p WHERE p.brand = :brand AND p.status = 'ACTIVE'")
    Page<Product> findByBrand(@Param("brand") String brand, Pageable pageable);
    
    @Query("SELECT p FROM Product p WHERE p.price BETWEEN :minPrice AND :maxPrice AND p.status = 'ACTIVE'")
    Page<Product> findByPriceBetween(@Param("minPrice") BigDecimal minPrice, @Param("maxPrice") BigDecimal maxPrice, Pageable pageable);
    
    @Query("SELECT p FROM Product p WHERE p.isFeatured = true AND p.status = 'ACTIVE' ORDER BY p.createdAt DESC")
    List<Product> findFeaturedProducts(Pageable pageable);
    
    @Query("SELECT p FROM Product p WHERE " +
           "(LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.brand) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.sku) LIKE LOWER(CONCAT('%', :query, '%'))) " +
           "AND p.status = 'ACTIVE'")
    Page<Product> searchProducts(@Param("query") String query, Pageable pageable);
    
    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.images WHERE p.id = :id")
    Optional<Product> findByIdWithImages(@Param("id") UUID id);
    
    @Query("SELECT p FROM Product p JOIN p.tags t WHERE t IN :tags AND p.status = 'ACTIVE' GROUP BY p HAVING COUNT(DISTINCT t) = :tagCount")
    Page<Product> findByTags(@Param("tags") List<String> tags, @Param("tagCount") long tagCount, Pageable pageable);
    
    @Query("SELECT DISTINCT p.brand FROM Product p WHERE p.brand IS NOT NULL AND p.status = 'ACTIVE' ORDER BY p.brand")
    List<String> findAllBrands();
    
    
    @Query("UPDATE Product p SET p.status = :status WHERE p.id = :id")
    void updateStatus(@Param("id") UUID id, @Param("status") ProductStatus status);
    
    @Query("SELECT COUNT(p) FROM Product p WHERE p.category.id = :categoryId")
    long countByCategoryId(@Param("categoryId") UUID categoryId);
}