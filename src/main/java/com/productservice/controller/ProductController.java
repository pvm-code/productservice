package com.productservice.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.productservice.dto.ApiResponse;
import com.productservice.dto.ProductRequest;
import com.productservice.dto.ProductResponse;
import com.productservice.service.ProductService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
            @Valid @RequestBody ProductRequest request) {

        ProductResponse product =
                productService.createProduct(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ApiResponse<>(
                        true,
                        "Product created successfully",
                        product));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProduct(
            @PathVariable UUID id) {

        ProductResponse product =
                productService.getProductById(id);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Product fetched successfully",
                        product));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getAllProducts() {

        List<ProductResponse> products =
                productService.getAllProducts();

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Products fetched successfully",
                        products));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @PathVariable UUID id,
            @Valid @RequestBody ProductRequest request) {

        ProductResponse product =
                productService.updateProduct(id, request);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Product updated successfully",
                        product));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deactivateProduct(
            @PathVariable UUID id) {

        productService.deactivateProduct(id);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Product deactivated successfully",
                        null));
    }
    
    
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> searchProducts(
            @RequestParam String keyword) {

        List<ProductResponse> products = productService.searchProducts(keyword);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Products searched successfully",
                        products
                )
        );
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getProductsByCategory(
            @PathVariable String category) {

        List<ProductResponse> products =
                productService.getProductsByCategory(category);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Products fetched by category successfully",
                        products
                )
        );
    }
    
}