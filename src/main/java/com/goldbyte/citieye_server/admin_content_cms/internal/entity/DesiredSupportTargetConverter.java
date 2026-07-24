package com.goldbyte.citieye_server.admin_content_cms.internal.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class DesiredSupportTargetConverter implements AttributeConverter<String, String> {
    @Override
    public String convertToDatabaseColumn(String attribute) {
        return attribute; // Returns String directly, compatible with stringtype=unspecified
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        return dbData;
    }
}
