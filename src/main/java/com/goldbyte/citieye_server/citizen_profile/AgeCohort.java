package com.goldbyte.citieye_server.citizen_profile;

import java.util.Arrays;

/**
 * Enum representing the 11 progressive age cohorts used by CitiEye
 * to conditionally serve profile data-collection questions.
 * <p>
 * Each constant maps a min/max age range to a named cohort. At login,
 * {@link #fromAge(int)} resolves the citizen's current calculated age
 * to the appropriate cohort, which the {@code ProgressiveQuestionRegistry}
 * then uses to return the relevant question set.
 * </p>
 */
public enum AgeCohort {

    EARLY_CHILDHOOD(0, 5, "0–5: Early Childhood Development"),
    FOUNDATIONAL_EDUCATION(6, 11, "6–11: Foundational Education"),
    SECONDARY_EDUCATION(12, 18, "12–18: Secondary Education & Career Prep"),
    HIGHER_EDUCATION(19, 26, "19–26: Higher Education & Skill Acquisition"),
    WORKFORCE_INTEGRATION(27, 32, "27–32: Workforce Integration"),
    CAREER_GROWTH(33, 41, "33–41: Career Growth & Family Stability"),
    MID_LIFE_REINVENTION(42, 44, "42–44: Mid-Life Reinvention"),
    LEGACY_PLANNING(45, 48, "45–48: Legacy & Stability Planning"),
    PRE_RETIREMENT(49, 54, "49–54: Pre-Retirement Transition"),
    ACTIVE_AGING(55, 60, "55–60: Active Aging"),
    ELDERLY_CARE(61, 150, "60–70+: Elderly Care & Support"),
    UNKNOWN(-1, -1, "Unknown Cohort");

    private final int minAge;
    private final int maxAge;
    private final String label;

    AgeCohort(int minAge, int maxAge, String label) {
        this.minAge = minAge;
        this.maxAge = maxAge;
        this.label = label;
    }

    /**
     * Resolves the correct {@code AgeCohort} for a given age.
     *
     * @param age the citizen's current age in years
     * @return the matching {@code AgeCohort}, or {@link #UNKNOWN} if no range matches
     */
    public static AgeCohort fromAge(int age) {
        return Arrays.stream(values())
                .filter(cohort -> age >= cohort.minAge && age <= cohort.maxAge)
                .findFirst()
                .orElse(UNKNOWN);
    }

    public String getLabel() {
        return label;
    }

    public int getMinAge() {
        return minAge;
    }

    public int getMaxAge() {
        return maxAge;
    }
}
