package com.goldbyte.citieye_server.citizen_profile.internal.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * JPA entity representing a citizen's vocational profile, trade specialization,
 * acquired skills, apprenticeships, and vocational development plans.
 * Stored under the 'citizen_profile' schema.
 */
@Entity
@Table(name = "vocational_profiles", schema = "citizen_profile")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VocationalProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Size(max = 50)
    @Column(name = "citizen_code", nullable = false, unique = true, length = 50)
    private String citizenCode;

    @Size(max = 255)
    @Column(name = "trade_specialization", length = 255)
    private String tradeSpecialization;

    @Column(name = "acquired_skills", columnDefinition = "TEXT")
    private String acquiredSkills;

    @Size(max = 50)
    @Column(name = "proficiency_level", length = 50)
    private String proficiencyLevel;

    @Column(name = "career_trajectories", columnDefinition = "TEXT")
    private String careerTrajectories;

    @Column(name = "planned_apprenticeships", columnDefinition = "TEXT")
    private String plannedApprenticeships;

    @Column(name = "vocational_development_goals", columnDefinition = "TEXT")
    private String vocationalDevelopmentGoals;

    @Column(name = "certifications", columnDefinition = "TEXT")
    private String certifications;

    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    public void setAcquiredSkills(String acquiredSkills) {
        this.acquiredSkills = acquiredSkills;
    }

    public void setAcquiredSkills(List<String> skills) {
        if (skills == null || skills.isEmpty()) {
            this.acquiredSkills = null;
        } else {
            this.acquiredSkills = String.join(", ", skills);
        }
    }

    public List<String> getAcquiredSkillsList() {
        if (acquiredSkills == null || acquiredSkills.isBlank()) {
            return List.of();
        }
        return Arrays.stream(acquiredSkills.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = OffsetDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}
