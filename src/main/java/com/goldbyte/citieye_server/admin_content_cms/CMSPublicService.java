package com.goldbyte.citieye_server.admin_content_cms;

import java.util.List;
import java.util.UUID;

public interface CMSPublicService {
    List<PostDto> getTargetedPosts(Integer cohortId, String stateResidence, 
                                   String stateOrigin);

    List<OpportunityDto> getTargetedOpportunities(String citizenCode, Integer cohortId, String stateResidence, String stateOrigin);

    void applyForOpportunity(String citizenCode, UUID opportunityId);

    OpportunityApplicationDto updateApplicationStatus(UUID applicationId, String officerCode, String status, String notes);

    List<OpportunityApplicationDto> getApplicationsForOfficer(String officerCode);
}
