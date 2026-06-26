package org.yearup.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.yearup.models.Category;
import org.yearup.models.Product;
import org.yearup.service.CategoryService;
import org.yearup.service.ProductService;

import java.util.List;

@RestController
@RequestMapping("categories")
@CrossOrigin
public class CategoriesController
{
    private final CategoryService categoryService;
    private final ProductService productService;

    public CategoriesController(CategoryService categoryService, ProductService productService)
    {
        this.categoryService = categoryService;
        this.productService = productService;
    }

    @GetMapping("")
    @PreAuthorize("permitAll()")
    public List<Category> getAll()
    {
        return categoryService.getAllCategories();
    }

    @GetMapping("{id}")
    @PreAuthorize("permitAll()")
    public Category getById(@PathVariable int id)
    {
        Category category = categoryService.getById(id);
        if (category == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return category;
    }

    @GetMapping("{categoryId}/products")
    @PreAuthorize("permitAll()")
    public List<Product> getProductsById(@PathVariable int categoryId)
    {
        return productService.listByCategoryId(categoryId);
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Category> addCategory(@RequestBody Category category)
    {
        Category created = categoryService.create(category);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Category updateCategory(@PathVariable int id, @RequestBody Category category)
    {
        if (categoryService.getById(id) == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return categoryService.update(id, category);
    }

    @DeleteMapping("{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteCategory(@PathVariable int id)
    {
        if (categoryService.getById(id) == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        categoryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

