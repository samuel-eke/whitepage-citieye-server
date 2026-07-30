package com.goldbyte.citieye_server.fieldofficer.internal.controller;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller exposing REST endpoints for field officers to view and interact with citizens.
 * All endpoints are restricted to field officer and admin roles.
 *
 * <ul>
 *   <li>{@code GET /api/field-officers/citizens} — returns ALL citizens registered on the application (paginated)</li>
 *   <li>{@code GET /api/field-officers/citizens/{citizenCode}} — returns citizen profile details filtered by officer domain/specialty</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/field-officers/citizens")
public class FieldOfficerCitizenController {

    @PersistenceContext
    private EntityManager em;

    // ── List all citizens (no state/LGA filtering, showing all registered users) ─────────────────

    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<?> listAllCitizens(
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) String cohortName,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String lga,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {

        if (size > 100) size = 100;

        String officerCode = SecurityContextHolder.getContext().getAuthentication().getName();
        if (officerCode == null || "anonymousUser".equalsIgnoreCase(officerCode)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Unauthorized. Please log in."));
        }

        OfficerDomainInfo officerInfo = resolveOfficerInfo(officerCode);
        if (officerInfo == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Could not resolve officer details."));
        }

        boolean isSuperAdmin = officerInfo.role().contains("super");

        StringBuilder sql = new StringBuilder(
                "SELECT citizen_code, first_name, middle_name, last_name, " +
                "       state_of_origin, state_of_residence, lga, residence_lga, gender, age, cohort_name, registered_at " +
                "FROM analytics.citizen_summary WHERE 1=1 "
        );
        StringBuilder countSql = new StringBuilder(
                "SELECT COUNT(*) FROM analytics.citizen_summary WHERE 1=1 "
        );

        if (!isSuperAdmin) {
            sql.append("AND state_of_residence = :officerState ");
            countSql.append("AND state_of_residence = :officerState ");
        }
        if (gender != null && !gender.isBlank()) {
            sql.append("AND gender = :gender ");
            countSql.append("AND gender = :gender ");
        }
        if (cohortName != null && !cohortName.isBlank()) {
            sql.append("AND cohort_name ILIKE :cohortName ");
            countSql.append("AND cohort_name ILIKE :cohortName ");
        }
        if (lga != null && !lga.isBlank()) {
            sql.append("AND lga ILIKE :lga ");
            countSql.append("AND lga ILIKE :lga ");
        }
        if (query != null && !query.isBlank()) {
            sql.append("AND (first_name ILIKE :query OR last_name ILIKE :query OR citizen_code ILIKE :query) ");
            countSql.append("AND (first_name ILIKE :query OR last_name ILIKE :query OR citizen_code ILIKE :query) ");
        }
        sql.append("ORDER BY registered_at DESC LIMIT :size OFFSET :offset");

        jakarta.persistence.Query listQuery = em.createNativeQuery(sql.toString());
        jakarta.persistence.Query countQuery = em.createNativeQuery(countSql.toString());

        if (!isSuperAdmin) {
            listQuery.setParameter("officerState", officerInfo.stateOfResidence());
            countQuery.setParameter("officerState", officerInfo.stateOfResidence());
        }
        if (gender != null && !gender.isBlank()) {
            listQuery.setParameter("gender", gender);
            countQuery.setParameter("gender", gender);
        }
        if (cohortName != null && !cohortName.isBlank()) {
            listQuery.setParameter("cohortName", "%" + cohortName + "%");
            countQuery.setParameter("cohortName", "%" + cohortName + "%");
        }
        if (lga != null && !lga.isBlank()) {
            listQuery.setParameter("lga", "%" + lga.trim() + "%");
            countQuery.setParameter("lga", "%" + lga.trim() + "%");
        }
        if (query != null && !query.isBlank()) {
            String likeParam = "%" + query.trim() + "%";
            listQuery.setParameter("query", likeParam);
            countQuery.setParameter("query", likeParam);
        }

        listQuery.setParameter("size", size);
        listQuery.setParameter("offset", (long) page * size);

        @SuppressWarnings("unchecked")
        List<Object[]> rows = listQuery.getResultList();
        long total = ((Number) countQuery.getSingleResult()).longValue();

        List<Map<String, Object>> items = rows.stream().map(r -> {
            String fullName = buildFullName((String) r[1], (String) r[2], (String) r[3]);
            Map<String, Object> map = new HashMap<>();
            map.put("citizenCode",      r[0] != null ? r[0] : "");
            map.put("fullName",         fullName);
            map.put("firstName",        r[1] != null ? r[1] : "");
            map.put("lastName",         r[3] != null ? r[3] : "");
            map.put("stateOfOrigin",    r[4] != null ? r[4] : "");
            map.put("stateOfResidence", r[5] != null ? r[5] : "");
            map.put("lga",              r[6] != null ? r[6] : "");
            map.put("residenceLga",     r[7] != null ? r[7] : "");
            map.put("gender",           r[8] != null ? r[8] : "");
            map.put("age",              r[9] != null ? r[9] : 0);
            map.put("cohortName",       r[10] != null ? r[10] : "");
            map.put("registeredAt",     r[11] != null ? r[11].toString() : "");
            return map;
        }).toList();

        return ResponseEntity.ok(Map.of(
                "content",      items,
                "page",         page,
                "size",         size,
                "totalElements", total,
                "totalPages",   (int) Math.ceil((double) total / size)
        ));
    }

