package com.goldbyte.citieye_server.analytics.internal.dto;

/**
 * Citizen population and milestone stats per cohort group.
 */
public record CohortBreakdownDto(
        int cohortId,
        String cohortName,
        Integer minAge,
        Integer maxAge,
        long totalCitizens,
        long citizensWithAnyMilestoneCompleted,
        Double avgAgeAtRegistration
) {}
