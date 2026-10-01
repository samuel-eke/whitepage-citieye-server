package com.goldbyte.citieye_server.education.internal.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.goldbyte.citieye_server.education.internal.model.StudentAcademicUpdate;
import com.goldbyte.citieye_server.education.internal.service.StudentAcademicService;
import com.goldbyte.citieye_server.education.internal.service.StudentAcademicService.AcademicProgressionResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api/education")
public class StudentAcademicController {

    private final StudentAcademicService studentAcademicService;
    private final ObjectMapper objectMapper;

    public StudentAcademicController(
            StudentAcademicService studentAcademicService,
            @org.springframework.beans.factory.annotation.Autowired(required = false)
            @org.springframework.lang.Nullable ObjectMapper objectMapper) {
        this.studentAcademicService = studentAcademicService;
        this.objectMapper = objectMapper != null ? objectMapper : new ObjectMapper();
    }

    private String getCurrentUserCode() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equalsIgnoreCase(auth.getName())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized. Please log in.");
        }
        return auth.getName();
    }

    /**
     * Checks if the caller has educational staff or administrative privileges.
     */
    private boolean isStaffOrAdmin(Authentication auth) {
        if (auth == null || auth.getAuthorities() == null) {
            return false;
        }
        return auth.getAuthorities().stream().anyMatch(a -> {
            String role = a.getAuthority();
            return "field_officer_education".equalsIgnoreCase(role) ||
                    "ROLE_field_officer_education".equalsIgnoreCase(role) ||
                    "agency_admin".equalsIgnoreCase(role) ||
                    "ROLE_agency_admin".equalsIgnoreCase(role) ||
                    "super_admin".equalsIgnoreCase(role) ||
                    "ROLE_super_admin".equalsIgnoreCase(role);
        });
    }

    /**
     * Resolves the target citizen code preventing IDOR / horizontal privilege escalation.
     * Non-staff/admin callers cannot submit or modify academic records on behalf of other citizens.
     */
    private String resolveCitizenCode(String requestedCode, String callerCode, Authentication auth) {
        if (requestedCode != null && !requestedCode.isBlank()) {
            String trimmed = requestedCode.trim();
            if (!trimmed.equalsIgnoreCase(callerCode)) {
                if (!isStaffOrAdmin(auth)) {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                            "Forbidden. Regular citizens cannot submit or modify academic updates for other citizens.");
                }
                return trimmed;
            }
        }
        return callerCode;
    }

    /**
     * R1: Student Annual Academic Result Submission (Multipart with optional result card file)
     * Compulsory submission requirement: accepts either a multipart result card image/PDF
     * or dynamic subjectGrades JSON string (or both). Rejects with 400 Bad Request if neither is provided.
     */
    @PostMapping(value = "/student/academic-updates", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> submitAcademicUpdateMultipart(
            @RequestParam("academicLevel") String academicLevel,
            @RequestParam("academicYear") String academicYear,
            @RequestParam(value = "citizenCode", required = false) String targetCitizenCode,
            @RequestParam(value = "classGrade", required = false) String classGrade,
            @RequestParam(value = "institutionName", required = false) String institutionName,
            @RequestParam(value = "subjectGrades", required = false) String subjectGradesJson,
            @RequestPart(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "notes", required = false) String notes) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String callerCode = getCurrentUserCode();
        String citizenCode = resolveCitizenCode(targetCitizenCode, callerCode, auth);

        Map<String, String> subjectGrades = null;
        if (subjectGradesJson != null && !subjectGradesJson.isBlank()) {
            try {
                subjectGrades = objectMapper.readValue(subjectGradesJson, new TypeReference<Map<String, String>>() {});
            } catch (Exception e) {
                return ResponseEntity.badRequest().body(Map.of("message", "Invalid JSON format for subjectGrades: " + e.getMessage()));
            }
        }

        StudentAcademicUpdate saved = studentAcademicService.submitAcademicUpdate(
                citizenCode,
                academicLevel,
                academicYear,
                classGrade,
                institutionName,
                file,
                subjectGrades,
                notes
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    /**
     * R1: Student Annual Academic Result Submission (JSON payload with dynamic subject-grade mappings)
     */
    @PostMapping(value = { "/student/academic-updates", "/student/academic-updates/json" }, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> submitAcademicUpdateJson(@Valid @RequestBody AcademicUpdateJsonRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String callerCode = getCurrentUserCode();
        String citizenCode = resolveCitizenCode(request.getCitizenCode(), callerCode, auth);

        StudentAcademicUpdate saved = studentAcademicService.submitAcademicUpdate(
                citizenCode,
                request.getAcademicLevel(),
                request.getAcademicYear(),
                request.getClassGrade(),
                request.getInstitutionName(),
                null,
                request.getSubjectGrades(),
                request.getNotes()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    /**
     * R1: Administrative Result Status Transition Endpoint for officers/admins.
     * Transitions status (SUBMITTED -> VERIFIED -> ACTIVE / REJECTED) and logs transition in real time.
     */
    @PatchMapping("/academic-updates/{id}/status")
    public ResponseEntity<?> updateAcademicStatus(
            @PathVariable Long id,
            @Valid @RequestBody StatusTransitionRequest request) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String actorCode = getCurrentUserCode();

        boolean hasOfficerOrAdminRole = isStaffOrAdmin(auth) || auth.getAuthorities().stream().anyMatch(a -> {
            String role = a.getAuthority();
            return role.equalsIgnoreCase("EDIT_EDUCATION_RECORD");
        });

        if (!hasOfficerOrAdminRole) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Forbidden. Only educational officers and administrators can update result status."));
        }

        StudentAcademicUpdate updated = studentAcademicService.updateStatus(id, request.getStatus(), request.getRemarks(), actorCode);
        return ResponseEntity.ok(updated);
    }

    /**
     * R1: Query Student Academic Progression.
     * Returns historical series of annual updates and timestamped status transitions.
     */
    @GetMapping("/student/academic-progression/{citizenCode}")
    public ResponseEntity<?> getAcademicProgression(@PathVariable String citizenCode) {
        String callerCode = getCurrentUserCode();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        boolean isStaff = isStaffOrAdmin(auth);

        if (!isStaff && !callerCode.equalsIgnoreCase(citizenCode.trim())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Forbidden. You can only view your own academic progression."));
        }

        AcademicProgressionResponse progression = studentAcademicService.getAcademicProgression(citizenCode);
        return ResponseEntity.ok(progression);
    }

    @GetMapping("/student/academic-updates")
    public ResponseEntity<?> getMyAcademicUpdates() {
        String citizenCode = getCurrentUserCode();
        return ResponseEntity.ok(studentAcademicService.getUpdatesByCitizen(citizenCode));
    }

    @GetMapping("/student/academic-updates/{id}")
    public ResponseEntity<?> getAcademicUpdateById(@PathVariable Long id) {
        return studentAcademicService.getUpdateById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body((StudentAcademicUpdate) null));
    }

    @Getter
    @Setter
    public static class AcademicUpdateJsonRequest {
        private String citizenCode;

        @NotBlank(message = "Academic level is required")
        private String academicLevel;

        @NotBlank(message = "Academic year is required")
        private String academicYear;

        private String classGrade;
        private String institutionName;
        private Map<String, String> subjectGrades;
        private String notes;
    }

    @Getter
    @Setter
    public static class StatusTransitionRequest {
        @NotBlank(message = "Status is required")
        private String status; // SUBMITTED, VERIFIED, ACTIVE, REJECTED
        private String remarks;
    }
}
