package com.ecommerce.product.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "products", indexes = {
        @Index(name = "idx_product_sku", columnList = "sku"),
        @Index(name = "idx_product_slug", columnList = "slug"),
        @Index(name = "idx_product_category", columnList = "category_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(nullable = false, unique = true, length = 50)
    private String sku;
    
    @Column(nullable = false, length = 200)
    private String name;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "short_description", length = 500)
    private String shortDescription;
    
    @Column(name = "slug", nullable = false, unique = true, length = 200)
    private String slug;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
    
    @Column(name = "compare_at_price", precision = 10, scale = 2)
    private BigDecimal compareAtPrice;
    
    @Column(name = "cost_price", precision = 10, scale = 2)
    private BigDecimal costPrice;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;
    
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductImage> images = new ArrayList<>();
    
    @Column(name = "brand", length = 100)
    private String brand;
    
    @Column(name = "weight")
    private Double weight;
    
    @Column(name = "length")
    private Double length;
    
    @Column(name = "width")
    private Double width;
    
    @Column(name = "height")
    private Double height;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ProductStatus status = ProductStatus.DRAFT;
    
    @Column(name = "is_featured")
    private Boolean isFeatured = false;
    
    @Column(name = "meta_title", length = 200)
    private String metaTitle;
    
    @Column(name = "meta_description", length = 500)
    private String metaDescription;
    
    @Column(name = "meta_keywords", length = 500)
    private String metaKeywords;
    
    @ElementCollection
    @CollectionTable(name = "product_tags", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "tag")
    private List<String> tags = new ArrayList<>();
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    @PreUpdate
    private void generateSlug() {
        if (this.slug == null || this.slug.isEmpty()) {
            this.slug = this.name.toLowerCase()
                    .replaceAll("[^a-z0-9]+", "-")
                    .replaceAll("^-|-$", "");
        }
    }
    
    public enum ProductStatus {
        DRAFT,
        ACTIVE,
        INACTIVE,
        OUT_OF_STOCK,
        DISCONTINUED
    }
}