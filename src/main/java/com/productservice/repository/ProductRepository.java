package com.productservice.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.productservice.entity.Product;

public interface ProductRepository extends JpaRepository<Product, UUID> {

    @Query("""
            SELECT p
            FROM Product p
            WHERE p.active = true
              AND (
                    LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))
                  )
            ORDER BY p.name ASC
            """)
    List<Product> searchActiveProducts(
            @Param("keyword") String keyword);

    @Query("""
            SELECT p
            FROM Product p
            WHERE p.active = true
              AND LOWER(p.category) = LOWER(:category)
            ORDER BY p.name ASC
            """)
    List<Product> findActiveProductsByCategory(
            @Param("category") String category);

    @Modifying
    @Query("""
            UPDATE Product p
            SET p.stock = p.stock - :quantity
            WHERE p.id = :productId
              AND p.active = true
              AND p.stock >= :quantity
            """)
    int decreaseStockIfAvailable(
            @Param("productId") UUID productId,
            @Param("quantity") int quantity);
    
    @Modifying
    @Query("""
            UPDATE Product p
            SET p.stock = p.stock + :quantity
            WHERE p.id = :productId
              AND p.active = true
            """)
    int increaseStock(
            @Param("productId") UUID productId,
            @Param("quantity") int quantity);
}