    // ── Get Citizen Detail (specialty-scoped) ──────────────────────────────

    @GetMapping("/{citizenCode}")
    @Transactional(readOnly = true)
    public ResponseEntity<?> getCitizenDetail(@PathVariable String citizenCode) {
        String officerCode = SecurityContextHolder.getContext().getAuthentication().getName();
        if (officerCode == null || "anonymousUser".equalsIgnoreCase(officerCode)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Unauthorized. Please log in."));
        }

        // Determine officer's specialty/role
        OfficerDomainInfo officerInfo = resolveOfficerInfo(officerCode);
        if (officerInfo == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Could not resolve officer details."));
        }

        // Fetch citizen profile details
        @SuppressWarnings("unchecked")
        List<Object[]> rows = em.createNativeQuery(
                "SELECT " +
                // Identity/Basic (cols 0 - 13)
                "citizen_code, first_name, middle_name, last_name, phone_number, email, " +
                "date_of_birth, age, gender, state_of_origin, state_of_residence, lga, address, registered_at, " +
                // Cohorts & details (cols 14 - 16)
                "cohort_name, age_at_registration, profile_last_updated, " +
                // Health profile (cols 17 - 31)
                "is_expectant_parent, is_pregnant, blood_group, blood_genotype, birth_weight_kg, " +
                "delivery_assistant_type, exclusive_breastfeeding_months, vaccination_record_completed, missing_vaccines, nhis_enrolled, " +
                "chronic_health_registry, cardio_screening_date, hypertension_diabetes_flag, chronic_illness_diagnoses, mobility_level, " +
                // Education profile (cols 32 - 47)
                "current_educational_level, highest_educational_qualification, course_of_study, university_attended, degree_attained, " +
                "year_of_graduation, school_name, current_class_grade, finished_primary_6, has_first_school_leaving_cert, " +
                "fslc_grade, school_meals_recipient, literacy_score_percent, numeracy_score_percent, average_attendance_rate, " +
                "vision_hearing_pass, " +
                // Secondary Education (cols 48 - 56)
                "sec_enrollment_status, annual_grade_average, registered_waec, waec_grades, registered_neco, " +
                "neco_grades, exam_subsidy_qualified, preferred_career_track, digital_literacy_cert, " +
                // Tertiary/Higher Education (cols 57 - 67)
                "tertiary_institution_name, tertiary_institution_code, nelfund_student_loan, cgpa, expected_graduation_year, " +
                "nysc_callup_number, nysc_posting_state, nysc_ppa_address, vocational_licensing, extracurricular_activities, " +
                "exploitation_abuse_reported, " +
                // General Profile fields (cols 68 - 72)
                "current_employment_status, place_of_employment_business, nature_of_business_job, name_of_employer_business, household_size, residence_lga " +
                "FROM analytics.citizen_summary WHERE citizen_code = :code"
        ).setParameter("code", citizenCode.trim()).getResultList();

        if (rows.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "No citizen found with code: " + citizenCode));
        }

        Object[] r = rows.get(0);

        // Geographically scoped access check
        String citizenState = safeStr(r[10]);
        boolean isSuperAdmin = officerInfo.role().contains("super");
        if (!isSuperAdmin && !officerInfo.stateOfResidence().equalsIgnoreCase(citizenState)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Forbidden. You can only view citizens in your state of residence: " + officerInfo.stateOfResidence()));
        }

        Map<String, Object> responseMap = new HashMap<>();

        // 1. Add Basic Identity (All officers get this)
        responseMap.put("citizenCode",             safeStr(r[0]));
        responseMap.put("firstName",               safeStr(r[1]));
        responseMap.put("middleName",              safeStr(r[2]));
        responseMap.put("lastName",                safeStr(r[3]));
        responseMap.put("phoneNumber",             safeStr(r[4]));
        responseMap.put("email",                   safeStr(r[5]));
        responseMap.put("dateOfBirth",             r[6] != null ? r[6].toString() : "");
        responseMap.put("age",                     r[7] != null ? r[7] : 0);
        responseMap.put("gender",                  safeStr(r[8]));
        responseMap.put("stateOfOrigin",           safeStr(r[9]));
        responseMap.put("stateOfResidence",        safeStr(r[10]));
        responseMap.put("lga",                     safeStr(r[11]));
        responseMap.put("residenceLga",             safeStr(r[73]));
        responseMap.put("address",                 safeStr(r[12]));
        responseMap.put("registeredAt",            r[13] != null ? r[13].toString() : "");
        responseMap.put("cohortName",              safeStr(r[14]));
        responseMap.put("ageAtRegistration",       r[15] != null ? r[15] : 0);
        responseMap.put("profileLastUpdated",      r[16] != null ? r[16].toString() : "");

        boolean isHealthSpec = "MEDICAL".equalsIgnoreCase(officerInfo.specialty()) || 
                               "HEALTH".equalsIgnoreCase(officerInfo.specialty()) || 
                               officerInfo.role().contains("health");
                               
        boolean isEdSpec = "EDUCATION".equalsIgnoreCase(officerInfo.specialty()) || 
                           officerInfo.role().contains("education");

        // Admin roles (agency_admin, super_admin) get all info
        boolean isAdmin = officerInfo.role().contains("admin") || officerInfo.role().contains("super");

        if (isAdmin) {
            isHealthSpec = true;
            isEdSpec = true;
        }

        // 2. Add Health details if officer specialty matches or admin
        if (isHealthSpec) {
            responseMap.put("isExpectantParent",       r[17]);
            responseMap.put("isPregnant",              r[18]);
            responseMap.put("bloodGroup",              safeStr(r[19]));
            responseMap.put("bloodGenotype",           safeStr(r[20]));
            responseMap.put("birthWeightKg",           r[21]);
            responseMap.put("deliveryAssistantType",   safeStr(r[22]));
            responseMap.put("exclusiveBreastfeedingMonths", r[23]);
            responseMap.put("vaccinationCompleted",    r[24]);
            responseMap.put("missingVaccines",         safeStr(r[25]));
            responseMap.put("nhisEnrolled",            r[26]);
            responseMap.put("chronicHealthRegistry",   r[27]);
            responseMap.put("cardioScreeningDate",     r[28] != null ? r[28].toString() : "");
            responseMap.put("hypertensionDiabetesFlag", r[29]);
            responseMap.put("chronicIllnessDiagnoses",  safeStr(r[30]));
            responseMap.put("mobilityLevel",           safeStr(r[31]));
        }

        // 3. Add Education details if officer specialty matches or admin
        if (isEdSpec) {
            responseMap.put("educationalLevel",        safeStr(r[32]));
            responseMap.put("highestQualification",    safeStr(r[33]));
            responseMap.put("courseOfStudy",           safeStr(r[34]));
            responseMap.put("universityAttended",      safeStr(r[35]));
            responseMap.put("degreeAttained",          safeStr(r[36]));
            responseMap.put("yearOfGraduation",        r[37]);
            responseMap.put("schoolName",              safeStr(r[38]));
            responseMap.put("currentClassGrade",       safeStr(r[39]));
            responseMap.put("finishedPrimary6",        r[40]);
            responseMap.put("hasFirstSchoolLeavingCert", r[41]);
            responseMap.put("fslcGrade",               safeStr(r[42]));
            responseMap.put("schoolMealsRecipient",    r[43]);
            responseMap.put("literacyScorePercent",    r[44]);
            responseMap.put("numeracyScorePercent",    r[45]);
            responseMap.put("averageAttendanceRate",   r[46]);
            responseMap.put("visionHearingPass",       r[47]);
            
            responseMap.put("secEnrollmentStatus",     safeStr(r[48]));
            responseMap.put("annualGradeAverage",      r[49]);
            responseMap.put("registeredWaec",          r[50]);
            responseMap.put("waecGrades",              safeStr(r[51]));
            responseMap.put("registeredNeco",          r[52]);
            responseMap.put("necoGrades",              safeStr(r[53]));
            responseMap.put("examSubsidyQualified",    r[54]);
            responseMap.put("preferredCareerTrack",    safeStr(r[55]));
            responseMap.put("digitalLiteracyCert",     r[56]);
            
            responseMap.put("tertiaryInstitutionName",  safeStr(r[57]));
            responseMap.put("tertiaryInstitutionCode",  safeStr(r[58]));
            responseMap.put("nelfundStudentLoan",      r[59]);
            responseMap.put("cgpa",                    r[60]);
            responseMap.put("expectedGraduationYear",  r[61]);
            responseMap.put("nyscCallupNumber",        safeStr(r[62]));
            responseMap.put("nyscPostingState",        safeStr(r[63]));
            responseMap.put("nyscPpaAddress",          safeStr(r[64]));
            responseMap.put("vocationalLicensing",     safeStr(r[65]));
            responseMap.put("extracurricularActivities", safeStr(r[66]));
            responseMap.put("exploitationAbuseReported", r[67]);
        }

        // 4. General fields (always include or include for general officers/admins)
        if (!isHealthSpec && !isEdSpec) {
            responseMap.put("employmentStatus",        safeStr(r[68]));
            responseMap.put("placeOfEmployment",       safeStr(r[69]));
            responseMap.put("natureOfBusinessJob",     safeStr(r[70]));
            responseMap.put("nameOfEmployerBusiness",  safeStr(r[71]));
            responseMap.put("householdSize",           r[72]);
        }

        return ResponseEntity.ok(responseMap);
    }

    // ── Helper methods ─────────────────────────────────────────────────────

    private OfficerDomainInfo resolveOfficerInfo(String officerCode) {
        @SuppressWarnings("unchecked")
        List<Object[]> rows = em.createNativeQuery(
                "SELECT specialty, role::text, state_of_residence::text " +
                "FROM auth.admin_user WHERE citizen_code = :code"
        ).setParameter("code", officerCode).getResultList();

        if (rows.isEmpty()) return null;
        Object[] row = rows.get(0);
        return new OfficerDomainInfo(
                row[0] != null ? (String) row[0] : "",
                row[1] != null ? (String) row[1] : "",
                row[2] != null ? (String) row[2] : ""
        );
    }

    private String safeStr(Object val) {
        return val != null ? (String) val : "";
    }

    private String buildFullName(String first, String middle, String last) {
        StringBuilder sb = new StringBuilder();
        if (first  != null) sb.append(first).append(" ");
        if (middle != null && !middle.isBlank()) sb.append(middle).append(" ");
        if (last   != null) sb.append(last);
        return sb.toString().trim();
    }

    private record OfficerDomainInfo(String specialty, String role, String stateOfResidence) {}
}
