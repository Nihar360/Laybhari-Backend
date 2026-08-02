package com.laybhari.controller;

import com.laybhari.dto.InventoryItemDto;
import com.laybhari.service.AdminInventoryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminInventoryController {

    private final AdminInventoryService adminInventoryService;

    public AdminInventoryController(AdminInventoryService adminInventoryService) {
        this.adminInventoryService = adminInventoryService;
    }

    // GET /api/admin/inventory?page=0&size=50&lowStockOnly=false (ADMIN only)
    @GetMapping("/inventory")
    public ResponseEntity<Page<InventoryItemDto>> getInventory(
            @RequestParam(name = "lowStockOnly", required = false, defaultValue = "false") Boolean lowStockOnly,
            Pageable pageable) {
        return ResponseEntity.ok(adminInventoryService.getInventory(lowStockOnly, pageable));
    }

    // PUT /api/admin/variants/{variantId}/stock body: { "stock": 25 } (ADMIN only)
    @PutMapping("/variants/{variantId}/stock")
    public ResponseEntity<InventoryItemDto> updateVariantStock(
            @PathVariable Long variantId,
            @RequestBody Map<String, Integer> body) {
        Integer stock = body != null ? body.get("stock") : null;
        return ResponseEntity.ok(adminInventoryService.updateVariantStock(variantId, stock));
    }
}
