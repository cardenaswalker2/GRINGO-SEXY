package com.gringosexy.service;

import com.gringosexy.dto.ContentRequest;
import com.gringosexy.enums.ContentCategory;
import com.gringosexy.enums.DeviceType;
import com.gringosexy.model.Content;
import com.gringosexy.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ContentService {

    Content createContent(ContentRequest request, String createdBy, String ipAddress);

    Content updateContent(String id, ContentRequest request, String ipAddress);

    void deleteContent(String id, String ipAddress);

    Content findById(String id);

    Content findBySlug(String slug);

    List<Content> getContentsForUserCategory(User user, ContentCategory category);

    List<Content> getContentsForDevice(DeviceType deviceType);

    long countActiveVideosByDevice(DeviceType deviceType);

    long countActivePhotosByDevice(DeviceType deviceType);

    long countActiveContentsByDevice(DeviceType deviceType);

    Content toggleActive(String id, String ipAddress);

    Page<Content> searchContents(String query, ContentCategory category, DeviceType deviceType, Pageable pageable);

    void initDefaultContents();
}
