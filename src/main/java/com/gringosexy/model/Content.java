package com.gringosexy.model;

import com.gringosexy.enums.ContentCategory;
import com.gringosexy.enums.DeviceType;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Document(collection = "contents")
public class Content {

    @Id
    private String id;

    private String title;

    @Indexed(unique = true)
    private String slug;

    private String summary;

    private String body;

    @Indexed
    private ContentCategory category;

    // A content item can target specific devices (empty or null means ALL devices)
    @Indexed
    private Set<DeviceType> targetDevices = new HashSet<>();

    private boolean applyToAllDevices = false;

    private String imageUrl;

    private String videoUrl;

    private boolean active = true;

    private boolean featured = false;

    private int sortOrder = 0;

    private String createdBy;

    @CreatedDate
    @Indexed
    private Instant createdAt = Instant.now();

    @LastModifiedDate
    private Instant updatedAt = Instant.now();

    public Content() {
    }

    public Content(String title, String slug, String summary, String body, ContentCategory category, Set<DeviceType> targetDevices, boolean applyToAllDevices) {
        this.title = title;
        this.slug = slug;
        this.summary = summary;
        this.body = body;
        this.category = category;
        this.targetDevices = targetDevices != null ? targetDevices : new HashSet<>();
        this.applyToAllDevices = applyToAllDevices;
        this.active = true;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public boolean isAvailableFor(DeviceType deviceType) {
        if (this.applyToAllDevices) {
            return true;
        }
        if (this.targetDevices == null || this.targetDevices.isEmpty()) {
            return true;
        }
        return deviceType != null && this.targetDevices.contains(deviceType);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public ContentCategory getCategory() {
        return category;
    }

    public void setCategory(ContentCategory category) {
        this.category = category;
    }

    public Set<DeviceType> getTargetDevices() {
        return targetDevices != null ? targetDevices : new HashSet<>();
    }

    public void setTargetDevices(Set<DeviceType> targetDevices) {
        this.targetDevices = targetDevices;
    }

    public boolean isApplyToAllDevices() {
        return applyToAllDevices;
    }

    public void setApplyToAllDevices(boolean applyToAllDevices) {
        this.applyToAllDevices = applyToAllDevices;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isFeatured() {
        return featured;
    }

    public void setFeatured(boolean featured) {
        this.featured = featured;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
