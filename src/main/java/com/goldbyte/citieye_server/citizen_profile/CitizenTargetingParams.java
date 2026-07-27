package com.goldbyte.citieye_server.citizen_profile;

public class CitizenTargetingParams {
    private final Integer cohortId;
    private final String stateResidence;
    private final String stateOrigin;

    public CitizenTargetingParams(Integer cohortId, String stateResidence, 
                                  String stateOrigin) {
        this.cohortId = cohortId;
        this.stateResidence = stateResidence;
        this.stateOrigin = stateOrigin;
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
}
