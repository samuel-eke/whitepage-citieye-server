package com.goldbyte.citieye_server.analytics.internal.controller;

import com.goldbyte.citieye_server.analytics.internal.dto.*;
import com.goldbyte.citieye_server.analytics.internal.service.AnalyticsService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

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

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private AnalyticsService analyticsService;

    private record AdminStateInfo(String role, String stateOfResidence) {}

    private AdminStateInfo resolveAdminInfo(String adminCode) {
        if (adminCode == null || "anonymousUser".equalsIgnoreCase(adminCode)) {
            return null;
        }
        @SuppressWarnings("unchecked")
        List<Object[]> rows = em.createNativeQuery(
                "SELECT role::text, state_of_residence::text FROM auth.admin_user WHERE citizen_code = :code"
        ).setParameter("code", adminCode).getResultList();
        if (rows.isEmpty()) return null;
        Object[] row = rows.get(0);
        return new AdminStateInfo((String) row[0], (String) row[1]);
    }

    /**
     * Returns the full analytics summary — totals, state/LGA breakdown, cohort breakdown,
     * employment snapshot, and health indicators.
     */
    @GetMapping("/summary")
    public ResponseEntity<?> getSummary() {
        try {
            String adminCode = SecurityContextHolder.getContext().getAuthentication().getName();
            AdminStateInfo adminInfo = resolveAdminInfo(adminCode);
            if (adminInfo == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", "Could not resolve administrator details."));
            }

            if ("super_admin".equalsIgnoreCase(adminInfo.role())) {
                return ResponseEntity.ok(analyticsService.getSummary(null));
            } else {
                return ResponseEntity.ok(analyticsService.getSummary(adminInfo.stateOfResidence()));
            }
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
    public ResponseEntity<?> listCitizens(
            @RequestParam(required = false) String stateOfOrigin,
            @RequestParam(required = false) String lga,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) String cohortName,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {

        if (size > 100) size = 100; // cap maximum page size

        try {
            String adminCode = SecurityContextHolder.getContext().getAuthentication().getName();
            AdminStateInfo adminInfo = resolveAdminInfo(adminCode);
            if (adminInfo == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", "Could not resolve administrator details."));
            }

            String stateResidenceFilter = null;
            if (!"super_admin".equalsIgnoreCase(adminInfo.role())) {
                stateResidenceFilter = adminInfo.stateOfResidence();
            }

            PagedResult<CitizenListItemDto> result =
                    analyticsService.listCitizens(stateOfOrigin, stateResidenceFilter, lga, gender, cohortName, page, size);
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

        String adminCode = SecurityContextHolder.getContext().getAuthentication().getName();
        AdminStateInfo adminInfo = resolveAdminInfo(adminCode);
        if (adminInfo == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Could not resolve administrator details."));
        }

        Optional<CitizenProfileAnalyticsDto> profileOpt = analyticsService.getCitizenProfileDetail(citizenCode);
        if (profileOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "No citizen found with code: " + citizenCode));
        }

        CitizenProfileAnalyticsDto profile = profileOpt.get();
        boolean isSuperAdmin = "super_admin".equalsIgnoreCase(adminInfo.role());
        if (!isSuperAdmin && !adminInfo.stateOfResidence().equalsIgnoreCase(profile.stateOfResidence())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Forbidden. You only have access to citizens in " + adminInfo.stateOfResidence()));
        }

        return ResponseEntity.ok(profile);
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
