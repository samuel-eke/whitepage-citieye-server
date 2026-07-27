package com.goldbyte.citieye_server.health.internal.controller;

import com.goldbyte.citieye_server.citizen_profile.CitizenProfilePublicService;
import com.goldbyte.citieye_server.health.internal.model.HealthRecord;
import com.goldbyte.citieye_server.health.internal.repo.HealthRecordRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/field-officers/health")
public class HealthRecordController {

    private final HealthRecordRepository healthRecordRepository;
    private final CitizenProfilePublicService citizenProfilePublicService;

    public HealthRecordController(HealthRecordRepository healthRecordRepository,
                                  CitizenProfilePublicService citizenProfilePublicService) {
        this.healthRecordRepository = healthRecordRepository;
        this.citizenProfilePublicService = citizenProfilePublicService;
    }

    @GetMapping("/records/{citizenCode}")
    public ResponseEntity<?> getHealthRecords(@PathVariable String citizenCode) {
        if (citizenCode == null || citizenCode.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Citizen code is required."));
        }

        List<HealthRecord> records = healthRecordRepository.findAllByCitizenCode(citizenCode.trim());
        return ResponseEntity.ok(records);
    }

    @PostMapping("/records")
    public ResponseEntity<?> logHealthRecord(@Valid @RequestBody HealthRecordRequest request) {
        String officerCode = SecurityContextHolder.getContext().getAuthentication().getName();

        if (officerCode == null || "anonymousUser".equalsIgnoreCase(officerCode)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Unauthorized. Please log in as a field officer."));
        }

        // Validate target citizen exists
        if (!citizenProfilePublicService.profileExists(request.getCitizenCode())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Invalid citizen code. Citizen profile not found."));
        }

        // Create and save health record
        HealthRecord record = new HealthRecord();
        record.setCitizenCode(request.getCitizenCode().trim());
        record.setOfficerCode(officerCode);
        record.setRecordType(request.getRecordType().trim().toUpperCase());
        record.setDescription(request.getDescription().trim());
        record.setNotes(request.getNotes() != null ? request.getNotes().trim() : null);
        record.setLoggedAt(OffsetDateTime.now());

        HealthRecord saved = healthRecordRepository.save(record);

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "message", "Health record logged successfully.",
                "recordId", saved.getId()
        ));
    }

    @PutMapping("/records/{recordId}")
    public ResponseEntity<?> updateHealthRecord(@PathVariable Long recordId, @Valid @RequestBody HealthRecordRequest request) {
        // Enforce admin privileges
        boolean hasEditPrivilege = SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                .stream()
                .anyMatch(a -> a.getAuthority().equals("super_admin") || a.getAuthority().equals("agency_admin") || a.getAuthority().equals("EDIT_HEALTH_RECORD"));

        if (!hasEditPrivilege) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Forbidden. You do not have permissions to edit health records."));
        }

        return healthRecordRepository.findById(recordId)
                .map(record -> {
                    record.setRecordType(request.getRecordType().trim().toUpperCase());
                    record.setDescription(request.getDescription().trim());
                    record.setNotes(request.getNotes() != null ? request.getNotes().trim() : null);
                    healthRecordRepository.save(record);
                    return ResponseEntity.ok(Map.of("message", "Health record updated successfully."));
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "Health record not found with ID: " + recordId)));
    }

    @DeleteMapping("/records/{recordId}")
    public ResponseEntity<?> deleteHealthRecord(@PathVariable Long recordId) {
        // Enforce super admin privileges
        boolean isSuperAdmin = SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                .stream()
                .anyMatch(a -> a.getAuthority().equals("super_admin") || a.getAuthority().equals("DELETE_HEALTH_RECORD"));

        if (!isSuperAdmin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Forbidden. Only super administrators can delete health records."));
        }

        return healthRecordRepository.findById(recordId)
                .map(record -> {
                    healthRecordRepository.delete(record);
                    return ResponseEntity.ok(Map.of("message", "Health record deleted successfully."));
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "Health record not found with ID: " + recordId)));
    }

    @Getter
    @Setter
    public static class HealthRecordRequest {
        @NotBlank(message = "Citizen code is required")
        @Size(max = 30)
        private String citizenCode;

        @NotBlank(message = "Record type is required")
        @Size(max = 50)
        private String recordType; // VACCINATION, SCREENING, NUTRITION

        @NotBlank(message = "Description is required")
        private String description;

        private String notes;
    }
}
