package com.ecommerce.inventory.controller;

import com.ecommerce.inventory.entity.Inventory;
import com.ecommerce.inventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
@Slf4j
public class InventoryController {

    private final InventoryService inventoryService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Inventory> createInventory(@RequestParam String productId, @RequestParam Integer quantity) {
        log.info("Creating inventory for product: {} with quantity: {}", productId, quantity);
        Inventory inventory = inventoryService.createInventory(productId, quantity);
        return ResponseEntity.status(HttpStatus.CREATED).body(inventory);
    }

    @GetMapping("/{productId}")
    public ResponseEntity<Inventory> getInventory(@PathVariable String productId) {
        log.info("Getting inventory for product: {}", productId);
        Optional<Inventory> inventory = inventoryService.getInventoryByProductId(productId);
        return inventory.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<Inventory>> getAllInventory() {
        log.info("Getting all active inventory");
        List<Inventory> inventories = inventoryService.getAllActiveInventory();
        return ResponseEntity.ok(inventories);
    }

    @GetMapping("/low-stock")
    public ResponseEntity<List<Inventory>> getLowStockItems() {
        log.info("Getting low stock items");
        List<Inventory> lowStockItems = inventoryService.getLowStockItems();
        return ResponseEntity.ok(lowStockItems);
    }

    @PostMapping("/{productId}/add-stock")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'WAREHOUSE')")
    public ResponseEntity<String> addStock(@PathVariable String productId, @RequestParam Integer quantity) {
        log.info("Adding stock for product: {} quantity: {}", productId, quantity);
        inventoryService.addStock(productId, quantity);
        return ResponseEntity.ok("Stock added successfully");
    }

    @PostMapping("/{productId}/reserve")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SYSTEM')")
    public ResponseEntity<Map<String, Boolean>> reserveStock(@PathVariable String productId, @RequestParam Integer quantity) {
        log.info("Reserving stock for product: {} quantity: {}", productId, quantity);
        boolean reserved = inventoryService.reserveStock(productId, quantity);
        return ResponseEntity.ok(Map.of("reserved", reserved));
    }

    @PostMapping("/{productId}/release")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SYSTEM')")
    public ResponseEntity<String> releaseReservedStock(@PathVariable String productId, @RequestParam Integer quantity) {
        log.info("Releasing reserved stock for product: {} quantity: {}", productId, quantity);
        inventoryService.releaseReservedStock(productId, quantity);
        return ResponseEntity.ok("Reserved stock released successfully");
    }

    @PostMapping("/{productId}/confirm")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SYSTEM')")
    public ResponseEntity<String> confirmStockReduction(@PathVariable String productId, @RequestParam Integer quantity) {
        log.info("Confirming stock reduction for product: {} quantity: {}", productId, quantity);
        inventoryService.confirmStockReduction(productId, quantity);
        return ResponseEntity.ok("Stock reduction confirmed");
    }

    @GetMapping("/{productId}/check")
    public ResponseEntity<Map<String, Boolean>> checkStock(@PathVariable String productId, @RequestParam Integer quantity) {
        log.info("Checking stock for product: {} quantity: {}", productId, quantity);
        boolean inStock = inventoryService.isInStock(productId, quantity);
        return ResponseEntity.ok(Map.of("inStock", inStock));
    }

    // Bulk reservation endpoints for order service integration
    @PostMapping("/reserve")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SYSTEM', 'USER')")
    public ResponseEntity<Map<String, Object>> reserveInventoryBulk(@RequestBody Map<String, Object> request) {
        String productId = (String) request.get("productId");
        Integer quantity = (Integer) request.get("quantity");
        String orderId = (String) request.get("orderId");
        
        log.info("Bulk reserving inventory for product: {} quantity: {} order: {}", productId, quantity, orderId);
        
        try {
            boolean reserved = inventoryService.reserveStock(productId, quantity);
            if (reserved) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Inventory reserved successfully",
                    "productId", productId,
                    "quantity", quantity,
                    "orderId", orderId
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Insufficient inventory available",
                    "productId", productId,
                    "quantity", quantity,
                    "orderId", orderId
                ));
            }
        } catch (Exception e) {
            log.error("Error reserving inventory for product: {} quantity: {} order: {}", productId, quantity, orderId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "Error reserving inventory: " + e.getMessage(),
                "productId", productId,
                "quantity", quantity,
                "orderId", orderId
            ));
        }
    }

    @PostMapping("/release")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SYSTEM', 'USER')")
    public ResponseEntity<Map<String, Object>> releaseInventoryBulk(@RequestBody Map<String, Object> request) {
        String productId = (String) request.get("productId");
        Integer quantity = (Integer) request.get("quantity");
        String orderId = (String) request.get("orderId");
        
        log.info("Bulk releasing inventory for product: {} quantity: {} order: {}", productId, quantity, orderId);
        
        try {
            inventoryService.releaseReservedStock(productId, quantity);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Inventory released successfully",
                "productId", productId,
                "quantity", quantity,
                "orderId", orderId
            ));
        } catch (Exception e) {
            log.error("Error releasing inventory for product: {} quantity: {} order: {}", productId, quantity, orderId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "Error releasing inventory: " + e.getMessage(),
                "productId", productId,
                "quantity", quantity,
                "orderId", orderId
            ));
        }
    }

    @PostMapping("/confirm")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SYSTEM', 'USER')")
    public ResponseEntity<Map<String, Object>> confirmInventoryBulk(@RequestBody Map<String, Object> request) {
        String productId = (String) request.get("productId");
        Integer quantity = (Integer) request.get("quantity");
        String orderId = (String) request.get("orderId");
        
        log.info("Bulk confirming inventory for product: {} quantity: {} order: {}", productId, quantity, orderId);
        
        try {
            inventoryService.confirmStockReduction(productId, quantity);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Inventory confirmed successfully",
                "productId", productId,
                "quantity", quantity,
                "orderId", orderId
            ));
        } catch (Exception e) {
            log.error("Error confirming inventory for product: {} quantity: {} order: {}", productId, quantity, orderId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "Error confirming inventory: " + e.getMessage(),
                "productId", productId,
                "quantity", quantity,
                "orderId", orderId
            ));
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Inventory service is healthy");
    }
}