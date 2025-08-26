package com.ecommerce.inventory.repository;

import com.ecommerce.inventory.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, String> {

    Optional<Inventory> findByProductId(String productId);
    
    List<Inventory> findByProductIdIn(List<String> productIds);
    
    List<Inventory> findByAvailableQuantityLessThanEqualAndActiveTrue(Integer threshold);
    
    List<Inventory> findByActiveTrue();

    @Query("SELECT i FROM Inventory i WHERE i.availableQuantity >= :quantity AND i.productId = :productId AND i.active = true")
    Optional<Inventory> findByProductIdAndAvailableQuantityGreaterThanEqual(@Param("productId") String productId, @Param("quantity") Integer quantity);

    @Modifying
    @Query("UPDATE Inventory i SET i.quantity = i.quantity + :quantity WHERE i.productId = :productId")
    int addStock(@Param("productId") String productId, @Param("quantity") Integer quantity);

    @Modifying
    @Query("UPDATE Inventory i SET i.reservedQuantity = i.reservedQuantity + :quantity WHERE i.productId = :productId")
    int reserveStock(@Param("productId") String productId, @Param("quantity") Integer quantity);

    @Modifying
    @Query("UPDATE Inventory i SET i.reservedQuantity = i.reservedQuantity - :quantity WHERE i.productId = :productId")
    int releaseReservedStock(@Param("productId") String productId, @Param("quantity") Integer quantity);
}