package com.goldbyte.citieye_server.userauth.internal.model.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * JPA converter that maps the {@link NigerianState} Java enum to/from
 * the exact string values stored in the {@code auth.nigerian_state} PostgreSQL enum.
 *
 * <p>This is necessary because two state names contain spaces ("Akwa Ibom", "Cross River")
 * which are invalid Java identifiers, so the Java enum uses underscores instead.
 */
@Converter
public class NigerianStateConverter implements AttributeConverter<NigerianState, String> {

    @Override
    public String convertToDatabaseColumn(NigerianState attribute) {
        if (attribute == null) return null;
        // Replace underscores with spaces to match the PostgreSQL enum literal
        return attribute.name().replace('_', ' ');
    }

    @Override
    public NigerianState convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        // Replace spaces with underscores to match the Java enum constant name
        return NigerianState.valueOf(dbData.replace(' ', '_'));
    }
}
