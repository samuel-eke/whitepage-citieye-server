package com.goldbyte.citieye_server.analytics.internal.dto;

import java.time.OffsetDateTime;

/**
 * Lightweight DTO for paginated citizen listing in the analytics dashboard.
 */
public record CitizenListItemDto(
        String citizenCode,
        String fullName,
        String stateOfOrigin,
        String stateOfResidence,
        String lga,
        String residenceLga,
        String gender,
        Integer age,
        String cohortName,
        OffsetDateTime registeredAt
) {}
