package com.goldbyte.citieye_server.cohort_placement.internal.model.enums;

/**
 * Metric types for KPI templates.
 */
public enum MetricType {
    numeric,
    boolean_type, // Note: boolean is a reserved keyword in Java, so we map to boolean_type or similar, but wait, the database has 'boolean' as enum value. If we use boolean_type we can write a converter or use the enum name as string mapping, or write boolean_type and use a converter if needed, or simply name it 'boolean_val'.
    // Let's use lowercase values but since 'boolean' is a reserved Java keyword, we must map it.
    // Let's use boolean_val or similar, or use a custom converter to map Java enum to/from PostgreSQL literal 'boolean'.
    percentage,
    scale;

    // To allow mapping 'boolean' literally to/from database
    public static MetricType fromValue(String value) {
        if ("boolean".equalsIgnoreCase(value)) {
            return boolean_type;
        }
        return valueOf(value.toLowerCase());
    }

    public String toValue() {
        if (this == boolean_type) {
            return "boolean";
        }
        return name();
    }
}
