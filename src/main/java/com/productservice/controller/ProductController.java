package com.productservice.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.productservice.dto.ApiResponse;
import com.productservice.dto.ProductRequest;
import com.productservice.dto.ProductResponse;
import com.productservice.service.ProductService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("api/v1/products")
public class ProductController {
	
	
	private final ProductService productService;

	public ProductController(ProductService productService) {
		
		this.productService = productService;
	}
	
	
	@PostMapping
	public ResponseEntity<ApiResponse<ProductResponse>> createProduct(@Valid @RequestBody 
					ProductRequest request){
		
	      ProductResponse product  = productService.createProduct(request);
		
		ApiResponse<ProductResponse> response = new ApiResponse<>(
				
				true,
				"prodcut created successfully",
				product
				
				
				);
		
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
		
		
	}

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductById(
            @PathVariable UUID id) {

        ProductResponse product = productService.getProductById(id);

        ApiResponse<ProductResponse> response = new ApiResponse<>(
                true,
                "Product fetched successfully",
                product
        );

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getAllProducts() {

        List<ProductResponse> products = productService.getAllProducts();

        ApiResponse<List<ProductResponse>> response = new ApiResponse<>(
                true,
                "Products fetched successfully",
                products
        );

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }
	

}
