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

    private static final String SELECT_CITIZEN_COLUMNS =
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
            // General Profile fields (cols 68 - 73)
            "current_employment_status, place_of_employment_business, nature_of_business_job, name_of_employer_business, household_size, residence_lga ";

    // ── List citizens scoped to officer's state with role-specific requisite data ───────────────

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
        if (!isSuperAdmin && (officerInfo.stateOfResidence() == null || officerInfo.stateOfResidence().isBlank())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Forbidden. Officer has no assigned state of residence."));
        }

        StringBuilder sql = new StringBuilder(
                "SELECT " + SELECT_CITIZEN_COLUMNS + "FROM analytics.citizen_summary WHERE 1=1 "
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
            sql.append("AND (lga ILIKE :lga OR residence_lga ILIKE :lga) ");
            countSql.append("AND (lga ILIKE :lga OR residence_lga ILIKE :lga) ");
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
            String lgaParam = "%" + lga.trim() + "%";
            listQuery.setParameter("lga", lgaParam);
            countQuery.setParameter("lga", lgaParam);
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

        List<Map<String, Object>> items = rows.stream()
                .map(r -> mapCitizenRow(r, officerInfo))
                .toList();

        return ResponseEntity.ok(Map.of(
                "content",       items,
                "page",          page,
                "size",          size,
                "totalElements", total,
                "totalPages",    (int) Math.ceil((double) total / size)
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
                "SELECT " + SELECT_CITIZEN_COLUMNS + "FROM analytics.citizen_summary WHERE citizen_code = :code"
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

        return ResponseEntity.ok(mapCitizenRow(r, officerInfo));
    }

    private Map<String, Object> mapCitizenRow(Object[] r, OfficerDomainInfo officerInfo) {
        Map<String, Object> map = new HashMap<>();

        // 1. Basic Identity (All officers get this)
        String fullName = buildFullName(safeStr(r[1]), safeStr(r[2]), safeStr(r[3]));
        map.put("citizenCode",             safeStr(r[0]));
        map.put("fullName",                fullName);
        map.put("firstName",               safeStr(r[1]));
        map.put("middleName",              safeStr(r[2]));
        map.put("lastName",                safeStr(r[3]));
        map.put("phoneNumber",             safeStr(r[4]));
        map.put("email",                   safeStr(r[5]));
        map.put("dateOfBirth",             r[6] != null ? r[6].toString() : "");
        map.put("age",                     r[7] != null ? r[7] : 0);
        map.put("gender",                  safeStr(r[8]));
        map.put("stateOfOrigin",           safeStr(r[9]));
        map.put("stateOfResidence",        safeStr(r[10]));
        map.put("lga",                     safeStr(r[11]));
        map.put("residenceLga",            safeStr(r[73]));
        map.put("address",                 safeStr(r[12]));
        map.put("registeredAt",            r[13] != null ? r[13].toString() : "");
        map.put("cohortName",              safeStr(r[14]));
        map.put("ageAtRegistration",       r[15] != null ? r[15] : 0);
        map.put("profileLastUpdated",      r[16] != null ? r[16].toString() : "");

        boolean isHealthSpec = "MEDICAL".equalsIgnoreCase(officerInfo.specialty()) ||
                               "HEALTH".equalsIgnoreCase(officerInfo.specialty()) ||
                               officerInfo.role().contains("health");

        boolean isEdSpec = "EDUCATION".equalsIgnoreCase(officerInfo.specialty()) ||
                           officerInfo.role().contains("education");

        boolean isAdmin = officerInfo.role().contains("admin") || officerInfo.role().contains("super");

        if (isAdmin) {
            isHealthSpec = true;
            isEdSpec = true;
        }

        // 2. Health details if officer specialty matches or admin
        if (isHealthSpec) {
            map.put("isExpectantParent",              r[17]);
            map.put("isPregnant",                     r[18]);
            map.put("bloodGroup",                     safeStr(r[19]));
            map.put("bloodGenotype",                  safeStr(r[20]));
            map.put("birthWeightKg",                  r[21]);
            map.put("deliveryAssistantType",          safeStr(r[22]));
            map.put("exclusiveBreastfeedingMonths",   r[23]);
            map.put("vaccinationCompleted",           r[24]);
            map.put("missingVaccines",                safeStr(r[25]));
            map.put("nhisEnrolled",                   r[26]);
            map.put("chronicHealthRegistry",          r[27]);
            map.put("cardioScreeningDate",            r[28] != null ? r[28].toString() : "");
            map.put("hypertensionDiabetesFlag",       r[29]);
            map.put("chronicIllnessDiagnoses",        safeStr(r[30]));
            map.put("mobilityLevel",                  safeStr(r[31]));
        }

        // 3. Education details if officer specialty matches or admin
        if (isEdSpec) {
            map.put("educationalLevel",               safeStr(r[32]));
            map.put("highestQualification",           safeStr(r[33]));
            map.put("courseOfStudy",                  safeStr(r[34]));
            map.put("universityAttended",             safeStr(r[35]));
            map.put("degreeAttained",                 safeStr(r[36]));
            map.put("yearOfGraduation",               r[37]);
            map.put("schoolName",                     safeStr(r[38]));
            map.put("currentClassGrade",              safeStr(r[39]));
            map.put("finishedPrimary6",               r[40]);
            map.put("hasFirstSchoolLeavingCert",      r[41]);
            map.put("fslcGrade",                      safeStr(r[42]));
            map.put("schoolMealsRecipient",           r[43]);
            map.put("literacyScorePercent",           r[44]);
            map.put("numeracyScorePercent",           r[45]);
            map.put("averageAttendanceRate",          r[46]);
            map.put("visionHearingPass",              r[47]);

            map.put("secEnrollmentStatus",            safeStr(r[48]));
            map.put("annualGradeAverage",             r[49]);
            map.put("registeredWaec",                 r[50]);
            map.put("waecGrades",                     safeStr(r[51]));
            map.put("registeredNeco",                 r[52]);
            map.put("necoGrades",                     safeStr(r[53]));
            map.put("examSubsidyQualified",           r[54]);
            map.put("preferredCareerTrack",           safeStr(r[55]));
            map.put("digitalLiteracyCert",            r[56]);

            map.put("tertiaryInstitutionName",        safeStr(r[57]));
            map.put("tertiaryInstitutionCode",        safeStr(r[58]));
            map.put("nelfundStudentLoan",             r[59]);
            map.put("cgpa",                           r[60]);
            map.put("expectedGraduationYear",         r[61]);
            map.put("nyscCallupNumber",               safeStr(r[62]));
            map.put("nyscPostingState",               safeStr(r[63]));
            map.put("nyscPpaAddress",                 safeStr(r[64]));
            map.put("vocationalLicensing",            safeStr(r[65]));
            map.put("extracurricularActivities",      safeStr(r[66]));
            map.put("exploitationAbuseReported",      r[67]);
        }

        // 4. General / Employment fields (include for general officers or admins)
        if (isAdmin || (!isHealthSpec && !isEdSpec)) {
            map.put("employmentStatus",               safeStr(r[68]));
            map.put("placeOfEmployment",              safeStr(r[69]));
            map.put("natureOfBusinessJob",            safeStr(r[70]));
            map.put("nameOfEmployerBusiness",         safeStr(r[71]));
            map.put("householdSize",                  r[72]);
        }

        return map;
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
