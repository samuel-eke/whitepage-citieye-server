package com.goldbyte.citieye_server.notification;

import java.util.ArrayList;
import java.util.List;

/**
 * Synchronous application event used to query matching citizen_code values from other modules
 * based on the targeting parameters of a post.
 */
public class QueryTargetUsersEvent {
    private final Integer cohortId;
    private final String stateResidence;
    private final String stateOrigin;
    private final List<String> targetCitizenCodes = new ArrayList<>();

    public QueryTargetUsersEvent(Integer cohortId, String stateResidence, String stateOrigin) {
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

    public List<String> getTargetCitizenCodes() {
        return targetCitizenCodes;
    }

    public void addCitizenCode(String citizenCode) {
        if (citizenCode != null && !this.targetCitizenCodes.contains(citizenCode)) {
            this.targetCitizenCodes.add(citizenCode);
        }
    }

    public void addCitizenCodes(List<String> citizenCodes) {
        if (citizenCodes != null) {
            for (String code : citizenCodes) {
                addCitizenCode(code);
            }
        }
    }
}
