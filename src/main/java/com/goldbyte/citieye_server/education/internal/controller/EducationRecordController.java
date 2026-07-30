package com.goldbyte.citieye_server.education.internal.controller;

import com.goldbyte.citieye_server.citizen_profile.CitizenProfilePublicService;
import com.goldbyte.citieye_server.citizen_profile.CitizenTargetingParams;
import com.goldbyte.citieye_server.education.internal.model.EducationRecord;
import com.goldbyte.citieye_server.education.internal.repo.EducationRecordRepository;
import com.goldbyte.citieye_server.education.internal.service.EducationQuestionRegistry;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/field-officers/education")
public class EducationRecordController {

    @PersistenceContext
    private EntityManager em;

    private final EducationRecordRepository educationRecordRepository;
    private final CitizenProfilePublicService citizenProfilePublicService;
    private final EducationQuestionRegistry educationQuestionRegistry;

    public EducationRecordController(EducationRecordRepository educationRecordRepository,
                                     CitizenProfilePublicService citizenProfilePublicService,
                                     EducationQuestionRegistry educationQuestionRegistry) {
        this.educationRecordRepository = educationRecordRepository;
        this.citizenProfilePublicService = citizenProfilePublicService;
        this.educationQuestionRegistry = educationQuestionRegistry;
    }

    private record OfficerStateInfo(String role, String stateOfResidence) {}

    private OfficerStateInfo resolveOfficerInfo(String officerCode) {
        @SuppressWarnings("unchecked")
        List<Object[]> rows = em.createNativeQuery(
                "SELECT role::text, state_of_residence::text FROM auth.admin_user WHERE citizen_code = :code"
        ).setParameter("code", officerCode).getResultList();
        if (rows.isEmpty()) return null;
        Object[] row = rows.get(0);
        return new OfficerStateInfo((String) row[0], (String) row[1]);
    }

    @GetMapping("/questions")
    public ResponseEntity<?> getQuestions(@RequestParam("type") String type) {
        try {
            return ResponseEntity.ok(educationQuestionRegistry.getQuestionsForType(type));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/records/{citizenCode}")
    public ResponseEntity<?> getEducationRecords(@PathVariable String citizenCode) {
        if (citizenCode == null || citizenCode.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Citizen code is required."));
        }

        String officerCode = SecurityContextHolder.getContext().getAuthentication().getName();
        if (officerCode == null || "anonymousUser".equalsIgnoreCase(officerCode)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Unauthorized. Please log in."));
        }

        OfficerStateInfo officerInfo = resolveOfficerInfo(officerCode);
        if (officerInfo == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Could not resolve officer details."));
        }

        CitizenTargetingParams citizenParams = citizenProfilePublicService.getTargetingParams(citizenCode);
        if (citizenParams.getStateResidence() == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Citizen profile not found."));
        }

        boolean isSuperAdmin = officerInfo.role().contains("super");
        if (!isSuperAdmin && !officerInfo.stateOfResidence().equalsIgnoreCase(citizenParams.getStateResidence())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Forbidden. You can only access records for citizens in your state of residence: " + officerInfo.stateOfResidence()));
        }

        List<EducationRecord> records = educationRecordRepository.findAllByCitizenCode(citizenCode.trim());
        return ResponseEntity.ok(records);
    }

    @PostMapping("/records")
    public ResponseEntity<?> logEducationRecord(@Valid @RequestBody EducationRecordRequest request) {
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

        OfficerStateInfo officerInfo = resolveOfficerInfo(officerCode);
        if (officerInfo == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Could not resolve officer details."));
        }

        CitizenTargetingParams citizenParams = citizenProfilePublicService.getTargetingParams(request.getCitizenCode());
        boolean isSuperAdmin = officerInfo.role().contains("super");
        if (!isSuperAdmin && !officerInfo.stateOfResidence().equalsIgnoreCase(citizenParams.getStateResidence())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Forbidden. You can only log records for citizens in your state of residence: " + officerInfo.stateOfResidence()));
        }

        // Create and save education record
        EducationRecord record = new EducationRecord();
        record.setCitizenCode(request.getCitizenCode().trim());
        record.setOfficerCode(officerCode);
        record.setRecordType(request.getRecordType().trim().toUpperCase());
        record.setInstitutionName(request.getInstitutionName().trim());
        record.setAcademicYear(request.getAcademicYear().trim());
        record.setClassGrade(request.getClassGrade() != null ? request.getClassGrade().trim() : null);
        record.setSubjectGrades(request.getSubjectGrades());
        record.setAttendanceRate(request.getAttendanceRate());
        record.setLoggedAt(OffsetDateTime.now());

        // Map optional Primary School questionnaire fields
        record.setPrimaryClassGrade(request.getPrimaryClassGrade());
        record.setPrimaryAttendanceRateTier(request.getPrimaryAttendanceRateTier());
        record.setPrimaryEnglishLiteracyPass(request.getPrimaryEnglishLiteracyPass());
        record.setPrimaryBasicNumeracyPass(request.getPrimaryBasicNumeracyPass());
        record.setPrimaryMaterialsAccess(request.getPrimaryMaterialsAccess());
        record.setPrimaryClassRepeating(request.getPrimaryClassRepeating());
        record.setPrimaryEverDroppedOut(request.getPrimaryEverDroppedOut());
        record.setPrimarySchoolFeedingRecipient(request.getPrimarySchoolFeedingRecipient());
        record.setPrimaryAbsentDueToHunger(request.getPrimaryAbsentDueToHunger());
        record.setPrimaryCommuteDistance(request.getPrimaryCommuteDistance());
        record.setPrimaryGuardianEducation(request.getPrimaryGuardianEducation());
        record.setPrimaryGuardianHomeworkSupport(request.getPrimaryGuardianHomeworkSupport());
        record.setPrimarySiblingInSchoolCount(request.getPrimarySiblingInSchoolCount());
        record.setPrimarySubjectStrengths(request.getPrimarySubjectStrengths());
        record.setPrimaryVocationalInterest(request.getPrimaryVocationalInterest());

        // Map optional Secondary School questionnaire fields
        record.setSecSchoolType(request.getSecSchoolType());
        record.setSecEnrolled(request.getSecEnrolled());
        record.setSecAttendanceRateTier(request.getSecAttendanceRateTier());
        record.setSecSuspensionHistory(request.getSecSuspensionHistory());
        record.setSecClassRepeating(request.getSecClassRepeating());
        record.setSecExamsRegistered(request.getSecExamsRegistered());
        record.setSecExamTutoringAccess(request.getSecExamTutoringAccess());
        record.setSecPostSecondaryPlan(request.getSecPostSecondaryPlan());
        record.setSecCareerInterests(request.getSecCareerInterests());
        record.setSecGuardianEmployment(request.getSecGuardianEmployment());
        record.setSecFeesPaidInFull(request.getSecFeesPaidInFull());
        record.setSecWorkingWhileInSchool(request.getSecWorkingWhileInSchool());
        record.setSecDailyChoresBurden(request.getSecDailyChoresBurden());
        record.setSecAttendanceBarriers(request.getSecAttendanceBarriers());
        record.setSecCounselorAccess(request.getSecCounselorAccess());
        record.setSecDropoutPressureReasons(request.getSecDropoutPressureReasons());
        record.setSecExtracurricularActivities(request.getSecExtracurricularActivities());
        record.setSecDigitalLiteracyCheck(request.getSecDigitalLiteracyCheck());
        record.setSecSpokenLanguages(request.getSecSpokenLanguages());

        EducationRecord saved = educationRecordRepository.save(record);

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "message", "Education record logged successfully.",
                "recordId", saved.getId()
        ));
    }

