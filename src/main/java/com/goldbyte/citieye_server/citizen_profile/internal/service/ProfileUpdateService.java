package com.goldbyte.citieye_server.citizen_profile.internal.service;

import com.goldbyte.citieye_server.citizen_profile.internal.model.Profile;
import com.goldbyte.citieye_server.citizen_profile.internal.model.enums.EmploymentStatus;
import com.goldbyte.citieye_server.citizen_profile.internal.repo.ProfileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.Set;

/**
 * Service handling safe, reflection-based progressive updates to the {@link Profile} entity.
 * <p>
 * Validates requested field names, handles type conversions (String to Date, Number to BigDecimal, etc.),
 * and enforces read-only guards on system-managed columns like {@code citizenCode}, {@code age}, and {@code updatedAt}.
 * </p>
 */
@Service
public class ProfileUpdateService {

    private static final Logger log = LoggerFactory.getLogger(ProfileUpdateService.class);

    // Protected system columns that citizens cannot directly update
    private static final Set<String> IMMUTABLE_FIELDS = Set.of(
            "citizenCode",
            "citizen_code",
            "age",
            "updatedAt",
            "updated_at"
    );

    @Autowired
    private ProfileRepository profileRepository;

    /**
     * Dynamically updates an existing profile with a map of key-value changes.
     *
     * @param citizenCode the unique code of the citizen whose profile is being updated
     * @param updates     a map of snake_case or camelCase field names to their new values
     * @throws IllegalArgumentException if a field is not recognized or cannot be updated
     */
    @Transactional
    public void updateProfile(String citizenCode, Map<String, Object> updates) {
        if (updates == null || updates.isEmpty()) {
            return;
        }

        Profile profile = profileRepository.findById(citizenCode)
                .orElseThrow(() -> new IllegalArgumentException("Profile not found for citizen: " + citizenCode));

        for (Map.Entry<String, Object> entry : updates.entrySet()) {
            String rawKey = entry.getKey();
            Object rawValue = entry.getValue();

            // Skip immutable system-managed fields
            if (IMMUTABLE_FIELDS.contains(rawKey)) {
                log.warn("Skipping attempt to modify protected field '{}' on citizen '{}' profile", rawKey, citizenCode);
                continue;
            }

            // Map and resolve field name
            String fieldName = resolveFieldName(rawKey);
            updateField(profile, fieldName, rawValue);
        }

        profile.setUpdatedAt(OffsetDateTime.now());
        profileRepository.save(profile);
        log.info("Successfully updated progressive profile for citizen: {}", citizenCode);
    }

    /**
     * Resolves the Java camelCase field name from a snake_case key,
     * handling any specific custom mappings.
     */
    private String resolveFieldName(String rawKey) {
        // Special case handling for property-column mapping differences in Profile.java
        if ("nassp_welfare_registered".equalsIgnoreCase(rawKey) || "nasspWelfareRegistered".equalsIgnoreCase(rawKey)) {
            return "nassWelfareRegistered";
        }
        return snakeToCamel(rawKey);
    }

    /**
     * Reflectively sets a field on the Profile entity with appropriate type coercion.
     */
    private void updateField(Profile profile, String fieldName, Object value) {
        try {
            Field field = Profile.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            Class<?> targetType = field.getType();

            if (value == null) {
                field.set(profile, null);
                return;
            }

            Object coercedValue = coerceValue(value, targetType, fieldName);
            field.set(profile, coercedValue);

        } catch (NoSuchFieldException e) {
            log.error("Failed to find field '{}' on Profile entity", fieldName);
            throw new IllegalArgumentException("Unknown profile field: " + fieldName);
        } catch (IllegalAccessException e) {
            log.error("Security restriction accessing field '{}' on Profile entity", fieldName, e);
            throw new RuntimeException("Internal error updating field: " + fieldName, e);
        }
    }

    /**
     * Performs safe runtime conversion of raw input types to target JPA field types.
     */
    private Object coerceValue(Object value, Class<?> targetType, String fieldName) {
        String stringValue = String.valueOf(value).trim();

        // 1. Target: String
        if (targetType == String.class) {
            return stringValue;
        }

        // 2. Target: Boolean
        if (targetType == Boolean.class) {
            if (value instanceof Boolean) {
                return value;
            }
            return "true".equalsIgnoreCase(stringValue) || "1".equals(stringValue) || "yes".equalsIgnoreCase(stringValue);
        }

        // 3. Target: Integer
        if (targetType == Integer.class) {
            if (value instanceof Number) {
                return ((Number) value).intValue();
            }
            try {
                return Integer.valueOf(stringValue);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(String.format("Invalid integer value '%s' for field '%s'", stringValue, fieldName));
            }
        }

        // 4. Target: BigDecimal
        if (targetType == BigDecimal.class) {
            try {
                return new BigDecimal(stringValue);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(String.format("Invalid decimal value '%s' for field '%s'", stringValue, fieldName));
            }
        }

        // 5. Target: LocalDate
        if (targetType == LocalDate.class) {
            try {
                return LocalDate.parse(stringValue);
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException(String.format("Invalid date format '%s' for field '%s'. Expected YYYY-MM-DD.", stringValue, fieldName));
            }
        }

        // 6. Target: EmploymentStatus (Enum)
        if (targetType == EmploymentStatus.class) {
            try {
                return EmploymentStatus.valueOf(stringValue.toLowerCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(String.format("Invalid employment status '%s'. Must be one of: employed, unemployed, business_owner", stringValue));
            }
        }

        throw new IllegalArgumentException(String.format("Unsupported field datatype '%s' for field '%s'", targetType.getSimpleName(), fieldName));
    }

    private String snakeToCamel(String snake) {
        if (!snake.contains("_")) {
            return snake;
        }
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
