package com.gringosexy.dto;

import com.gringosexy.enums.DeviceType;

public class DeviceCatalogCardDto {

    private DeviceType deviceType;
    private String slug;
    private String name;
    private String description;
    private String iconClass;
    private long videoCount;
    private long photoCount;
    private long totalCount;
    private boolean userDevice;

    public DeviceCatalogCardDto() {
    }

    public DeviceCatalogCardDto(DeviceType deviceType, String slug, String name, String description,
                                String iconClass, long videoCount, long photoCount, long totalCount, boolean userDevice) {
        this.deviceType = deviceType;
        this.slug = slug;
        this.name = name;
        this.description = description;
        this.iconClass = iconClass;
        this.videoCount = videoCount;
        this.photoCount = photoCount;
        this.totalCount = totalCount;
        this.userDevice = userDevice;
    }

    public DeviceType getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(DeviceType deviceType) {
        this.deviceType = deviceType;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIconClass() {
        return iconClass;
    }

    public void setIconClass(String iconClass) {
        this.iconClass = iconClass;
    }

    public long getVideoCount() {
        return videoCount;
    }

    public void setVideoCount(long videoCount) {
        this.videoCount = videoCount;
    }

    public long getPhotoCount() {
        return photoCount;
    }

    public void setPhotoCount(long photoCount) {
        this.photoCount = photoCount;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(long totalCount) {
        this.totalCount = totalCount;
    }

    public boolean isUserDevice() {
        return userDevice;
    }

    public void setUserDevice(boolean userDevice) {
        this.userDevice = userDevice;
    }
}
