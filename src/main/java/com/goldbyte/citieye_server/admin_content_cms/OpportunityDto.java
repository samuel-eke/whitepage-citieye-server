package com.goldbyte.citieye_server.admin_content_cms;

import java.util.List;
import java.util.UUID;

public class OpportunityDto {
    private final UUID id;
    private final String title;
    private final String type;
    private final String status;
    private final String brief;
    private final String fullDescription;
    private final List<String> requiredDocuments;
    private final String actionLabel;
    
    // Personalized citizen application state
    private final String applicationStatus;
    private final String assignedOfficerName;
    private final String officerNotes;

    public OpportunityDto(UUID id, String title, String type, String status, String brief, 
                          String fullDescription, List<String> requiredDocuments, String actionLabel, 
                          String applicationStatus, String assignedOfficerName, String officerNotes) {
        this.id = id;
        this.title = title;
        this.type = type;
        this.status = status;
        this.brief = brief;
        this.fullDescription = fullDescription;
        this.requiredDocuments = requiredDocuments;
        this.actionLabel = actionLabel;
        this.applicationStatus = applicationStatus;
        this.assignedOfficerName = assignedOfficerName;
        this.officerNotes = officerNotes;
    }

    public UUID getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getType() {
        return type;
    }

    public String getStatus() {
        return status;
    }

    public String getBrief() {
        return brief;
    }

    public String getFullDescription() {
        return fullDescription;
    }

    public List<String> getRequiredDocuments() {
        return requiredDocuments;
    }

    public String getActionLabel() {
        return actionLabel;
    }

    public String getApplicationStatus() {
        return applicationStatus;
    }

    public String getAssignedOfficerName() {
        return assignedOfficerName;
    }

    public String getOfficerNotes() {
        return officerNotes;
    }
}
