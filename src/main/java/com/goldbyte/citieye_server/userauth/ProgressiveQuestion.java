package com.goldbyte.citieye_server.userauth;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * A UI-ready representation of a single progressive data-collection field.
 * <p>
 * Returned as part of the citizen login response to allow the frontend to
 * dynamically render the appropriate form fields based on the citizen's
 * age cohort, filtered to only fields that are still {@code NULL} in their profile.
 * </p>
 * <p>
 * Fields flagged with {@code piiSensitive = true} require explicit NDPR
 * consent acknowledgment before the user submits the form on the frontend.
 * </p>
 * <p>
 * Placed in the {@code userauth} public API because it is published and consumed
 * by {@code userauth} at login time. The {@code citizen_profile} module reads
 * this type when populating the {@link ProgressiveQuestionsQueryEvent}.
 * </p>
 */
@Getter
@Builder
public class ProgressiveQuestion {

    /**
     * The exact database column name (snake_case) this question maps to.
     * Used by the profile update API to know which column to write to.
     * Example: {@code "school_name"}, {@code "nelfund_student_loan"}
     */
    private final String fieldName;

    /**
     * The expected input type for frontend form rendering.
     * Possible values: {@code "TEXT"}, {@code "BOOLEAN"}, {@code "NUMBER"},
     * {@code "DECIMAL"}, {@code "DATE"}, {@code "SELECT"}
     */
    private final String dataType;

    /**
     * The human-readable question label shown to the citizen in the UI.
     * Example: {@code "What is the name of your school?"}
     */
    private final String label;

    /**
     * Broad grouping category for UI sectioning.
     * Example: {@code "Education"}, {@code "Health"}, {@code "Financial"}, {@code "Safeguarding"}
     */
    private final String category;

    /**
     * Optional list of allowed values for {@code SELECT} type fields.
     * {@code null} for free-text, boolean, decimal, date, and number inputs.
     */
    private final List<String> options;

    /**
     * Whether this field is classified as sensitive under Nigeria's NDPR.
     * {@code true} fields require explicit consent before the frontend submits them.
     */
    private final boolean piiSensitive;
}
