package com.goldbyte.citieye_server.admin_content_cms.internal.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class NigerianStateTargetConverter implements AttributeConverter<String, String> {
    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null) {
            return null;
        }
        // Replace underscores with spaces (e.g. Akwa_Ibom -> Akwa Ibom) to match PG enum values
        return attribute.replace('_', ' ');
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        // Replace spaces with underscores for Java enum compatibility
        return dbData.replace(' ', '_');
    }
}
