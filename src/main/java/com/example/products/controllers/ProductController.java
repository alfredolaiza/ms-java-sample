package com.example.products.controllers;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.products.entities.Product;
import com.example.products.repositories.ProductRepository;

import java.net.URI;
import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    // TODO: Implement CRUD endpoints for Product entity

    private final ProductRepository productRepository;
    public ProductController(ProductRepository productRepository) {
        this.productRepository=productRepository;
    }

    @GetMapping
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    package com.example.products.controllers;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.products.entities.Product;
import com.example.products.repositories.ProductRepository;

import java.net.URI;
import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    // TODO: Implement CRUD endpoints for Product entity

    private final ProductRepository productRepository;
    public ProductController(ProductRepository productRepository) {
        this.productRepository=productRepository;
    }

    @GetMapping
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @PostMapping
    public ResponseEntity<Product> createProduct(@Valid @RequestBody Product product, UriComponentsBuilder uriBuilder) {
        product.setActive(true);
        product.setCreatedAt(Instant.now());
        
        Product saved = productRepository.save(product);
        URI location = uriBuilder.path("/api/products/{id}").buildAndExpand(saved.getId()).toUri();
        return ResponseEntity.created(location).body(saved);
    }


}