    @PutMapping("/records/{recordId}")
    public ResponseEntity<?> updateEducationRecord(@PathVariable Long recordId, @Valid @RequestBody EducationRecordRequest request) {
        // Enforce admin privileges
        boolean hasEditPrivilege = SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                .stream()
                .anyMatch(a -> a.getAuthority().equals("super_admin") || a.getAuthority().equals("agency_admin") || a.getAuthority().equals("EDIT_EDUCATION_RECORD"));

        if (!hasEditPrivilege) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Forbidden. You do not have permissions to edit education records."));
        }

        return educationRecordRepository.findById(recordId)
                .map(record -> {
                    record.setRecordType(request.getRecordType().trim().toUpperCase());
                    record.setInstitutionName(request.getInstitutionName().trim());
                    record.setAcademicYear(request.getAcademicYear().trim());
                    record.setClassGrade(request.getClassGrade() != null ? request.getClassGrade().trim() : null);
                    record.setSubjectGrades(request.getSubjectGrades());
                    record.setAttendanceRate(request.getAttendanceRate());

                    // Update Primary School questionnaire fields
                    record.setPrimaryClassGrade(request.getPrimaryClassGrade());
                    record.setPrimaryAttendanceRateTier(request.getPrimaryAttendanceRateTier());
                    record.setPrimaryEnglishLiteracyPass(request.getPrimaryEnglishLiteracyPass());
                    record.setPrimaryBasicNumeracyPass(request.getPrimaryBasicNumeracyPass());
                    record.setPrimaryMaterialsAccess(request.getPrimaryMaterialsAccess());
                    record.setPrimaryClassRepeating(request.getPrimaryClassRepeating());
                    record.setPrimaryEverDroppedOut(request.getPrimaryEverDroppedOut());
                    record.setPrimarySchoolFeedingRecipient(request.getPrimarySchoolFeedingRecipient());
                    record.setPrimaryAbsentDueToHunger(request.getPrimaryAbsentDueToHunger());
                    record.setPrimaryCommuteDistance(request.getPrimaryCommuteDistance());
                    record.setPrimaryGuardianEducation(request.getPrimaryGuardianEducation());
                    record.setPrimaryGuardianHomeworkSupport(request.getPrimaryGuardianHomeworkSupport());
                    record.setPrimarySiblingInSchoolCount(request.getPrimarySiblingInSchoolCount());
                    record.setPrimarySubjectStrengths(request.getPrimarySubjectStrengths());
                    record.setPrimaryVocationalInterest(request.getPrimaryVocationalInterest());

                    // Update Secondary School questionnaire fields
                    record.setSecSchoolType(request.getSecSchoolType());
                    record.setSecEnrolled(request.getSecEnrolled());
                    record.setSecAttendanceRateTier(request.getSecAttendanceRateTier());
                    record.setSecSuspensionHistory(request.getSecSuspensionHistory());
                    record.setSecClassRepeating(request.getSecClassRepeating());
                    record.setSecExamsRegistered(request.getSecExamsRegistered());
                    record.setSecExamTutoringAccess(request.getSecExamTutoringAccess());
                    record.setSecPostSecondaryPlan(request.getSecPostSecondaryPlan());
                    record.setSecCareerInterests(request.getSecCareerInterests());
                    record.setSecGuardianEmployment(request.getSecGuardianEmployment());
                    record.setSecFeesPaidInFull(request.getSecFeesPaidInFull());
                    record.setSecWorkingWhileInSchool(request.getSecWorkingWhileInSchool());
                    record.setSecDailyChoresBurden(request.getSecDailyChoresBurden());
                    record.setSecAttendanceBarriers(request.getSecAttendanceBarriers());
                    record.setSecCounselorAccess(request.getSecCounselorAccess());
                    record.setSecDropoutPressureReasons(request.getSecDropoutPressureReasons());
                    record.setSecExtracurricularActivities(request.getSecExtracurricularActivities());
                    record.setSecDigitalLiteracyCheck(request.getSecDigitalLiteracyCheck());
                    record.setSecSpokenLanguages(request.getSecSpokenLanguages());

                    educationRecordRepository.save(record);
                    return ResponseEntity.ok(Map.of("message", "Education record updated successfully."));
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "Education record not found with ID: " + recordId)));
    }

    @DeleteMapping("/records/{recordId}")
    public ResponseEntity<?> deleteEducationRecord(@PathVariable Long recordId) {
        // Enforce super admin privileges
        boolean isSuperAdmin = SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                .stream()
                .anyMatch(a -> a.getAuthority().equals("super_admin") || a.getAuthority().equals("DELETE_EDUCATION_RECORD"));

        if (!isSuperAdmin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Forbidden. Only super administrators can delete education records."));
        }

        return educationRecordRepository.findById(recordId)
                .map(record -> {
                    educationRecordRepository.delete(record);
                    return ResponseEntity.ok(Map.of("message", "Education record deleted successfully."));
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "Education record not found with ID: " + recordId)));
    }

    @Getter
    @Setter
    public static class EducationRecordRequest {
        @NotBlank(message = "Citizen code is required")
        @Size(max = 30)
        private String citizenCode;

        @NotBlank(message = "Record type is required")
        @Size(max = 50)
        private String recordType; // ENROLLMENT, EXAM_RESULT, ATTENDANCE

        @NotBlank(message = "Institution name is required")
        @Size(max = 150)
        private String institutionName;

        @NotBlank(message = "Academic year is required")
        @Size(max = 15)
        private String academicYear; // e.g. "2025/2026"

        @Size(max = 20)
        private String classGrade; // e.g. "Primary 6", "SSS 3"

        private Map<String, String> subjectGrades;

        private BigDecimal attendanceRate;

        // --- Primary School Fields (Ages 6-11) ---
        private String primaryClassGrade;
        private String primaryAttendanceRateTier;
        private Boolean primaryEnglishLiteracyPass;
        private Boolean primaryBasicNumeracyPass;
        private Boolean primaryMaterialsAccess;
        private Boolean primaryClassRepeating;
        private Boolean primaryEverDroppedOut;
        private Boolean primarySchoolFeedingRecipient;
        private Boolean primaryAbsentDueToHunger;
        private String primaryCommuteDistance;
        private String primaryGuardianEducation;
        private String primaryGuardianHomeworkSupport;
        private Integer primarySiblingInSchoolCount;
        private String primarySubjectStrengths;
        private String primaryVocationalInterest;

        // --- Secondary School Fields (Ages 12-18) ---
        private String secSchoolType;
        private Boolean secEnrolled;
        private String secAttendanceRateTier;
        private Boolean secSuspensionHistory;
        private Boolean secClassRepeating;
        private String secExamsRegistered;
        private String secExamTutoringAccess;
        private String secPostSecondaryPlan;
        private String secCareerInterests;
        private String secGuardianEmployment;
        private Boolean secFeesPaidInFull;
        private Boolean secWorkingWhileInSchool;
        private String secDailyChoresBurden;
        private String secAttendanceBarriers;
        private Boolean secCounselorAccess;
        private String secDropoutPressureReasons;
        private Boolean secExtracurricularActivities;
        private Boolean secDigitalLiteracyCheck;
        private String secSpokenLanguages;
    }
}
