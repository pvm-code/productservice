package com.productservice.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.productservice.dto.ProductRequest;
import com.productservice.dto.ProductResponse;
import com.productservice.entity.Product;
import com.productservice.exception.InsufficientStockException;
import com.productservice.exception.ProductNotFoundException;
import com.productservice.repository.ProductRepository;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional
    public ProductResponse createProduct(ProductRequest request) {

        Product product = new Product();

        product.setName(request.getName().trim());
        product.setDescription(request.getDescription());
        product.setCategory(request.getCategory().trim());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        product.setActive(true);

        Product savedProduct = productRepository.save(product);

        return mapToResponse(savedProduct);
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductById(UUID id) {

        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new ProductNotFoundException(
                                "Product not found: " + id));

        return mapToResponse(product);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getAllProducts() {

        return productRepository.findAll()
                .stream()
                .filter(Product::isActive)
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public ProductResponse updateProduct(
            UUID id,
            ProductRequest request) {

        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new ProductNotFoundException(
                                "Product not found: " + id));

        product.setName(request.getName().trim());
        product.setDescription(request.getDescription());
        product.setCategory(request.getCategory().trim());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());

        Product updatedProduct = productRepository.save(product);

        return mapToResponse(updatedProduct);
    }
    @Transactional
    public void decreaseStock(UUID productId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }

        if (!productRepository.existsById(productId)) {
            throw new ProductNotFoundException(
                    "Product not found: " + productId
            );
        }

        int updatedRows =
                productRepository.decreaseStockIfAvailable(productId, quantity);

        if (updatedRows == 0) {
            throw new InsufficientStockException(
                    "Insufficient stock for product: " + productId
            );
        }
    }
    @Transactional
    public void increaseStock(UUID productId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }

        if (!productRepository.existsById(productId)) {
            throw new ProductNotFoundException(
                    "Product not found: " + productId
            );
        }

        int updatedRows =
                productRepository.increaseStock(productId, quantity);

        if (updatedRows == 0) {
            throw new ProductNotFoundException(
                    "Product is inactive or cannot be updated: " + productId
            );
        }
    }
    @Transactional
    public void deactivateProduct(UUID id) {

        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new ProductNotFoundException(
                                "Product not found: " + id));

        product.setActive(false);

        productRepository.save(product);
    }
    @Transactional(readOnly = true)
    public List<ProductResponse> searchProducts(String keyword) {
        String normalizedKeyword = keyword == null ? "" : keyword.trim();

        if (normalizedKeyword.isBlank()) {
            return getAllProducts();
        }

        return productRepository.searchActiveProducts(normalizedKeyword)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getProductsByCategory(String category) {
        String normalizedCategory = category == null ? "" : category.trim();

        if (normalizedCategory.isBlank()) {
            return List.of();
        }

        return productRepository.findActiveProductsByCategory(normalizedCategory)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }
    private ProductResponse mapToResponse(Product product) {

        ProductResponse response = new ProductResponse();

        response.setId(product.getId());
        response.setName(product.getName());
        response.setDescription(product.getDescription());
        response.setCategory(product.getCategory());
        response.setPrice(product.getPrice());
        response.setStock(product.getStock());
        response.setActive(product.isActive());
        response.setCreatedAt(product.getCreatedAt());
        response.setUpdatedAt(product.getUpdatedAt());

        return response;
    }
}