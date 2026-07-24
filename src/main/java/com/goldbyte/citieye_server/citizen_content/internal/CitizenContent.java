package com.goldbyte.citieye_server.citizen_content.internal;

import com.goldbyte.citieye_server.admin_content_cms.CMSPublicService;
import com.goldbyte.citieye_server.admin_content_cms.PostDto;
import com.goldbyte.citieye_server.citizen_profile.CitizenProfilePublicService;
import com.goldbyte.citieye_server.citizen_profile.CitizenTargetingParams;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/citizen")
class CitizenContent {

    private final CitizenProfilePublicService citizenProfilePublicService;
    private final CMSPublicService cmsPublicService;

    public CitizenContent(CitizenProfilePublicService citizenProfilePublicService, 
                          CMSPublicService cmsPublicService) {
        this.citizenProfilePublicService = citizenProfilePublicService;
        this.cmsPublicService = cmsPublicService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboardFeed() {
        String citizenCode = SecurityContextHolder.getContext().getAuthentication().getName();
        if (citizenCode == null || citizenCode.equals("anonymousUser")) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized access."));
        }

        // 1. Get citizen demographic targeting parameters
        CitizenTargetingParams params = citizenProfilePublicService.getTargetingParams(citizenCode);

        // 2. Fetch posts from CMS that match targeting criteria
        List<PostDto> feed = cmsPublicService.getTargetedPosts(
                params.getCohortId(),
                params.getStateResidence(),
                params.getStateOrigin(),
                params.getDesiredSupport()
        );

        return ResponseEntity.ok(feed);
    }
}
