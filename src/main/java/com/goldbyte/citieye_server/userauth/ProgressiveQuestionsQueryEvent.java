package com.goldbyte.citieye_server.userauth;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Spring Application Event used to query progressive profile questions
 * for a specific citizen based on their current age cohort.
 * <p>
 * This event is published by the {@code userauth} module after successful login.
 * The {@code citizen_profile} module listens for it, queries the citizen's profile
 * to identify null (unanswered) fields within the appropriate age cohort, and
 * populates the {@code questions} list before returning control to the publisher.
 * </p>
 * <p>
 * Placed in the {@code userauth} public API (same package as {@code CitizenRegisteredEvent})
 * to maintain one-way dependency direction: {@code citizen_profile} → {@code userauth}.
 * This avoids the circular dependency that would arise if placed in {@code citizen_profile}.
 * </p>
 */
@Getter
public class ProgressiveQuestionsQueryEvent {

    /** The unique citizen code identifying the authenticated user. */
    private final String citizenCode;

    /** The citizen's current age in years, used to resolve the correct age cohort. */
    private final int age;

    /**
     * The list of unanswered questions for this citizen's cohort.
     * Populated by the {@code citizen_profile} event listener before the event completes.
     * Empty by default; will remain empty if all cohort fields are already answered.
     */
    @Setter
    private List<ProgressiveQuestion> questions = new ArrayList<>();

    public ProgressiveQuestionsQueryEvent(String citizenCode, int age) {
        this.citizenCode = citizenCode;
        this.age = age;
    }
}
