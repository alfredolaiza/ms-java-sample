package com.example.products.controllers;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.products.entities.Category;
import com.example.products.repositories.CategoryRepository;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {
	private final CategoryRepository categoryRepository;

	public CategoryController(CategoryRepository categoryRepository) {
		this.categoryRepository = categoryRepository;
	}

	@GetMapping
	public List<Category> getAllCategories() {
		return categoryRepository.findAll();
	}

	@PostMapping
	public ResponseEntity<Category> createCategory(@Valid @RequestBody Category category, UriComponentsBuilder uriBuilder) {
		Category saved = categoryRepository.save(category);
		URI location = uriBuilder.path("/api/categories/{id}").buildAndExpand(saved.getId()).toUri();
		return ResponseEntity.created(location).body(saved);
	}

	@GetMapping("/{id}")
	public ResponseEntity<Category> getCategoryById(@PathVariable Long id) {
		return categoryRepository.findById(id)
				.map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
		if (!categoryRepository.existsById(id)) {
			return ResponseEntity.notFound().build();
		}
		categoryRepository.deleteById(id);
		return ResponseEntity.noContent().build();
	}

	@PutMapping("/{id}")
	public ResponseEntity<Category> updateCategory(@PathVariable Long id, @Valid @RequestBody Category updatedCategory) {
		return categoryRepository.findById(id)
				.map(existingCategory -> {
					existingCategory.setName(updatedCategory.getName());
					Category saved = categoryRepository.save(existingCategory);
					return ResponseEntity.ok(saved);
				})
				.orElse(ResponseEntity.notFound().build());
	}
}
