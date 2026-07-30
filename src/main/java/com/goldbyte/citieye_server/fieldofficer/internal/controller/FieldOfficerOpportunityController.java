package com.goldbyte.citieye_server.fieldofficer.internal.controller;

import com.goldbyte.citieye_server.admin_content_cms.CMSPublicService;
import com.goldbyte.citieye_server.admin_content_cms.OpportunityApplicationDto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("api/field-officers/opportunities")
public class FieldOfficerOpportunityController {

    private final CMSPublicService cmsPublicService;

    public FieldOfficerOpportunityController(CMSPublicService cmsPublicService) {
        this.cmsPublicService = cmsPublicService;
    }

    @GetMapping("/applications")
    public ResponseEntity<List<OpportunityApplicationDto>> getAssignedApplications() {
        String officerCode = SecurityContextHolder.getContext().getAuthentication().getName();
        List<OpportunityApplicationDto> applications = cmsPublicService.getApplicationsForOfficer(officerCode);
        return ResponseEntity.ok(applications);
    }

    @PutMapping("/applications/{id}")
    public ResponseEntity<?> updateApplication(
            @PathVariable("id") UUID id,
            @RequestBody Map<String, String> request) {
        String officerCode = SecurityContextHolder.getContext().getAuthentication().getName();
        String status = request.get("status");
        String notes = request.get("officerNotes");

        if (status == null || status.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Status is required"));
        }

        try {
            OpportunityApplicationDto updated = cmsPublicService.updateApplicationStatus(id, officerCode, status, notes);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
