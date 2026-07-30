package com.goldbyte.citieye_server.citizen_profile;

import java.time.LocalDate;

public class CitizenTargetingParams {
    private final Integer cohortId;
    private final String stateResidence;
    private final String stateOrigin;
    private final LocalDate dateOfBirth;

    public CitizenTargetingParams(Integer cohortId, String stateResidence, 
                                  String stateOrigin, LocalDate dateOfBirth) {
        this.cohortId = cohortId;
        this.stateResidence = stateResidence;
        this.stateOrigin = stateOrigin;
        this.dateOfBirth = dateOfBirth;
    }

    public Integer getCohortId() {
        return cohortId;
    }

    public String getStateResidence() {
        return stateResidence;
    }

    public String getStateOrigin() {
        return stateOrigin;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }
}

