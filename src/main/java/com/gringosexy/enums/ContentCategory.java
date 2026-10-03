package com.gringosexy.enums;

public enum ContentCategory {
    MODIFICATIONS("Modificaciones", "modificaciones", "Personaliza tu sistema y saca el máximo provecho de tu procesador y opciones ocultas", "fas fa-sliders-h", 1),
    SENSITIVITIES("Sensibilidades", "sensibilidades", "Ajusta la precisión de puntero y responde más rápido en cada juego competitivo", "fas fa-bullseye", 2),
    OPTIMIZATIONS("Optimizaciones", "optimizaciones", "Elimina procesos innecesarios en segundo plano y mejora la memoria RAM disponible", "fas fa-bolt", 3),
    
    // Legacy support to prevent Mongo read exceptions during database cleanup
    PERFORMANCE("Rendimiento", "rendimiento", "Legacy", "fas fa-bolt", 4),
    BATTERY("Batería", "bateria", "Legacy", "fas fa-battery-full", 5),
    PRIVACY("Privacidad", "privacidad", "Legacy", "fas fa-shield-alt", 6),
    APPS("Apps y Herramientas", "apps", "Legacy", "fas fa-cubes", 7),
    PERSONALIZATION("Personalización", "personalizacion", "Legacy", "fas fa-paint-brush", 8),
    SUPPORT("Soporte", "soporte", "Legacy", "fas fa-headset", 9);



    private final String displayName;
    private final String slug;
    private final String description;
    private final String iconClass;
    private final int sortOrder;

    ContentCategory(String displayName, String slug, String description, String iconClass, int sortOrder) {
        this.displayName = displayName;
        this.slug = slug;
        this.description = description;
        this.iconClass = iconClass;
        this.sortOrder = sortOrder;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getSlug() {
        return slug;
    }

    public String getDescription() {
        return description;
    }

    public String getIconClass() {
        return iconClass;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public static ContentCategory fromSlug(String slug) {
        for (ContentCategory category : values()) {
            if (category.getSlug().equalsIgnoreCase(slug)) {
                return category;
            }
        }
        return null;
    }
}
