package com.gringosexy.repository;

import com.gringosexy.enums.ContentCategory;
import com.gringosexy.model.Category;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends MongoRepository<Category, String> {

    Optional<Category> findBySlug(String slug);

    Optional<Category> findByCode(ContentCategory code);

    boolean existsByCode(ContentCategory code);

    boolean existsBySlug(String slug);

    List<Category> findAllByOrderBySortOrderAsc();

    List<Category> findByActiveTrueOrderBySortOrderAsc();
}
