package com.goldbyte.citieye_server.citizen_profile.internal.listener;

import com.goldbyte.citieye_server.userauth.ProgressiveQuestion;
import com.goldbyte.citieye_server.userauth.ProgressiveQuestionsQueryEvent;
import com.goldbyte.citieye_server.citizen_profile.AgeCohort;
import com.goldbyte.citieye_server.citizen_profile.internal.model.Profile;
import com.goldbyte.citieye_server.citizen_profile.internal.repo.ProfileRepository;
import com.goldbyte.citieye_server.citizen_profile.internal.service.ProgressiveQuestionRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles the {@link ProgressiveQuestionsQueryEvent} published by the {@code userauth}
 * module at citizen login time.
 * <p>
 * Resolves the citizen's age cohort using {@link AgeCohort#fromAge(int)}, fetches
 * all defined questions for that cohort from the {@link ProgressiveQuestionRegistry},
 * and then filters the list to only return questions where the corresponding
 * field in the citizen's {@link Profile} entity is currently {@code null}
 * (i.e., the citizen has not yet provided that data).
 * </p>
 * <p>
 * The filtered question list is set back onto the event object, and the calling
 * service in {@code userauth} reads it from the event after this listener returns.
 * This is a fully synchronous, same-thread interaction maintaining data integrity
 * within the request lifecycle.
 * </p>
 */
@Component
public class ProgressiveProfileEventListener {

    private static final Logger log = LoggerFactory.getLogger(ProgressiveProfileEventListener.class);

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private ProgressiveQuestionRegistry questionRegistry;

    @EventListener
    public void onQueryProgressiveQuestions(ProgressiveQuestionsQueryEvent event) {
        String citizenCode = event.getCitizenCode();
        int age = event.getAge();

        // 1. Resolve the age cohort
        AgeCohort cohort = AgeCohort.fromAge(age);
        if (cohort == AgeCohort.UNKNOWN) {
            log.warn("Could not resolve age cohort for citizen '{}' with age '{}'", citizenCode, age);
            return;
        }

        // 2. Fetch the citizen's existing profile
        Profile profile = profileRepository.findById(citizenCode).orElse(null);
        if (profile == null) {
            log.warn("No profile found for citizen '{}' during progressive question resolution", citizenCode);
            return;
        }

        // 3. Get all defined questions for this cohort
        List<ProgressiveQuestion> allCohortQuestions = questionRegistry.getQuestionsForCohort(cohort);

        // 4. Filter out questions for fields that are already answered (non-null)
        List<ProgressiveQuestion> pendingQuestions = new ArrayList<>();
        for (ProgressiveQuestion question : allCohortQuestions) {
            if (isFieldNull(profile, question.getFieldName())) {
                pendingQuestions.add(question);
            }
        }

        // 5. Populate the event with the pending questions for the caller to read
        event.setQuestions(pendingQuestions);
        log.debug("Progressive profiling: resolved {} pending question(s) for citizen '{}' in cohort '{}'",
                pendingQuestions.size(), citizenCode, cohort.getLabel());
    }

    /**
     * Checks whether the JPA entity field corresponding to a given
     * snake_case database column name is {@code null} on the profile.
     *
     * @param profile   the loaded {@link Profile} entity
     * @param fieldName the snake_case database column name (e.g., {@code "school_name"})
     * @return {@code true} if the field is {@code null} or cannot be resolved;
     *         {@code false} if the field has a non-null value
     */
    private boolean isFieldNull(Profile profile, String fieldName) {
        try {
            String javaFieldName = snakeToCamel(fieldName);
            Field field = Profile.class.getDeclaredField(javaFieldName);
            field.setAccessible(true);
            return field.get(profile) == null;
        } catch (NoSuchFieldException e) {
            log.warn("Progressive profile field '{}' (Java: '{}') not found on Profile entity — including in pending list",
                    fieldName, snakeToCamel(fieldName));
            return true; // Treat unmapped fields as pending to avoid silently dropping questions
        } catch (IllegalAccessException e) {
            log.error("Cannot access field '{}' on Profile entity", fieldName, e);
            return true;
        }
    }

    /**
     * Converts a snake_case database column name to its camelCase Java field equivalent.
     * Example: {@code "school_name"} → {@code "schoolName"}
     *
     * @param snake the snake_case string
     * @return the corresponding camelCase string
     */
    private String snakeToCamel(String snake) {
        StringBuilder sb = new StringBuilder();
        boolean nextUpper = false;
        for (char c : snake.toCharArray()) {
            if (c == '_') {
                nextUpper = true;
            } else {
                sb.append(nextUpper ? Character.toUpperCase(c) : c);
                nextUpper = false;
            }
        }
        return sb.toString();
    }
}
