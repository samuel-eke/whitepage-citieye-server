package com.goldbyte.citieye_server.analytics.internal.dto;

/**
 * Employment status distribution snapshot.
 */
public record EmploymentBreakdownDto(
        String employmentStatus,
        String monthlyIncomeRange,
        String industrySector,
        String highestAcademicQualification,
        String stateOfOrigin,
        String lga,
        long citizenCount
) {}
