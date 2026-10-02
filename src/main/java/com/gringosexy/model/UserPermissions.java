package com.gringosexy.model;

import java.io.Serializable;
import java.util.Objects;

public class UserPermissions implements Serializable {

    private boolean modifications = true;
    private boolean sensitivities = true;
    private boolean optimizations = true;
    private boolean performance = true;
    private boolean battery = true;
    private boolean privacy = true;
    private boolean apps = true;
    private boolean personalization = true;
    private boolean support = true;

    public UserPermissions() {
    }

    public static UserPermissions allEnabled() {
        UserPermissions perms = new UserPermissions();
        perms.setModifications(true);
        perms.setSensitivities(true);
        perms.setOptimizations(true);
        perms.setPerformance(true);
        perms.setBattery(true);
        perms.setPrivacy(true);
        perms.setApps(true);
        perms.setPersonalization(true);
        perms.setSupport(true);
        return perms;
    }

    public static UserPermissions allDisabled() {
        UserPermissions perms = new UserPermissions();
        perms.setModifications(false);
        perms.setSensitivities(false);
        perms.setOptimizations(false);
        perms.setPerformance(false);
        perms.setBattery(false);
        perms.setPrivacy(false);
        perms.setApps(false);
        perms.setPersonalization(false);
        perms.setSupport(false);
        return perms;
    }

    public boolean hasPermissionFor(String categorySlug) {
        if (categorySlug == null) return false;
        switch (categorySlug.toLowerCase()) {
            case "modificaciones":
                return this.modifications;
            case "sensibilidades":
                return this.sensitivities;
            case "optimizaciones":
                return this.optimizations;
            case "rendimiento":
                return this.performance;
            case "bateria":
                return this.battery;
            case "privacidad":
                return this.privacy;
            case "apps":
                return this.apps;
            case "personalizacion":
                return this.personalization;
            case "soporte":
                return this.support;
            default:
                return false;
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

    public boolean isPerformance() {
        return performance;
    }

    public void setPerformance(boolean performance) {
        this.performance = performance;
    }

    public boolean isBattery() {
        return battery;
    }

    public void setBattery(boolean battery) {
        this.battery = battery;
    }

    public boolean isPrivacy() {
        return privacy;
    }

    public void setPrivacy(boolean privacy) {
        this.privacy = privacy;
    }

    public boolean isApps() {
        return apps;
    }

    public void setApps(boolean apps) {
        this.apps = apps;
    }

    public boolean isPersonalization() {
        return personalization;
    }

    public void setPersonalization(boolean personalization) {
        this.personalization = personalization;
    }

    public boolean isSupport() {
        return support;
    }

    public void setSupport(boolean support) {
        this.support = support;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserPermissions that = (UserPermissions) o;
        return modifications == that.modifications &&
                sensitivities == that.sensitivities &&
                optimizations == that.optimizations &&
                performance == that.performance &&
                battery == that.battery &&
                privacy == that.privacy &&
                apps == that.apps &&
                personalization == that.personalization &&
                support == that.support;
    }

    @Override
    public int hashCode() {
        return Objects.hash(modifications, sensitivities, optimizations, performance, battery, privacy, apps, personalization, support);
    }
}
