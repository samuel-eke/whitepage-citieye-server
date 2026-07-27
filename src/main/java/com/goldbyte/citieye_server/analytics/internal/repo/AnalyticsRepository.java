package com.goldbyte.citieye_server.analytics.internal.repo;

import com.goldbyte.citieye_server.analytics.internal.dto.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Analytics repository using native SQL queries against materialized views
 * in the {@code analytics} schema. All reads are performed read-only against
 * the pre-computed views; the views themselves are refreshed via
 * {@link #refreshAllMaterializedViews()}.
 */
@Repository
public class AnalyticsRepository {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsRepository.class);

    @PersistenceContext
    private EntityManager em;

    // ── Headline Counts ────────────────────────────────────────────────────

    /**
     * Returns the total number of registered citizens directly from {@code auth.users_auth}.
     * This bypasses the materialized view for accuracy immediately after a new registration.
     */
    public long countTotalCitizens() {
        return ((Number) em
                .createNativeQuery("SELECT COUNT(*) FROM auth.users_auth WHERE role = 'citizen'")
                .getSingleResult()).longValue();
    }

    public long countCitizensRegisteredInLastDays(int days) {
        return ((Number) em
                .createNativeQuery(
                        "SELECT COUNT(*) FROM auth.users_auth " +
                        "WHERE role = 'citizen' AND created_at >= NOW() - CAST(:interval AS INTERVAL)")
                .setParameter("interval", days + " days")
                .getSingleResult()).longValue();
    }

    public long countMaleCitizens() {
        return ((Number) em
                .createNativeQuery(
                        "SELECT COUNT(*) FROM auth.users_auth WHERE role = 'citizen' AND gender::text = 'male'")
                .getSingleResult()).longValue();
    }

    public long countFemaleCitizens() {
        return ((Number) em
                .createNativeQuery(
                        "SELECT COUNT(*) FROM auth.users_auth WHERE role = 'citizen' AND gender::text = 'female'")
                .getSingleResult()).longValue();
    }

    // ── State + LGA Breakdown ──────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    public List<StateBreakdownDto> findStateAndLgaBreakdown() {
        List<Object[]> rows = em.createNativeQuery(
                "SELECT state_of_origin, state_of_residence, lga, " +
                "       total_citizens, male_count, female_count, " +
                "       registered_last_7_days, registered_last_30_days " +
                "FROM analytics.citizen_stats_by_state_lga " +
                "ORDER BY total_citizens DESC"
        ).getResultList();

        List<StateBreakdownDto> result = new ArrayList<>();
        for (Object[] row : rows) {
            result.add(new StateBreakdownDto(
                    (String)  row[0],
                    (String)  row[1],
                    (String)  row[2],
                    toLong(row[3]),
                    toLong(row[4]),
                    toLong(row[5]),
                    toLong(row[6]),
                    toLong(row[7])
            ));
        }
        return result;
    }

    // ── Cohort Breakdown ───────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    public List<CohortBreakdownDto> findCohortBreakdown() {
        List<Object[]> rows = em.createNativeQuery(
                "SELECT cohort_id, cohort_name, min_age, max_age, " +
                "       total_citizens, citizens_with_any_milestone_completed, avg_age_at_registration " +
                "FROM analytics.cohort_population " +
                "ORDER BY min_age ASC"
        ).getResultList();

        List<CohortBreakdownDto> result = new ArrayList<>();
        for (Object[] row : rows) {
            result.add(new CohortBreakdownDto(
                    toInt(row[0]),
                    (String) row[1],
                    row[2] != null ? toInt(row[2]) : null,
                    row[3] != null ? toInt(row[3]) : null,
                    toLong(row[4]),
                    toLong(row[5]),
                    row[6] != null ? ((Number) row[6]).doubleValue() : null
            ));
        }
        return result;
    }

    // ── Employment Snapshot ────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    public List<EmploymentBreakdownDto> findEmploymentBreakdown() {
        List<Object[]> rows = em.createNativeQuery(
                "SELECT employment_status, monthly_income_range, industry_sector, " +
                "       highest_academic_qualification, state_of_origin, lga, citizen_count " +
                "FROM analytics.employment_snapshot " +
                "ORDER BY citizen_count DESC " +
                "LIMIT 200"
        ).getResultList();

        List<EmploymentBreakdownDto> result = new ArrayList<>();
        for (Object[] row : rows) {
            result.add(new EmploymentBreakdownDto(
                    (String) row[0],
                    (String) row[1],
                    (String) row[2],
                    (String) row[3],
                    (String) row[4],
                    (String) row[5],
                    toLong(row[6])
            ));
        }
        return result;
    }

    // ── Health Snapshot ────────────────────────────────────────────────────

    public HealthIndicatorsSummaryDto findHealthIndicatorsSummary() {
        Object[] row = (Object[]) em.createNativeQuery(
                "SELECT " +
                "  SUM(total_citizens_profiled), " +
                "  SUM(vaccination_complete), " +
                "  SUM(pregnant_count), " +
                "  SUM(expectant_parents), " +
                "  SUM(hypertension_diabetes_count), " +
                "  SUM(nhis_enrolled_count), " +
                "  SUM(nhis_senior_plan_count), " +
                "  SUM(blood_group_recorded), " +
                "  SUM(chronic_illness_count) " +
                "FROM analytics.health_snapshot"
        ).getSingleResult();

        return new HealthIndicatorsSummaryDto(
                toLong(row[0]),
                toLong(row[1]),
                toLong(row[2]),
                toLong(row[3]),
                toLong(row[4]),
                toLong(row[5]),
                toLong(row[6]),
                toLong(row[7]),
                toLong(row[8])
        );
    }

    // ── Citizen List (Paginated) ───────────────────────────────────────────

    @SuppressWarnings("unchecked")
    public List<CitizenListItemDto> searchCitizens(String stateOfOrigin, String lga,
                                                   String gender, String cohortName,
                                                   int page, int size) {
        StringBuilder sql = new StringBuilder(
                "SELECT citizen_code, first_name, middle_name, last_name, " +
                "       state_of_origin, state_of_residence, lga, gender, age, cohort_name, registered_at " +
                "FROM analytics.citizen_summary WHERE 1=1 "
        );

        if (stateOfOrigin != null && !stateOfOrigin.isBlank()) {
            sql.append("AND state_of_origin = :stateOfOrigin ");
        }
        if (lga != null && !lga.isBlank()) {
            sql.append("AND lga ILIKE :lga ");
        }
        if (gender != null && !gender.isBlank()) {
            sql.append("AND gender = :gender ");
        }
        if (cohortName != null && !cohortName.isBlank()) {
            sql.append("AND cohort_name ILIKE :cohortName ");
        }

        sql.append("ORDER BY registered_at DESC LIMIT :size OFFSET :offset");

        Query query = em.createNativeQuery(sql.toString());
        if (stateOfOrigin != null && !stateOfOrigin.isBlank()) query.setParameter("stateOfOrigin", stateOfOrigin);
        if (lga            != null && !lga.isBlank())           query.setParameter("lga", "%" + lga + "%");
        if (gender         != null && !gender.isBlank())        query.setParameter("gender", gender);
        if (cohortName     != null && !cohortName.isBlank())    query.setParameter("cohortName", "%" + cohortName + "%");
        query.setParameter("size", size);
        query.setParameter("offset", (long) page * size);

        List<Object[]> rows = query.getResultList();
        List<CitizenListItemDto> result = new ArrayList<>();
        for (Object[] row : rows) {
            String fullName = buildFullName((String) row[1], (String) row[2], (String) row[3]);
            result.add(new CitizenListItemDto(
                    (String)       row[0],
                    fullName,
                    (String)       row[4],
                    (String)       row[5],
                    (String)       row[6],
                    (String)       row[7],
                    row[8] != null ? toInt(row[8]) : null,
                    (String)       row[9],
                    toOffsetDateTime(row[10])
            ));
        }
        return result;
    }

    public long countSearchCitizens(String stateOfOrigin, String lga, String gender, String cohortName) {
        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(*) FROM analytics.citizen_summary WHERE 1=1 "
        );
        if (stateOfOrigin != null && !stateOfOrigin.isBlank()) sql.append("AND state_of_origin = :stateOfOrigin ");
        if (lga            != null && !lga.isBlank())           sql.append("AND lga ILIKE :lga ");
        if (gender         != null && !gender.isBlank())        sql.append("AND gender = :gender ");
        if (cohortName     != null && !cohortName.isBlank())    sql.append("AND cohort_name ILIKE :cohortName ");

        Query query = em.createNativeQuery(sql.toString());
        if (stateOfOrigin != null && !stateOfOrigin.isBlank()) query.setParameter("stateOfOrigin", stateOfOrigin);
        if (lga            != null && !lga.isBlank())           query.setParameter("lga", "%" + lga + "%");
        if (gender         != null && !gender.isBlank())        query.setParameter("gender", gender);
        if (cohortName     != null && !cohortName.isBlank())    query.setParameter("cohortName", "%" + cohortName + "%");

        return toLong(query.getSingleResult());
    }

    // ── Full Citizen Profile Detail ────────────────────────────────────────

    @SuppressWarnings("unchecked")
    public Optional<CitizenProfileAnalyticsDto> findCitizenProfileDetail(String citizenCode) {
        List<Object[]> rows = em.createNativeQuery(
                "SELECT " +
                // Identity (cols 0-13)
                "citizen_code, first_name, middle_name, last_name, phone_number, email, " +
                "date_of_birth, age, gender, state_of_origin, state_of_residence, lga, address, registered_at, " +
                // Cohort (cols 14-16)
                "cohort_name, age_at_registration, cohort_last_transitioned_at, " +
                // Basic profile (cols 17-32)
                "hometown, current_educational_level, highest_educational_qualification, course_of_study, " +
                "university_attended, degree_attained, year_of_graduation, " +
                "is_expectant_parent, household_size, is_pregnant, blood_group, blood_genotype, " +
                "current_employment_status, place_of_employment_business, name_of_employer_business, nature_of_business_job, " +
                // Early childhood (cols 33-40)
                "birth_weight_kg, delivery_assistant_type, exclusive_breastfeeding_months, " +
                "vaccination_record_completed, missing_vaccines, is_registered_birth, parent_guardian_nin, preschool_enrollment, " +
                // Foundational Ed (cols 41-51)
                "school_name, current_class_grade, finished_primary_6, has_first_school_leaving_cert, " +
                "fslc_grade, school_meals_recipient, literacy_score_percent, numeracy_score_percent, " +
                "average_attendance_rate, vision_hearing_pass, commute_distance_km, " +
                // Secondary Ed (cols 52-63)
                "sec_enrollment_status, annual_grade_average, registered_waec, waec_grades, " +
                "registered_neco, neco_grades, exam_subsidy_qualified, preferred_career_track, " +
                "digital_literacy_cert, menstrual_kit_requested, school_fee_indigent_status, safeguarding_issue_reported, " +
                // Higher Ed (cols 64-74)
                "tertiary_institution_name, tertiary_institution_code, nelfund_student_loan, cgpa, " +
                "expected_graduation_year, nysc_callup_number, nysc_posting_state, nysc_ppa_address, " +
                "vocational_licensing, extracurricular_activities, exploitation_abuse_reported, " +
                // Workforce (cols 75-83)
                "employment_status, highest_academic_qualification, tin_number, bvn_number, " +
                "monthly_income_range, nhis_enrolled, industry_sector, business_registration_number, nysc_discharge_cert, " +
                // Career Growth (cols 84-91)
                "marital_status, dependent_children_count, housing_tenure_status, nhf_contributor, " +
                "cps_pension_pin, farming_land_ownership, cooperative_membership, chronic_health_registry, " +
                // Mid-life (cols 92-99)
                "reskilling_interest, physical_obsolescence_risk, family_education_burden, business_expansion_capital, " +
                "cardio_screening_date, primary_income_source, digital_upskilling_count, interest_in_political_office, " +
                // Legacy (cols 100-106)
                "estate_plan_will_registered, land_title_number, life_insurance_enrolled, elder_dependents_count, " +
                "hypertension_diabetes_flag, secondary_income_sector, cooperative_savings_status, " +
                // Pre-retirement (cols 107-114)
                "expected_retirement_age, years_of_service, retirement_livelihood_plan, pfa_pension_balance_range, " +
                "relocation_post_retirement, annual_geriatric_checkup, mortgage_outstanding, co_dependent_students, " +
                // Active aging (cols 115-121)
                "active_aging_status, senior_mentor_interest, senior_center_membership, mobility_level, " +
                "primary_geriatric_clinic, nhis_senior_plan, social_support_network, " +
                // Elderly (cols 122-129)
                "pension_disbursement_status, primary_caregiver_name, primary_caregiver_phone, " +
                "elderly_living_arrangement, adl_assistance_level, chronic_illness_diagnoses, " +
                "clean_water_electricity, nassp_welfare_registered, " +
                // Timestamps (cols 130)
                "profile_last_updated " +
                "FROM analytics.citizen_summary WHERE citizen_code = :code"
        ).setParameter("code", citizenCode).getResultList();

        if (rows.isEmpty()) return Optional.empty();
        Object[] r = rows.get(0);

        return Optional.of(new CitizenProfileAnalyticsDto(
                (String)  r[0],  (String)  r[1],  (String)  r[2],  (String)  r[3],
                (String)  r[4],  (String)  r[5],
                toLocalDate(r[6]),
                r[7] != null ? toInt(r[7]) : null,
                (String)  r[8],  (String)  r[9],  (String)  r[10], (String)  r[11],
                (String)  r[12], toOffsetDateTime(r[13]),
                (String)  r[14],
                r[15] != null ? toInt(r[15]) : null,
                toOffsetDateTime(r[16]),
                (String)  r[17], (String)  r[18], (String)  r[19], (String)  r[20],
                (String)  r[21], (String)  r[22],
                r[23] != null ? toInt(r[23]) : null,
                (Boolean) r[24],
                r[25] != null ? toInt(r[25]) : null,
                (Boolean) r[26],
                (String)  r[27], (String)  r[28],
                (String)  r[29], (String)  r[30], (String)  r[31], (String)  r[32],
                r[33] != null ? (BigDecimal) r[33] : null,
                (String)  r[34],
                r[35] != null ? toInt(r[35]) : null,
                (Boolean) r[36], (String) r[37], (Boolean) r[38], (String) r[39], (Boolean) r[40],
                (String)  r[41], (String) r[42],
                (Boolean) r[43], (Boolean) r[44],
                (String)  r[45], (Boolean) r[46],
                r[47] != null ? toInt(r[47]) : null,
                r[48] != null ? toInt(r[48]) : null,
                r[49] != null ? toInt(r[49]) : null,
                (Boolean) r[50], r[51] != null ? (BigDecimal) r[51] : null,
                (String)  r[52],
                r[53] != null ? (BigDecimal) r[53] : null,
                (Boolean) r[54], (String) r[55], (Boolean) r[56], (String) r[57],
                (Boolean) r[58], (String) r[59], (Boolean) r[60], (Boolean) r[61],
                (Boolean) r[62], (String) r[63], (String) r[64], (String) r[65],
                (Boolean) r[66], r[67] != null ? (BigDecimal) r[67] : null,
                r[68] != null ? toInt(r[68]) : null,
                (String) r[69], (String) r[70], (String) r[71],
                (String) r[72], (String) r[73], (String) r[74],
                (String)  r[75], (String) r[76], (String) r[77], (String) r[78],
                (String)  r[79], (Boolean) r[80], (String) r[81], (String)  r[82], (String) r[83],
                (String)  r[84],
                r[85] != null ? toInt(r[85]) : null,
                (String)  r[86], (Boolean) r[87], (String) r[88], (Boolean) r[89],
                (String)  r[90], (String)  r[91],
                (Boolean) r[92], (String) r[93],
                r[94] != null ? toInt(r[94]) : null,
                (Boolean) r[95],
                toLocalDate(r[96]), (String) r[97],
                r[98] != null ? toInt(r[98]) : null,
                (Boolean) r[99],
                (Boolean) r[100], (String) r[101], (Boolean) r[102],
                r[103] != null ? toInt(r[103]) : null,
                (Boolean) r[104], (String) r[105], (Boolean) r[106],
                r[107] != null ? toInt(r[107]) : null,
                r[108] != null ? toInt(r[108]) : null,
                (String)  r[109], (String) r[110], (String) r[111],
                toLocalDate(r[112]), (Boolean) r[113],
                r[114] != null ? toInt(r[114]) : null,
                (String)  r[115], (Boolean) r[116], (Boolean) r[117],
                (String)  r[118], (String) r[119], (Boolean) r[120], (String) r[121],
                (String)  r[122], (String) r[123], (String) r[124],
                (String)  r[125], (String) r[126], (String) r[127],
                (Boolean) r[128], (Boolean) r[129],
                toOffsetDateTime(r[130])
        ));
    }

    // ── Materialized View Refresh ──────────────────────────────────────────

    /**
     * Refreshes all analytics materialized views concurrently.
     * CONCURRENTLY allows reads to continue during refresh (requires unique indexes on each view).
     * Should be called after bulk citizen registrations or on a schedule.
     */
    @Transactional
    public void refreshAllMaterializedViews() {
        log.info("Refreshing analytics materialized views...");
        em.createNativeQuery("REFRESH MATERIALIZED VIEW CONCURRENTLY analytics.citizen_summary").executeUpdate();
        em.createNativeQuery("REFRESH MATERIALIZED VIEW CONCURRENTLY analytics.citizen_stats_by_state_lga").executeUpdate();
        em.createNativeQuery("REFRESH MATERIALIZED VIEW CONCURRENTLY analytics.cohort_population").executeUpdate();
        em.createNativeQuery("REFRESH MATERIALIZED VIEW CONCURRENTLY analytics.employment_snapshot").executeUpdate();
        em.createNativeQuery("REFRESH MATERIALIZED VIEW CONCURRENTLY analytics.health_snapshot").executeUpdate();
        log.info("Analytics materialized views refreshed successfully.");
    }

    // ── Private Helpers ───────────────────────────────────────────────────

    private static long toLong(Object val) {
        return val == null ? 0L : ((Number) val).longValue();
    }

    private static int toInt(Object val) {
        return val == null ? 0 : ((Number) val).intValue();
    }

    private static LocalDate toLocalDate(Object val) {
        if (val == null) return null;
        if (val instanceof LocalDate ld) return ld;
        if (val instanceof java.sql.Date d) return d.toLocalDate();
        return null;
    }

    private static OffsetDateTime toOffsetDateTime(Object val) {
        if (val == null) return null;
        if (val instanceof OffsetDateTime odt) return odt;
        if (val instanceof java.sql.Timestamp ts) return ts.toInstant().atOffset(java.time.ZoneOffset.UTC);
        return null;
    }

    private static String buildFullName(String first, String middle, String last) {
        StringBuilder sb = new StringBuilder();
        if (first  != null) sb.append(first).append(" ");
        if (middle != null && !middle.isBlank()) sb.append(middle).append(" ");
        if (last   != null) sb.append(last);
        return sb.toString().trim();
    }
}
