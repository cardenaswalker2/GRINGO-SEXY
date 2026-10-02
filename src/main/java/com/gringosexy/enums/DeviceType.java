package com.gringosexy.enums;

public enum DeviceType {
    IPHONE("iPhone", "Apple iOS Devices", "fab fa-apple"),
    SAMSUNG("Samsung", "Samsung Galaxy Devices", "fas fa-mobile-alt"),
    XIAOMI("Xiaomi", "Xiaomi & Poco Devices", "fas fa-mobile"),
    MOTOROLA("Motorola", "Motorola Moto Devices", "fas fa-mobile-android"),
    HUAWEI("Huawei", "Huawei Devices", "fas fa-mobile-screen"),
    OPPO("Oppo", "Oppo & Realme Devices", "fas fa-mobile-retro"),
    VIVO("Vivo", "Vivo Devices", "fas fa-mobile-screen-button"),
    PIXEL("Google Pixel", "Google Pixel Devices", "fab fa-google"),
    ONEPLUS("OnePlus", "OnePlus Devices", "fas fa-plus-square"),
    OTHER("Otro / Genérico", "Otros dispositivos móviles", "fas fa-gamepad");

    private final String displayName;
    private final String description;
    private final String iconClass;

    DeviceType(String displayName, String description, String iconClass) {
        this.displayName = displayName;
        this.description = description;
        this.iconClass = iconClass;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public String getIconClass() {
        return iconClass;
    }
}
