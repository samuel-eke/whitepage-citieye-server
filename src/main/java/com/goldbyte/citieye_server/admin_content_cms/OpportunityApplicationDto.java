package com.goldbyte.citieye_server.admin_content_cms;

import java.time.OffsetDateTime;
import java.util.UUID;

public class OpportunityApplicationDto {
    private final UUID id;
    private final String citizenCode;
    private final UUID opportunityId;
    private final String opportunityTitle;
    private final String applicationStatus;
    private final String assignedOfficerCode;
    private final String officerNotes;
    private final OffsetDateTime submittedAt;
    private final OffsetDateTime updatedAt;

    public OpportunityApplicationDto(UUID id, String citizenCode, UUID opportunityId, String opportunityTitle, 
                                     String applicationStatus, String assignedOfficerCode, String officerNotes, 
                                     OffsetDateTime submittedAt, OffsetDateTime updatedAt) {
        this.id = id;
        this.citizenCode = citizenCode;
        this.opportunityId = opportunityId;
        this.opportunityTitle = opportunityTitle;
        this.applicationStatus = applicationStatus;
        this.assignedOfficerCode = assignedOfficerCode;
        this.officerNotes = officerNotes;
        this.submittedAt = submittedAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public String getCitizenCode() {
        return citizenCode;
    }

    public UUID getOpportunityId() {
        return opportunityId;
    }

    public String getOpportunityTitle() {
        return opportunityTitle;
    }

    public String getApplicationStatus() {
        return applicationStatus;
    }

    public String getAssignedOfficerCode() {
        return assignedOfficerCode;
    }

    public String getOfficerNotes() {
        return officerNotes;
    }

    public OffsetDateTime getSubmittedAt() {
        return submittedAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
