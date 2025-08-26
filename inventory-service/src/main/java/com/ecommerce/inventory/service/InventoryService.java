package com.ecommerce.inventory.service;

import com.ecommerce.inventory.entity.Inventory;
import com.ecommerce.inventory.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    public Inventory createInventory(String productId, Integer quantity) {
        log.info("Creating inventory for product: {} with quantity: {}", productId, quantity);
        
        if (inventoryRepository.findByProductId(productId).isPresent()) {
            throw new IllegalArgumentException("Inventory already exists for product: " + productId);
        }

        Inventory inventory = Inventory.builder()
                .productId(productId)
                .quantity(quantity)
                .reservedQuantity(0)
                .availableQuantity(quantity)
                .active(true)
                .build();

        return inventoryRepository.save(inventory);
    }

    @Transactional(readOnly = true)
    public Optional<Inventory> getInventoryByProductId(String productId) {
        log.debug("Getting inventory for product: {}", productId);
        return inventoryRepository.findByProductId(productId);
    }

    @Transactional(readOnly = true)
    public List<Inventory> getAllActiveInventory() {
        log.debug("Getting all active inventory");
        return inventoryRepository.findByActiveTrue();
    }

    @Transactional(readOnly = true)
    public List<Inventory> getLowStockItems() {
        log.debug("Getting low stock items");
        return inventoryRepository.findByAvailableQuantityLessThanEqualAndActiveTrue(10);
    }

    public void addStock(String productId, Integer quantity) {
        log.info("Adding stock for product: {} quantity: {}", productId, quantity);
        
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new IllegalArgumentException("Inventory not found for product: " + productId));
        
        inventory.setQuantity(inventory.getQuantity() + quantity);
        inventoryRepository.save(inventory);
    }

    public boolean reserveStock(String productId, Integer quantity) {
        log.info("Reserving stock for product: {} quantity: {}", productId, quantity);
        
        Optional<Inventory> inventoryOpt = inventoryRepository
                .findByProductIdAndAvailableQuantityGreaterThanEqual(productId, quantity);
        
        if (inventoryOpt.isPresent()) {
            Inventory inventory = inventoryOpt.get();
            inventory.setReservedQuantity(inventory.getReservedQuantity() + quantity);
            inventoryRepository.save(inventory);
            log.info("Stock reserved successfully for product: {}", productId);
            return true;
        }
        
        log.warn("Insufficient stock for product: {} requested: {}", productId, quantity);
        return false;
    }

    public void releaseReservedStock(String productId, Integer quantity) {
        log.info("Releasing reserved stock for product: {} quantity: {}", productId, quantity);
        
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new IllegalArgumentException("Inventory not found for product: " + productId));
        
        inventory.setReservedQuantity(Math.max(0, inventory.getReservedQuantity() - quantity));
        inventoryRepository.save(inventory);
    }

    public void confirmStockReduction(String productId, Integer quantity) {
        log.info("Confirming stock reduction for product: {} quantity: {}", productId, quantity);
        
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new IllegalArgumentException("Inventory not found for product: " + productId));
        
        inventory.setQuantity(inventory.getQuantity() - quantity);
        inventory.setReservedQuantity(Math.max(0, inventory.getReservedQuantity() - quantity));
        inventoryRepository.save(inventory);
    }

    @Transactional(readOnly = true)
    public boolean isInStock(String productId, Integer quantity) {
        log.debug("Checking stock availability for product: {} quantity: {}", productId, quantity);
        return inventoryRepository.findByProductIdAndAvailableQuantityGreaterThanEqual(productId, quantity).isPresent();
    }
}