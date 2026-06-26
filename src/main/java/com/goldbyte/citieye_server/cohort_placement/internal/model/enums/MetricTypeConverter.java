package com.goldbyte.citieye_server.cohort_placement.internal.model.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class MetricTypeConverter implements AttributeConverter<MetricType, String> {

    @Override
    public String convertToDatabaseColumn(MetricType attribute) {
        if (attribute == null) return null;
        return attribute.toValue();
    }

    @Override
    public MetricType convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        return MetricType.fromValue(dbData);
    }
}
