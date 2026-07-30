package com.goldbyte.citieye_server.health.internal.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class HealthQuestion {
    private final String fieldName;
    private final String questionText;
    private final String dataType; // BOOLEAN, STRING, NUMBER, JSON
    private final List<String> choices; // Options list if applicable
    private final boolean ndprSensitive;
}
