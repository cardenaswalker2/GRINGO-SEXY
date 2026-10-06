package com.gringosexy.model;

import java.io.Serializable;
import java.util.Objects;

public class UserPermissions implements Serializable {

    private boolean modifications = true;
    private boolean sensitivities = true;
    private boolean optimizations = true;

    public UserPermissions() {
    }

    public static UserPermissions allEnabled() {
        UserPermissions perms = new UserPermissions();
        perms.setModifications(true);
        perms.setSensitivities(true);
        perms.setOptimizations(true);
        return perms;
    }

    public static UserPermissions allDisabled() {
        UserPermissions perms = new UserPermissions();
        perms.setModifications(false);
        perms.setSensitivities(false);
        perms.setOptimizations(false);
        return perms;
    }

    public boolean hasPermissionFor(String categorySlug) {
        if (categorySlug == null || categorySlug.trim().isEmpty()) return false;
        String slug = categorySlug.trim().toLowerCase();
        switch (slug) {
            case "modificaciones":
                return this.modifications;
            case "sensibilidades":
                return this.sensitivities;
            case "optimizaciones":
            case "rendimiento":
            case "bateria":
            case "battery":
            case "privacidad":
            case "apps":
            case "personalizacion":
                return this.optimizations;
            default:
                // Return true by default for general/other modules if user has basic permissions
                return true;
        }
    }

    public boolean isModifications() {
        return modifications;
    }

    public void setModifications(boolean modifications) {
        this.modifications = modifications;
    }

    public boolean isSensitivities() {
        return sensitivities;
    }

    public void setSensitivities(boolean sensitivities) {
        this.sensitivities = sensitivities;
    }

    public boolean isOptimizations() {
        return optimizations;
    }

    public void setOptimizations(boolean optimizations) {
        this.optimizations = optimizations;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserPermissions that = (UserPermissions) o;
        return modifications == that.modifications &&
                sensitivities == that.sensitivities &&
                optimizations == that.optimizations;
    }

    @Override
    public int hashCode() {
        return Objects.hash(modifications, sensitivities, optimizations);
    }
}
