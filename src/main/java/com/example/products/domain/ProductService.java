package com.example.products.domain;

import com.example.products.entities.Product;
import com.example.products.repositories.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {
    private final ProductRepository productRepository;

    @Autowired
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Product createProduct(Product product) {
        product.setActive(true);
        product.setCreatedAt(Instant.now());
        return productRepository.save(product);
    }

    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    public boolean deleteProduct(Long id) {
        Product product = productRepository.findById(id).orElse(null);
        if (product == null) {
            return false;
        }
        if (Boolean.TRUE.equals(product.getActive())) {
            throw new IllegalStateException("Cannot delete active product");
        }
        productRepository.deleteById(id);
        return true;
    }

    public Optional<Product> updateProduct(Long id, Product updatedProduct) {
        return productRepository.findById(id)
                .map(existingProduct -> {
                    existingProduct.setName(updatedProduct.getName());
                    existingProduct.setDescription(updatedProduct.getDescription());
                    existingProduct.setSku(updatedProduct.getSku());
                    existingProduct.setPrice(updatedProduct.getPrice());
                    existingProduct.setActive(updatedProduct.getActive());
                    existingProduct.setCategory(updatedProduct.getCategory());
                    existingProduct.setUpdatedAt(Instant.now());
                    return productRepository.save(existingProduct);
                });
    }

    public Optional<Product> deactivateProduct(Long id) {
        return productRepository.findById(id)
                .map(existingProduct -> {
                    existingProduct.setActive(false);
                    existingProduct.setUpdatedAt(Instant.now());
                    return productRepository.save(existingProduct);
                });
    }
}

