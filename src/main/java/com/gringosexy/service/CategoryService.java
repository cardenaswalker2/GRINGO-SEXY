package com.gringosexy.service;

import com.gringosexy.enums.ContentCategory;
import com.gringosexy.model.Category;

import java.util.List;

public interface CategoryService {

    List<Category> getAllCategories();

    List<Category> getActiveCategories();

    Category getBySlug(String slug);

    Category getByCode(ContentCategory code);

    Category getById(String id);

    Category saveCategory(Category category);

    Category createOrUpdateCategory(String id, String name, String slug, String description, String iconClass, int sortOrder, boolean active);

    void deleteCategory(String id);

    Category toggleActive(String id);

    void initDefaultCategories();
}
