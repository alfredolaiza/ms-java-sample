package com.example.products.domain;

import com.example.products.entities.Product;
import com.example.products.repositories.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

public class ProductServiceTest {

    private ProductRepository productRepository;
    private ProductService productService;

    @BeforeEach
    void setUp() {
        productRepository = Mockito.mock(ProductRepository.class);
        productService = new ProductService(productRepository);
    }

    @Test
    void deleteProduct_shouldThrowException_whenProductIsActive() {
        Product activeProduct = new Product();
        activeProduct.setId(1L);
        activeProduct.setName("Producto activo");
        activeProduct.setPrice(BigDecimal.TEN);
        activeProduct.setActive(true);
        activeProduct.setCreatedAt(Instant.now());

        when(productRepository.findById(1L)).thenReturn(Optional.of(activeProduct));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> productService.deleteProduct(1L));
        assertEquals("Cannot delete active product", ex.getMessage());

        verify(productRepository, never()).deleteById(anyLong());
    }

    @Test
    void deleteProduct_shouldReturnTrue_andDelete_whenProductIsInactive() {
        Product inactiveProduct = new Product();
        inactiveProduct.setId(2L);
        inactiveProduct.setName("Producto inactivo");
        inactiveProduct.setPrice(BigDecimal.TEN);
        inactiveProduct.setActive(false);
        inactiveProduct.setCreatedAt(Instant.now());

        when(productRepository.findById(2L)).thenReturn(Optional.of(inactiveProduct));

        boolean result = productService.deleteProduct(2L);

        assertTrue(result);
        verify(productRepository, times(1)).deleteById(2L);
    }

    @Test
    void deleteProduct_shouldReturnFalse_whenProductDoesNotExist() {
        when(productRepository.findById(anyLong())).thenReturn(Optional.empty());

        boolean result = productService.deleteProduct(99L);

        assertFalse(result);
        verify(productRepository, never()).deleteById(anyLong());
    }

    @Test
    void deleteProduct_shouldReturnFalse_whenIdIsNull() {
        boolean result = productService.deleteProduct(null);
        assertFalse(result, "Si el id es null, no debe intentar borrar y debe devolver false");
        verify(productRepository, never()).findById(anyLong());
        verify(productRepository, never()).deleteById(anyLong());
    }

    @Test
    void deleteProduct_shouldHandleNegativeId_asNotFound() {
        when(productRepository.findById(-1L)).thenReturn(Optional.empty());

        boolean result = productService.deleteProduct(-1L);

        assertFalse(result, "Un id negativo se trata como no encontrado");
        verify(productRepository, times(1)).findById(-1L);
        verify(productRepository, never()).deleteById(anyLong());
    }

    @Test
    void deleteProduct_shouldAllowDelete_whenActiveIsNull() {
        Product product = new Product();
        product.setId(3L);
        product.setName("Producto con active null");
        product.setPrice(BigDecimal.TEN);
        product.setActive(null); // borde: active sin inicializar
        product.setCreatedAt(Instant.now());

        when(productRepository.findById(3L)).thenReturn(Optional.of(product));

        boolean result = productService.deleteProduct(3L);

        assertTrue(result, "Si active es null, se asume que se puede borrar");
        verify(productRepository, times(1)).deleteById(3L);
    }
}
