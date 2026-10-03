package com.gringosexy.enums;

public enum DeviceType {
    SAMSUNG("Samsung", "Samsung Galaxy (One UI & Game Booster)", "fas fa-mobile-alt"),
    MOTOROLA("Motorola", "Motorola Moto Series (MyUX & Edge)", "fas fa-mobile-android"),
    XIAOMI("Xiaomi", "Xiaomi, Redmi & POCO (MIUI / HyperOS)", "fas fa-mobile"),
    REALME("Realme", "Realme & GT Series (Realme UI)", "fas fa-bolt"),
    IPHONE("iPhone", "Apple iOS Devices (11 al 16 Pro Max)", "fab fa-apple"),
    
    // Legacy mapping to avoid Mongo deserialization errors
    HUAWEI("Huawei", "Huawei Devices", "fas fa-mobile-screen"),
    OPPO("Oppo", "Oppo Devices", "fas fa-mobile-retro"),
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
