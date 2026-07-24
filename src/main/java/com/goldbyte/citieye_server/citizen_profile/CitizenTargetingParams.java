package com.goldbyte.citieye_server.citizen_profile;

public class CitizenTargetingParams {
    private final Integer cohortId;
    private final String stateResidence;
    private final String stateOrigin;
    private final String desiredSupport;

    public CitizenTargetingParams(Integer cohortId, String stateResidence, 
                                  String stateOrigin, String desiredSupport) {
        this.cohortId = cohortId;
        this.stateResidence = stateResidence;
        this.stateOrigin = stateOrigin;
        this.desiredSupport = desiredSupport;
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

    public String getDesiredSupport() {
        return desiredSupport;
    }
}
