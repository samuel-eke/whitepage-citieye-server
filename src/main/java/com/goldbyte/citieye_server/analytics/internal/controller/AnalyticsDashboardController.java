package com.goldbyte.citieye_server.analytics.internal.controller;

import com.goldbyte.citieye_server.analytics.internal.dto.*;
import com.goldbyte.citieye_server.analytics.internal.service.AnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST controller exposing analytics dashboard endpoints.
 * All endpoints are restricted to {@code agency_admin} and {@code super_admin} roles
 * via the Spring Security configuration in {@code SecurityConfig}.
 *
 * <ul>
 *   <li>{@code GET  /api/analytics/summary}               — headline metrics + all breakdowns</li>
 *   <li>{@code GET  /api/analytics/citizens}              — paginated citizen list with optional filters</li>
 *   <li>{@code GET  /api/analytics/citizens/{citizenCode}} — full profile detail for one citizen</li>
 *   <li>{@code POST /api/analytics/refresh}               — manually refresh materialized views (super_admin only)</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/analytics")
public class AnalyticsDashboardController {

    @Autowired
    private AnalyticsService analyticsService;

    /**
     * Returns the full analytics summary — totals, state/LGA breakdown, cohort breakdown,
     * employment snapshot, and health indicators.
     */
    @GetMapping("/summary")
    public ResponseEntity<AnalyticsSummaryDto> getSummary() {
        try {
            return ResponseEntity.ok(analyticsService.getSummary());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Returns a paginated list of citizens.
     *
     * @param stateOfOrigin optional filter — exact match on state of origin (e.g. "Lagos")
     * @param lga           optional filter — partial match on LGA name
     * @param gender        optional filter — "male" or "female"
     * @param cohortName    optional filter — partial match on cohort name
     * @param page          zero-based page index (default 0)
     * @param size          page size (default 20)
     */
    @GetMapping("/citizens")
    public ResponseEntity<PagedResult<CitizenListItemDto>> listCitizens(
            @RequestParam(required = false) String stateOfOrigin,
            @RequestParam(required = false) String lga,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) String cohortName,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {

        if (size > 100) size = 100; // cap maximum page size

        try {
            PagedResult<CitizenListItemDto> result =
                    analyticsService.listCitizens(stateOfOrigin, lga, gender, cohortName, page, size);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Returns the complete profile analytics detail for a single citizen,
     * including all progressive profile fields they have filled in.
     *
     * @param citizenCode the citizen's unique code (e.g. CTI-LA-IKE-000042)
     */
    @GetMapping("/citizens/{citizenCode}")
    public ResponseEntity<?> getCitizenDetail(@PathVariable String citizenCode) {
        if (citizenCode == null || citizenCode.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Citizen code must not be blank."));
        }

        return analyticsService.getCitizenProfileDetail(citizenCode)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "No citizen found with code: " + citizenCode)));
    }

    /**
     * Manually triggers a full refresh of all analytics materialized views.
     * Restricted to {@code super_admin} role only via security config.
     */
    @PostMapping("/refresh")
    public ResponseEntity<Map<String, String>> refreshViews() {
        try {
            analyticsService.refreshViews();
            return ResponseEntity.ok(Map.of("message", "Analytics views refreshed successfully."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed to refresh analytics views: " + e.getMessage()));
        }
    }
}
