package com.goldbyte.citieye_server.admin_content_cms;

/**
 * Spring Modulith application event published synchronously to validate targeting parameters
 * across different modules. Target modules listen to this event and flag any invalid options.
 */
public class ValidateTargetingEvent {
    private final Integer cohortId;
    private final String stateResidence;
    private final String stateOrigin;
    private final String desiredSupport;
    
    private boolean cohortValid = true;
    private boolean stateResidenceValid = true;
    private boolean stateOriginValid = true;
    private boolean desiredSupportValid = true;

    public ValidateTargetingEvent(Integer cohortId, String stateResidence, String stateOrigin, String desiredSupport) {
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

    public boolean isCohortValid() {
        return cohortValid;
    }

    public void setCohortValid(boolean cohortValid) {
        this.cohortValid = cohortValid;
    }

    public boolean isStateResidenceValid() {
        return stateResidenceValid;
    }

    public void setStateResidenceValid(boolean stateResidenceValid) {
        this.stateResidenceValid = stateResidenceValid;
    }

    public boolean isStateOriginValid() {
        return stateOriginValid;
    }

    public void setStateOriginValid(boolean stateOriginValid) {
        this.stateOriginValid = stateOriginValid;
    }

    public boolean isDesiredSupportValid() {
        return desiredSupportValid;
    }

    public void setDesiredSupportValid(boolean desiredSupportValid) {
        this.desiredSupportValid = desiredSupportValid;
    }

    public boolean isValid() {
        return cohortValid && stateResidenceValid && stateOriginValid && desiredSupportValid;
    }
}
