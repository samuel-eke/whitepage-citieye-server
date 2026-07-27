package com.goldbyte.citieye_server.admin_content_cms;

import java.util.List;

public interface CMSPublicService {
    List<PostDto> getTargetedPosts(Integer cohortId, String stateResidence, 
                                   String stateOrigin);
}
