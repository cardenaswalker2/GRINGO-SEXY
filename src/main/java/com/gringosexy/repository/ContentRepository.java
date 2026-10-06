package com.gringosexy.repository;

import com.gringosexy.enums.ContentCategory;
import com.gringosexy.enums.DeviceType;
import com.gringosexy.model.Content;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContentRepository extends MongoRepository<Content, String> {

    Optional<Content> findBySlug(String slug);

    boolean existsBySlug(String slug);

    List<Content> findByCategoryAndActiveTrueOrderBySortOrderAscCreatedAtDesc(ContentCategory category);

    // Find active contents for category and matching device (either applyToAllDevices=true OR targetDevices contains deviceType)
    @Query("{ 'category': ?0, 'active': true, $or: [ { 'applyToAllDevices': true }, { 'targetDevices': ?1 } ] }")
    List<Content> findByCategoryAndDevice(ContentCategory category, DeviceType deviceType);

    @Query("{ 'active': true, $or: [ { 'applyToAllDevices': true }, { 'targetDevices': ?0 } ] }")
    List<Content> findAllActiveByDevice(DeviceType deviceType);

    @Query(value = "{ 'active': true, 'videoUrl': { $ne: null, $ne: '' }, $or: [ { 'applyToAllDevices': true }, { 'targetDevices': ?0 } ] }", count = true)
    long countActiveVideosByDevice(DeviceType deviceType);

    @Query(value = "{ 'active': true, $or: [ { 'applyToAllDevices': true }, { 'targetDevices': ?0 } ] }", count = true)
    long countActiveContentsByDevice(DeviceType deviceType);

    long countByCategory(ContentCategory category);

    long countByActiveTrue();

    @Query("{ $or: [ " +
           "{ 'title': { $regex: ?0, $options: 'i' } }, " +
           "{ 'summary': { $regex: ?0, $options: 'i' } }, " +
           "{ 'body': { $regex: ?0, $options: 'i' } } " +
           "] }")
    Page<Content> searchContents(String query, Pageable pageable);
}
