package com.gringosexy.service.impl;

import com.gringosexy.enums.ContentCategory;
import com.gringosexy.exception.ResourceNotFoundException;
import com.gringosexy.model.Category;
import com.gringosexy.repository.CategoryRepository;
import com.gringosexy.service.CategoryService;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public List<Category> getAllCategories() {
        return categoryRepository.findAllByOrderBySortOrderAsc();
    }

    @Override
    public List<Category> getActiveCategories() {
        return categoryRepository.findByActiveTrueOrderBySortOrderAsc();
    }

    @Override
    public Category getBySlug(String slug) {
        return categoryRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada para slug: " + slug));
    }

    @Override
    public Category getByCode(ContentCategory code) {
        return categoryRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada para código: " + code));
    }

    @Override
    public Category getById(String id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con ID: " + id));
    }

    @Override
    public Category saveCategory(Category category) {
        return categoryRepository.save(category);
    }

    @Override
    public Category createOrUpdateCategory(String id, String name, String slug, String description, String iconClass, int sortOrder, boolean active) {
        Category category;
        String cleanSlug = (slug != null && !slug.trim().isEmpty())
                ? ContentServiceImpl.toSlug(slug)
                : ContentServiceImpl.toSlug(name);

        ContentCategory cCode = ContentCategory.fromSlug(cleanSlug);
        if (cCode == null) {
            cCode = ContentCategory.MODIFICATIONS;
        }

        if (id != null && !id.trim().isEmpty()) {
            category = getById(id);
        } else {
            category = new Category();
            category.setCreatedAt(Instant.now());
        }

        category.setName(name.trim());
        category.setSlug(cleanSlug);
        category.setCode(cCode);
        category.setDescription(description != null ? description.trim() : "");
        category.setIconClass(iconClass != null && !iconClass.trim().isEmpty() ? iconClass.trim() : "fas fa-folder");
        category.setSortOrder(sortOrder);
        category.setActive(active);
        category.setUpdatedAt(Instant.now());

        return categoryRepository.save(category);
    }

    @Override
    public void deleteCategory(String id) {
        Category category = getById(id);
        categoryRepository.delete(category);
    }

    @Override
    public Category toggleActive(String id) {
        Category category = getById(id);
        category.setActive(!category.isActive());
        category.setUpdatedAt(Instant.now());
        return categoryRepository.save(category);
    }

    @Override
    public void initDefaultCategories() {
        // Ensure only active defined categories exist and update them
        for (ContentCategory cc : ContentCategory.values()) {
            Category cat = categoryRepository.findBySlug(cc.getSlug()).orElse(null);
            if (cat == null) {
                cat = new Category(cc, cc.getDisplayName(), cc.getSlug(), cc.getDescription(), cc.getIconClass(), cc.getSortOrder());
            } else {
                cat.setCode(cc);
                cat.setName(cc.getDisplayName());
                cat.setDescription(cc.getDescription());
                cat.setIconClass(cc.getIconClass());
                cat.setSortOrder(cc.getSortOrder());
                cat.setActive(true);
            }
            categoryRepository.save(cat);
        }

        // Delete any legacy categories that are no longer in ContentCategory (e.g. PERFORMANCE, BATTERY, etc.)
        List<Category> all = categoryRepository.findAll();
        for (Category existing : all) {
            if (existing.getCode() == null || ContentCategory.fromSlug(existing.getSlug()) == null) {
                categoryRepository.delete(existing);
            }
        }
    }
}
