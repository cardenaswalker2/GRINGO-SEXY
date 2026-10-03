package com.gringosexy.dto;

import com.gringosexy.model.UserPermissions;

public class PermissionUpdateRequest {

    private boolean modifications;
    private boolean sensitivities;
    private boolean optimizations;

    public PermissionUpdateRequest() {
    }

    public UserPermissions toUserPermissions() {
        UserPermissions p = new UserPermissions();
        p.setModifications(this.modifications);
        p.setSensitivities(this.sensitivities);
        p.setOptimizations(this.optimizations);
        return p;
    }

    public static PermissionUpdateRequest fromUserPermissions(UserPermissions p) {
        PermissionUpdateRequest req = new PermissionUpdateRequest();
        if (p != null) {
            req.setModifications(p.isModifications());
            req.setSensitivities(p.isSensitivities());
            req.setOptimizations(p.isOptimizations());
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
}
