package com.gringosexy.enums;

public enum ContentCategory {
    MODIFICATIONS("Modificaciones", "modificaciones", "Ajustes del sistema, DPI, puntero y opciones de desarrollador", "fas fa-sliders-h", 1),
    SENSITIVITIES("Sensibilidades", "sensibilidades", "Configuraciones y sensibilidades óptimas para juegos y respuesta táctil", "fas fa-bullseye", 2),
    OPTIMIZATIONS("Optimizaciones", "optimizaciones", "Mejoras de memoria RAM, caché y limpieza de servicios en segundo plano", "fas fa-tachometer-alt", 3),
    PERFORMANCE("Rendimiento", "rendimiento", "Configuraciones para máximos FPS, aceleración gráfica y refrigeración", "fas fa-bolt", 4),
    BATTERY("Batería", "bateria", "Calibración, ahorro extremo y optimización de ciclos de carga", "fas fa-battery-full", 5),
    PRIVACY("Privacidad", "privacidad", "Seguridad, permisos ocultos, telemetría y protección de datos", "fas fa-shield-alt", 6),
    APPS("Apps y Herramientas", "apps", "Utilidades recomendadas, APKs/IPAs y optimizadores oficiales", "fas fa-cubes", 7),
    PERSONALIZATION("Personalización", "personalizacion", "Temas, iconos, animaciones y diseño visual personalizado", "fas fa-paint-brush", 8),
    SUPPORT("Soporte", "soporte", "Canal de ayuda técnica personalizada y resolución de dudas", "fas fa-headset", 9);

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
