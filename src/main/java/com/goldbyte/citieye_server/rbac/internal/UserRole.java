package com.goldbyte.citieye_server.rbac.internal;

import java.util.List;

public enum UserRole {
    super_admin(List.of("MANAGE_USERS", "CONFIGURE_ELIGIBILITY", "VIEW_AUDIT_LOGS", "VIEW_ANALYTICS", "EXPORT_REPORTS")),
    agency_admin(List.of("PUBLISH_CMS", "READ_CITIZENS", "VIEW_ANALYTICS")),
    field_officer(List.of("READ_ASSIGNED_COHORTS", "SUBMIT_SOCIO_ECONOMIC_REPORT", "OFFLINE_SYNC")),
    citizen(List.of("READ_PROFILE", "TAKE_ASSESSMENT", "INTERACT_CMS"));

    private final List<String> permissions;

    UserRole(List<String> permissions) {
        this.permissions = permissions;
    }

    public List<String> getPermissions() {
        return permissions;
    }

    public List<String> getInheritedPermissions() {
        java.util.Set<String> all = new java.util.LinkedHashSet<>(permissions);
        if (this == super_admin) {
            all.addAll(agency_admin.getInheritedPermissions());
        }
        return new java.util.ArrayList<>(all);
    }
}

