package com.goldbyte.citieye_server.userauth.internal.model.enums;

/**
 * Maps to the {@code auth.user_role} PostgreSQL enum.
 */
public enum UserRole {
    citizen,
    field_officer,
    field_officer_health,
    field_officer_education,
    agency_admin,
    super_admin
}
