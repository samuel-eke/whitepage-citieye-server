package com.goldbyte.citieye_server.admin_content_cms.internal.service;

import com.goldbyte.citieye_server.admin_content_cms.CMSPublicService;
import com.goldbyte.citieye_server.admin_content_cms.PostDto;
import com.goldbyte.citieye_server.admin_content_cms.PostPublishedEvent;
import com.goldbyte.citieye_server.admin_content_cms.ValidateTargetingEvent;
import com.goldbyte.citieye_server.admin_content_cms.internal.entity.AdminCMSContentEntity;
import com.goldbyte.citieye_server.admin_content_cms.internal.repo.AdminCMSContentEntityRepository;
import com.goldbyte.citieye_server.audit.AuditActionEvent;
import jakarta.transaction.Transactional;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ServiceAdminCMS implements CMSPublicService {
    private final AdminCMSContentEntityRepository adminCMSContentEntityRepository;
    private final ApplicationEventPublisher eventPublisher;

    ServiceAdminCMS(AdminCMSContentEntityRepository adminCMSContentEntityRepository, 
                    ApplicationEventPublisher eventPublisher) {
        this.adminCMSContentEntityRepository = adminCMSContentEntityRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public AdminCMSContentEntity createAPost(AdminCMSContentEntity cmsContent) {
        // Validate targeting criteria synchronously via application events
        ValidateTargetingEvent valEvent = new ValidateTargetingEvent(
                cmsContent.getTargetCohortId(),
                cmsContent.getTargetStateResidence(),
                cmsContent.getTargetStateOrigin(),
                cmsContent.getTargetDesiredSupport()
        );
        eventPublisher.publishEvent(valEvent);

        if (!valEvent.isValid()) {
            StringBuilder errorMsg = new StringBuilder("Invalid targeting parameters: ");
            if (!valEvent.isCohortValid()) errorMsg.append("Cohort does not exist. ");
            if (!valEvent.isStateResidenceValid()) errorMsg.append("Invalid State of Residence. ");
            if (!valEvent.isStateOriginValid()) errorMsg.append("Invalid State of Origin. ");
            if (!valEvent.isDesiredSupportValid()) errorMsg.append("Invalid Desired Support. ");
            throw new IllegalArgumentException(errorMsg.toString().trim());
        }

        // Set current admin code as author if not specified
        String currentAdminCode = SecurityContextHolder.getContext().getAuthentication().getName();
        if (cmsContent.getAuthor() == null) {
            cmsContent.setAuthor(currentAdminCode);
        }

        // Default datePublished to now if omitted in request payload
        if (cmsContent.getDatePublished() == null) {
            cmsContent.setDatePublished(java.time.OffsetDateTime.now());
        }

        AdminCMSContentEntity savedPost = adminCMSContentEntityRepository.save(cmsContent);

        // Publish governance audit log event
        eventPublisher.publishEvent(new AuditActionEvent(
                "PUBLISH_POST",
                currentAdminCode,
                savedPost.getId().toString(),
                "Published targeted post: " + savedPost.getTitle()
        ));

        // Publish async event for real-time and offline distribution
        eventPublisher.publishEvent(new PostPublishedEvent(
                savedPost.getId(),
                savedPost.getTitle(),
                savedPost.getCmsContent(),
                savedPost.getAuthor(),
                savedPost.getContentCategory(),
                savedPost.getDatePublished(),
                savedPost.getPictureUrl(),
                savedPost.getTargetCohortId(),
                savedPost.getTargetStateResidence(),
                savedPost.getTargetStateOrigin(),
                savedPost.getTargetDesiredSupport()
        ));

        return savedPost;
    }

    public List<AdminCMSContentEntity> viewAllPosts() {
        return adminCMSContentEntityRepository.findAll();
    }

    public List<AdminCMSContentEntity> viewTargetedPosts(Integer cohortId, String stateResidence, 
                                                         String stateOrigin, String desiredSupport) {
        return adminCMSContentEntityRepository.findTargetedPosts(cohortId, stateResidence, stateOrigin, desiredSupport);
    }

    @Override
    public List<PostDto> getTargetedPosts(Integer cohortId, String stateResidence, 
                                         String stateOrigin, String desiredSupport) {
        return viewTargetedPosts(cohortId, stateResidence, stateOrigin, desiredSupport)
                .stream()
                .map(entity -> new PostDto(
                        entity.getId(),
                        entity.getTitle(),
                        entity.getCmsContent(),
                        entity.getAuthor(),
                        entity.getContentCategory(),
                        entity.getDatePublished(),
                        entity.getPictureUrl(),
                        entity.getTargetCohortId(),
                        entity.getTargetStateResidence(),
                        entity.getTargetStateOrigin(),
                        entity.getTargetDesiredSupport()
                ))
                .collect(Collectors.toList());
    }
}
