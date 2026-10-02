package com.gringosexy.dto;

import com.gringosexy.enums.ContentCategory;
import com.gringosexy.enums.DeviceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.HashSet;
import java.util.Set;

public class ContentRequest {

    private String id;

    @NotBlank(message = "El título es obligatorio")
    @Size(min = 3, max = 150, message = "El título debe tener entre 3 y 150 caracteres")
    private String title;

    private String slug;

    @NotBlank(message = "El resumen o descripción corta es obligatorio")
    @Size(min = 10, max = 500, message = "El resumen debe tener entre 10 y 500 caracteres")
    private String summary;

    @NotBlank(message = "El contenido o instrucciones detalladas son obligatorios")
    private String body;

    @NotNull(message = "Debes seleccionar una categoría")
    private ContentCategory category;

    private Set<DeviceType> targetDevices = new HashSet<>();

    private boolean applyToAllDevices = true;

    private String imageUrl;

    private String videoUrl;

    private boolean active = true;

    private boolean featured = false;

    private int sortOrder = 0;

    public ContentRequest() {
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
}
