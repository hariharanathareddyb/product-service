package com.example.productservice;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private static final List<Product> PRODUCTS = List.of(
            new Product(1L, "Laptop", new BigDecimal("899.99")),
            new Product(2L, "Headphones", new BigDecimal("79.50")),
            new Product(3L, "Keyboard", new BigDecimal("45.00"))
    );

    @GetMapping
    public List<Product> findAll() {
        return PRODUCTS;
    }

    @GetMapping("/{id}")
    public Product findById(@PathVariable long id) {
        return PRODUCTS.stream()
                .filter(product -> product.id() == id)
                .findFirst()
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    public record Product(long id, String name, BigDecimal price) {}
}
