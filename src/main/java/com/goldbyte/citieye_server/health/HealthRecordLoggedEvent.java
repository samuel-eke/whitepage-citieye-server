package com.goldbyte.citieye_server.health;

import java.time.OffsetDateTime;

/**
 * Spring Modulith Application Event published when a health record is logged.
 * Placed in the public root package of the health module.
 */
public class HealthRecordLoggedEvent {
    private final Long recordId;
    private final String citizenCode;
    private final String recordType;
    private final String officerCode;
    private final OffsetDateTime loggedAt;

    public HealthRecordLoggedEvent(Long recordId, String citizenCode, String recordType, String officerCode, OffsetDateTime loggedAt) {
        this.recordId = recordId;
        this.citizenCode = citizenCode;
        this.recordType = recordType;
        this.officerCode = officerCode;
        this.loggedAt = loggedAt;
    }

    public Long getRecordId() {
        return recordId;
    }

    public String getCitizenCode() {
        return citizenCode;
    }

    public String getRecordType() {
        return recordType;
    }

    public String getOfficerCode() {
        return officerCode;
    }

    public OffsetDateTime getLoggedAt() {
        return loggedAt;
    }
}
