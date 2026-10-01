package com.goldbyte.citieye_server.userauth.internal.controller;

import com.goldbyte.citieye_server.userauth.internal.dto.OfficerSummaryDto;
import com.goldbyte.citieye_server.userauth.internal.service.AdminOfficerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller exposing REST endpoints for administrators to query registered field officers.
 * Security is enforced via SecurityConfig for agency_admin and super_admin roles.
 */
@RestController
@RequestMapping("/api/admin/officers")
public class AdminOfficerController {

    private final AdminOfficerService adminOfficerService;

    public AdminOfficerController(AdminOfficerService adminOfficerService) {
        this.adminOfficerService = adminOfficerService;
    }

    /**
     * Lists registered field officers with optional filters:
     * - role: e.g. "field_officer_education", "field_officer_health", "field_officer"
     * - specialty: e.g. "EDUCATION", "HEALTH", "MEDICAL"
     * - state: e.g. "Lagos", "Edo"
     * - lga: e.g. "Ikeja", "Oredo"
     * - query: text search across name, citizenCode, email, or phone
     */
    @GetMapping
    public ResponseEntity<List<OfficerSummaryDto>> listOfficers(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String specialty,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String lga,
            @RequestParam(required = false) String query
    ) {
        List<OfficerSummaryDto> officers = adminOfficerService.getFieldOfficers(role, specialty, state, lga, query);
        return ResponseEntity.ok(officers);
    }

    /**
     * Retrieves full profile details for a specific registered field officer by citizen code.
     */
    @GetMapping("/{citizenCode}")
    public ResponseEntity<OfficerSummaryDto> getOfficerByCode(@PathVariable String citizenCode) {
        OfficerSummaryDto officer = adminOfficerService.getFieldOfficerByCode(citizenCode);
        return ResponseEntity.ok(officer);
    }
}
