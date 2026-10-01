package com.goldbyte.citieye_server.citizen_profile.internal.dto;

import lombok.*;

import java.time.OffsetDateTime;

/**
 * Data transfer object returned when retrieving vocational profile details.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VocationalProfileResponse {

    private Long id;
    private String citizenCode;
    private String tradeSpecialization;
    private Object acquiredSkills;
    private String proficiencyLevel;
    private String careerTrajectories;
    private String plannedApprenticeships;
    private String vocationalDevelopmentGoals;
    private String certifications;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
