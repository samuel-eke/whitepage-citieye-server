package com.goldbyte.citieye_server.analytics.internal.dto;

import java.util.List;

/**
 * Top-level response DTO for the analytics summary dashboard endpoint.
 * Aggregates key headline metrics for admin consumers.
 */
public record AnalyticsSummaryDto(

        // ── Registration Totals ────────────────────────────────────────
        long totalCitizens,
        long totalCitizensLast7Days,
        long totalCitizensLast30Days,

        // ── Gender Breakdown ──────────────────────────────────────────
        long maleCitizens,
        long femaleCitizens,

        // ── Geographic Breakdown ──────────────────────────────────────
        List<StateBreakdownDto> byStateAndLga,

        // ── Cohort Population ─────────────────────────────────────────
        List<CohortBreakdownDto> byCohort,

        // ── Employment Snapshot ───────────────────────────────────────
        List<EmploymentBreakdownDto> byEmploymentStatus,

        // ── Health Indicators ─────────────────────────────────────────
        HealthIndicatorsSummaryDto healthIndicators
) {}
