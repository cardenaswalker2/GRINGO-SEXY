package com.gringosexy.dto;

import com.gringosexy.model.UserPermissions;

public class PermissionUpdateRequest {

    private boolean modifications;
    private boolean sensitivities;
    private boolean optimizations;
    private boolean performance;
    private boolean battery;
    private boolean privacy;
    private boolean apps;
    private boolean personalization;
    private boolean support;

    public PermissionUpdateRequest() {
    }

    public UserPermissions toUserPermissions() {
        UserPermissions p = new UserPermissions();
        p.setModifications(this.modifications);
        p.setSensitivities(this.sensitivities);
        p.setOptimizations(this.optimizations);
        p.setPerformance(this.performance);
        p.setBattery(this.battery);
        p.setPrivacy(this.privacy);
        p.setApps(this.apps);
        p.setPersonalization(this.personalization);
        p.setSupport(this.support);
        return p;
    }

    public static PermissionUpdateRequest fromUserPermissions(UserPermissions p) {
        PermissionUpdateRequest req = new PermissionUpdateRequest();
        if (p != null) {
            req.setModifications(p.isModifications());
            req.setSensitivities(p.isSensitivities());
            req.setOptimizations(p.isOptimizations());
            req.setPerformance(p.isPerformance());
            req.setBattery(p.isBattery());
            req.setPrivacy(p.isPrivacy());
            req.setApps(p.isApps());
            req.setPersonalization(p.isPersonalization());
            req.setSupport(p.isSupport());
        }
        return req;
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
}
