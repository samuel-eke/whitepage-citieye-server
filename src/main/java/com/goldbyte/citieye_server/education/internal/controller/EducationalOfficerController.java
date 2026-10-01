package com.goldbyte.citieye_server.education.internal.controller;

import com.goldbyte.citieye_server.education.internal.model.OfficerSchoolAssignment;
import com.goldbyte.citieye_server.education.internal.model.School;
import com.goldbyte.citieye_server.education.internal.model.StudentRecord;
import com.goldbyte.citieye_server.education.internal.service.EducationalOfficerService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Controller providing REST endpoints for educational officers to log comprehensive school
 * and student records, enforcing strict role-based data isolation scoped to assigned School and LGA.
 */
@RestController
@RequestMapping("/api/field-officers/education")
public class EducationalOfficerController {

    private final EducationalOfficerService educationalOfficerService;

    public EducationalOfficerController(EducationalOfficerService educationalOfficerService) {
        this.educationalOfficerService = educationalOfficerService;
    }

    private String getOfficerCode() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equalsIgnoreCase(auth.getName())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized. Please log in as an educational officer.");
        }
        return auth.getName();
    }

    // ─── School Endpoints ──────────────────────────────────────────────────────────

    @PostMapping("/schools")
    public ResponseEntity<?> createSchool(@Valid @RequestBody SchoolRequest request) {
        String officerCode = getOfficerCode();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        School school = new School();
        school.setName(request.getName().trim());
        school.setLga(request.getLga().trim());
        school.setState(request.getState());
        school.setPupilCount(request.getPupilCount());
        school.setSchoolType(request.getSchoolType());
        school.setAddress(request.getAddress());

        School created = educationalOfficerService.createSchool(school, officerCode, auth);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/schools")
    public ResponseEntity<List<School>> getSchools(@RequestParam(value = "lga", required = false) String lga) {
        String officerCode = getOfficerCode();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        List<School> schools = educationalOfficerService.getSchools(lga, officerCode, auth);
        return ResponseEntity.ok(schools);
    }

    @GetMapping("/schools/{id}")
    public ResponseEntity<?> getSchoolById(@PathVariable Long id) {
        String officerCode = getOfficerCode();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        School school = educationalOfficerService.getSchoolById(id, officerCode, auth);
        return ResponseEntity.ok(school);
    }

    // ─── Student Record Endpoints ──────────────────────────────────────────────────

    @PostMapping("/students")
    public ResponseEntity<?> createStudentRecord(@Valid @RequestBody StudentRecordRequest request) {
        String officerCode = getOfficerCode();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        StudentRecord record = new StudentRecord();
        record.setCitizenCode(request.getCitizenCode().trim());
        record.setSchoolId(request.getSchoolId());
        record.setSchoolName(request.getSchoolName());
        record.setLga(request.getLga().trim());
        record.setStudentName(request.getStudentName());
        record.setStateOfOrigin(request.getStateOfOrigin());
        record.setHealthConditionsSpecialNeeds(request.getHealthConditionsSpecialNeeds());
        record.setAttendanceRate(request.getAttendanceRate());
        record.setFamilyGuardianBackground(request.getFamilyGuardianBackground());
        record.setAcademicYear(request.getAcademicYear());
        record.setClassGrade(request.getClassGrade());

        StudentRecord created = educationalOfficerService.createStudentRecord(record, officerCode, auth);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/students")
    public ResponseEntity<List<StudentRecord>> getStudentRecords(
            @RequestParam(value = "schoolId", required = false) Long schoolId,
            @RequestParam(value = "lga", required = false) String lga) {
        String officerCode = getOfficerCode();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        List<StudentRecord> records = educationalOfficerService.getStudentRecords(schoolId, lga, officerCode, auth);
        return ResponseEntity.ok(records);
    }

    @GetMapping("/students/{id}")
    public ResponseEntity<?> getStudentRecordById(@PathVariable Long id) {
        String officerCode = getOfficerCode();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        StudentRecord record = educationalOfficerService.getStudentRecordById(id, officerCode, auth);
        return ResponseEntity.ok(record);
    }

    @PutMapping("/students/{id}")
    public ResponseEntity<?> updateStudentRecord(
            @PathVariable Long id,
            @RequestBody StudentRecordUpdateRequest request) {
        String officerCode = getOfficerCode();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        StudentRecord updatedData = new StudentRecord();
        updatedData.setStudentName(request.getStudentName());
        updatedData.setStateOfOrigin(request.getStateOfOrigin());
        updatedData.setHealthConditionsSpecialNeeds(request.getHealthConditionsSpecialNeeds());
        updatedData.setAttendanceRate(request.getAttendanceRate());
        updatedData.setFamilyGuardianBackground(request.getFamilyGuardianBackground());
        updatedData.setClassGrade(request.getClassGrade());
        updatedData.setAcademicYear(request.getAcademicYear());

        StudentRecord updated = educationalOfficerService.updateStudentRecord(id, updatedData, officerCode, auth);
        return ResponseEntity.ok(Map.of("message", "Student record updated successfully.", "record", updated));
    }

    // ─── Jurisdiction Assignment Endpoints ─────────────────────────────────────────

    @PostMapping("/assignments")
    public ResponseEntity<?> assignOfficerToSchool(@Valid @RequestBody OfficerAssignmentRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!educationalOfficerService.isSuperAdmin(auth)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Forbidden. Only super administrators can assign officers to schools."));
        }

        OfficerSchoolAssignment assignment = educationalOfficerService.assignOfficerToSchool(
                request.getOfficerCode(),
                request.getSchoolId(),
                request.getSchoolName(),
                request.getLga(),
                request.getState()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(assignment);
    }

    @GetMapping("/assignments/my")
    public ResponseEntity<List<OfficerSchoolAssignment>> getMyAssignments() {
        String officerCode = getOfficerCode();
        return ResponseEntity.ok(educationalOfficerService.getOfficerAssignments(officerCode));
    }

    @GetMapping("/assignments")
    public ResponseEntity<List<OfficerSchoolAssignment>> getAssignments(
            @RequestParam(value = "officerCode", required = false) String officerCode) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!educationalOfficerService.isSuperAdmin(auth)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        if (officerCode != null && !officerCode.isBlank()) {
            return ResponseEntity.ok(educationalOfficerService.getOfficerAssignments(officerCode.trim()));
        }
        return ResponseEntity.ok(educationalOfficerService.getAllActiveAssignments());
    }

    // ─── DTOs ─────────────────────────────────────────────────────────────────────

    @Getter
    @Setter
    public static class SchoolRequest {
        @NotBlank(message = "School name is required")
        private String name;

        @NotBlank(message = "LGA is required")
        private String lga;

        private String state;

        @NotNull(message = "Pupil count is required")
        @Positive(message = "Total pupil count must be greater than zero")
        private Integer pupilCount;

        private String schoolType;
        private String address;
    }

    @Getter
    @Setter
    public static class StudentRecordRequest {
        @NotBlank(message = "Citizen code is required")
        private String citizenCode;

        @NotNull(message = "School ID is required")
        private Long schoolId;

        private String schoolName;

        @NotBlank(message = "LGA is required")
        private String lga;

        private String studentName;
        private String stateOfOrigin;
        private String healthConditionsSpecialNeeds;

        @DecimalMin(value = "0.00", message = "Attendance rate cannot be less than 0%")
        @DecimalMax(value = "100.00", message = "Attendance rate cannot exceed 100%")
        private BigDecimal attendanceRate;

        private String familyGuardianBackground;
        private String academicYear;
        private String classGrade;
    }

    @Getter
    @Setter
    public static class StudentRecordUpdateRequest {
        private String studentName;
        private String stateOfOrigin;
        private String healthConditionsSpecialNeeds;

        @DecimalMin(value = "0.00", message = "Attendance rate cannot be less than 0%")
        @DecimalMax(value = "100.00", message = "Attendance rate cannot exceed 100%")
        private BigDecimal attendanceRate;

        private String familyGuardianBackground;
        private String academicYear;
        private String classGrade;
    }

    @Getter
    @Setter
    public static class OfficerAssignmentRequest {
        @NotBlank(message = "Officer code is required")
        private String officerCode;

        @NotNull(message = "School ID is required")
        private Long schoolId;

        private String schoolName;

        @NotBlank(message = "LGA is required")
        private String lga;

        private String state;
    }
}
