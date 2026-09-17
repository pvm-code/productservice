package com.productservice.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.productservice.dto.ApiResponse;
import com.productservice.service.ProductService;

@RestController
@RequestMapping("/internal/inventory")
public class InternalInventoryController {

    private final ProductService productService;

    public InternalInventoryController(ProductService productService) {
        this.productService = productService;
    }

    @PutMapping("/{id}/reserve")
    public ResponseEntity<ApiResponse<Void>> decreaseStock(
            @PathVariable UUID id,
            @RequestParam int quantity) {

        productService.decreaseStock(id, quantity);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Stock decreased successfully",
                        null
                )
        );
    }

    @PutMapping("/{id}/release")
    public ResponseEntity<ApiResponse<Void>> increaseStock(
            @PathVariable UUID id,
            @RequestParam int quantity) {

        productService.increaseStock(id, quantity);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Stock released successfully",
                        null
                )
        );
    }
}