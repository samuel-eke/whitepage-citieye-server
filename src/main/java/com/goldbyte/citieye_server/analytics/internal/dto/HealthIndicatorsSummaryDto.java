package com.goldbyte.citieye_server.analytics.internal.dto;

/**
 * Aggregated health indicators across all profiled citizens.
 */
public record HealthIndicatorsSummaryDto(
        long totalCitizensProfiled,
        long vaccinationComplete,
        long pregnantCount,
        long expectantParents,
        long hypertensionDiabetesCount,
        long nhisEnrolledCount,
        long nhisSeniorPlanCount,
        long bloodGroupRecorded,
        long chronicIllnessCount
) {}
