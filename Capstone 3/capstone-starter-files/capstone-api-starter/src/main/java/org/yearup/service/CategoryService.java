package org.yearup.service;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.yearup.models.Category;
import org.yearup.repository.CategoryRepository;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.yearup.models.Product;

import org.yearup.service.ProductService;


@Service
public class CategoryService
{
    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository)
    {
        this.categoryRepository = categoryRepository;
    }

    public List<Category> getAllCategories()
    {
        return categoryRepository.findAll();
    }

    public Category getById(int categoryId)
    {
        return categoryRepository.findById(categoryId).orElse(null);
    }

    public Category create(Category category)
    {
        category.setCategoryId(0);
        return categoryRepository.save(category);
    }

    public Category update(int categoryId, Category category)
    {
        Category existing = categoryRepository.findById(categoryId).orElse(null);
        if (existing == null) return null;

        existing.setName(category.getName());
        existing.setDescription(category.getDescription());
        return categoryRepository.save(existing);
    }

    public void delete(int categoryId)
    {
        categoryRepository.deleteById(categoryId);
    }
}
