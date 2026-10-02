package com.gringosexy.enums;

public enum TicketStatus {
    OPEN("Abierto", "badge-warning"),
    IN_PROGRESS("En Progreso", "badge-info"),
    RESOLVED("Resuelto", "badge-success"),
    CLOSED("Cerrado", "badge-secondary");

    private final String displayName;
    private final String badgeClass;

    TicketStatus(String displayName, String badgeClass) {
        this.displayName = displayName;
        this.badgeClass = badgeClass;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getBadgeClass() {
        return badgeClass;
    }
}
