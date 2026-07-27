package com.goldbyte.citieye_server.analytics.internal.dto;

/**
 * Aggregated citizen count broken down by state of origin, state of residence, and LGA.
 */
public record StateBreakdownDto(
        String stateOfOrigin,
        String stateOfResidence,
        String lga,
        long totalCitizens,
        long maleCount,
        long femaleCount,
        long registeredLast7Days,
        long registeredLast30Days
) {}
