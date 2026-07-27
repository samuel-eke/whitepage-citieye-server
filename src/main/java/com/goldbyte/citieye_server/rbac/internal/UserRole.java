package com.goldbyte.citieye_server.rbac.internal;

import java.util.List;

public enum UserRole {
    super_admin(List.of("MANAGE_USERS", "CONFIGURE_ELIGIBILITY", "VIEW_AUDIT_LOGS", "VIEW_ANALYTICS", "EXPORT_REPORTS")),
    agency_admin(List.of("PUBLISH_CMS", "READ_CITIZENS", "VIEW_ANALYTICS")),
    field_officer(List.of("READ_ASSIGNED_COHORTS", "SUBMIT_SOCIO_ECONOMIC_REPORT", "OFFLINE_SYNC")),
    field_officer_health(List.of("READ_ASSIGNED_COHORTS", "SUBMIT_HEALTH_REPORT", "OFFLINE_SYNC")),
    field_officer_education(List.of("READ_ASSIGNED_COHORTS", "SUBMIT_EDUCATION_REPORT", "OFFLINE_SYNC")),
    citizen(List.of("READ_PROFILE", "TAKE_ASSESSMENT", "INTERACT_CMS"));

    private final List<String> permissions;

    UserRole(List<String> permissions) {
        this.permissions = permissions;
    }

    public List<String> getPermissions() {
        return permissions;
    }

    public List<String> getInheritedPermissions() {
        java.util.Set<String> all = new java.util.LinkedHashSet<>();
        if (this == super_admin) {
            // A superadmin gets all defined permissions in the application
            for (UserRole role : UserRole.values()) {
                all.addAll(role.permissions);
            }
            // Plus explicit administrative edit/delete powers
            all.addAll(List.of(
                "EDIT_CITIZEN_PROFILE", "DELETE_CITIZEN_PROFILE",
                "EDIT_HEALTH_RECORD", "DELETE_HEALTH_RECORD",
                "EDIT_EDUCATION_RECORD", "DELETE_EDUCATION_RECORD",
                "EDIT_CMS", "DELETE_CMS"
            ));
        } else {
            all.addAll(permissions);
            if (this == agency_admin) {
                all.addAll(field_officer.getInheritedPermissions());
                all.addAll(field_officer_health.getInheritedPermissions());
                all.addAll(field_officer_education.getInheritedPermissions());
            } else if (this == field_officer || this == field_officer_health || this == field_officer_education) {
                all.addAll(citizen.getInheritedPermissions());
            }
        }
        return new java.util.ArrayList<>(all);
    }
}

