package com.goldbyte.citieye_server.citizen_profile.internal.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VocationalProfileRequest {

    private String tradeSpecialization;
    
    @Setter(AccessLevel.NONE)
    private Object acquiredSkills;
    
    private String proficiencyLevel;
    private String careerTrajectories;
    private String plannedApprenticeships;
    private String vocationalDevelopmentGoals;
    private String certifications;

    @JsonSetter
    public void setAcquiredSkills(Object acquiredSkills) {
        this.acquiredSkills = acquiredSkills;
    }

    @JsonIgnore
    public void setAcquiredSkills(String acquiredSkills) {
        this.acquiredSkills = acquiredSkills;
    }

    @JsonIgnore
    public void setAcquiredSkills(List<String> acquiredSkills) {
        this.acquiredSkills = acquiredSkills;
    }

    public String getAcquiredSkillsAsString() {
        if (acquiredSkills == null) return null;
        if (acquiredSkills instanceof List<?> list) {
            return String.join(", ", list.stream().map(Object::toString).toList());
        }
        return acquiredSkills.toString();
    }
}
