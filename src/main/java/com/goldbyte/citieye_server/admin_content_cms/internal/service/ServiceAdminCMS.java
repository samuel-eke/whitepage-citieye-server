package com.goldbyte.citieye_server.admin_content_cms.internal.service;

import com.goldbyte.citieye_server.admin_content_cms.CMSPublicService;
import com.goldbyte.citieye_server.admin_content_cms.PostDto;
import com.goldbyte.citieye_server.admin_content_cms.PostPublishedEvent;
import com.goldbyte.citieye_server.admin_content_cms.ValidateTargetingEvent;
import com.goldbyte.citieye_server.admin_content_cms.OpportunityDto;
import com.goldbyte.citieye_server.admin_content_cms.OpportunityApplicationDto;
import com.goldbyte.citieye_server.admin_content_cms.internal.entity.AdminCMSContentEntity;
import com.goldbyte.citieye_server.admin_content_cms.internal.entity.OpportunityEntity;
import com.goldbyte.citieye_server.admin_content_cms.internal.entity.OpportunityApplicationEntity;
import com.goldbyte.citieye_server.admin_content_cms.internal.repo.AdminCMSContentEntityRepository;
import com.goldbyte.citieye_server.admin_content_cms.internal.repo.OpportunityRepository;
import com.goldbyte.citieye_server.admin_content_cms.internal.repo.OpportunityApplicationRepository;
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
    private final OpportunityRepository opportunityRepository;
    private final OpportunityApplicationRepository opportunityApplicationRepository;

    @jakarta.persistence.PersistenceContext
    private jakarta.persistence.EntityManager em;

    ServiceAdminCMS(AdminCMSContentEntityRepository adminCMSContentEntityRepository, 
                    ApplicationEventPublisher eventPublisher,
                    OpportunityRepository opportunityRepository,
                    OpportunityApplicationRepository opportunityApplicationRepository) {
        this.adminCMSContentEntityRepository = adminCMSContentEntityRepository;
        this.eventPublisher = eventPublisher;
        this.opportunityRepository = opportunityRepository;
        this.opportunityApplicationRepository = opportunityApplicationRepository;
    }

    @Transactional
    public AdminCMSContentEntity createAPost(AdminCMSContentEntity cmsContent) {
        // Validate targeting criteria synchronously via application events
        ValidateTargetingEvent valEvent = new ValidateTargetingEvent(
                cmsContent.getTargetCohortId(),
                cmsContent.getTargetStateResidence(),
                cmsContent.getTargetStateOrigin()
        );
        eventPublisher.publishEvent(valEvent);

        if (!valEvent.isValid()) {
            StringBuilder errorMsg = new StringBuilder("Invalid targeting parameters: ");
            if (!valEvent.isCohortValid()) errorMsg.append("Cohort does not exist. ");
            if (!valEvent.isStateResidenceValid()) errorMsg.append("Invalid State of Residence. ");
            if (!valEvent.isStateOriginValid()) errorMsg.append("Invalid State of Origin. ");
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
                savedPost.getTargetStateOrigin()
        ));

        return savedPost;
    }

    public List<AdminCMSContentEntity> viewAllPosts() {
        return adminCMSContentEntityRepository.findAll();
    }

    public List<AdminCMSContentEntity> viewTargetedPosts(Integer cohortId, String stateResidence, 
                                                         String stateOrigin) {
        return adminCMSContentEntityRepository.findTargetedPosts(cohortId, stateResidence, stateOrigin);
    }

    @Override
    public List<PostDto> getTargetedPosts(Integer cohortId, String stateResidence, 
                                         String stateOrigin) {
        return viewTargetedPosts(cohortId, stateResidence, stateOrigin)
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
                        entity.getTargetStateOrigin()
                ))
                .collect(Collectors.toList());
    }

    private String resolveOfficerName(String officerCode) {
        if (officerCode == null) return null;
        @SuppressWarnings("unchecked")
        List<Object[]> rows = em.createNativeQuery(
                "SELECT first_name, last_name FROM auth.admin_user WHERE citizen_code = :code"
        ).setParameter("code", officerCode).getResultList();
        if (rows.isEmpty()) return null;
        Object[] row = rows.get(0);
        return row[0] + " " + row[1];
    }

    @Transactional
    public OpportunityEntity createOpportunity(OpportunityEntity opportunity) {
        ValidateTargetingEvent valEvent = new ValidateTargetingEvent(
                opportunity.getTargetCohortId(),
                opportunity.getTargetStateResidence(),
                opportunity.getTargetStateOrigin()
        );
        eventPublisher.publishEvent(valEvent);

        if (!valEvent.isValid()) {
            StringBuilder errorMsg = new StringBuilder("Invalid targeting parameters: ");
            if (!valEvent.isCohortValid()) errorMsg.append("Cohort does not exist. ");
            if (!valEvent.isStateResidenceValid()) errorMsg.append("Invalid State of Residence. ");
            if (!valEvent.isStateOriginValid()) errorMsg.append("Invalid State of Origin. ");
            throw new IllegalArgumentException(errorMsg.toString().trim());
        }

        return opportunityRepository.save(opportunity);
    }

    public List<OpportunityEntity> viewAllOpportunities() {
        return opportunityRepository.findAll();
    }

    @Override
    public List<OpportunityDto> getTargetedOpportunities(String citizenCode, Integer cohortId, 
                                                         String stateResidence, String stateOrigin) {
        List<OpportunityEntity> opportunities = opportunityRepository.findTargetedOpportunities(cohortId, stateResidence, stateOrigin);

        java.util.Map<java.util.UUID, OpportunityApplicationEntity> appMap = opportunityApplicationRepository.findAllByCitizenCode(citizenCode)
                .stream()
                .collect(Collectors.toMap(app -> app.getOpportunity().getId(), app -> app));

        return opportunities.stream().map(opt -> {
            OpportunityApplicationEntity app = appMap.get(opt.getId());
            String appStatus = app != null ? app.getApplicationStatus() : "NOT_APPLIED";
            String officerCode = app != null ? app.getAssignedOfficerCode() : null;
            String officerNotes = app != null ? app.getOfficerNotes() : null;
            String officerName = resolveOfficerName(officerCode);

            return new OpportunityDto(
                    opt.getId(),
                    opt.getTitle(),
                    opt.getOpportunityType(),
                    opt.getStatus(),
                    opt.getBrief(),
                    opt.getFullDescription(),
                    opt.getRequiredDocuments(),
                    opt.getActionLabel(),
                    appStatus,
                    officerName,
                    officerNotes
            );
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void applyForOpportunity(String citizenCode, java.util.UUID opportunityId) {
        OpportunityEntity opportunity = opportunityRepository.findById(opportunityId)
                .orElseThrow(() -> new IllegalArgumentException("Opportunity not found with ID: " + opportunityId));

        java.util.Optional<OpportunityApplicationEntity> existing = 
                opportunityApplicationRepository.findByCitizenCodeAndOpportunityId(citizenCode, opportunityId);

        if (existing.isPresent()) {
            throw new IllegalArgumentException("You have already applied for this opportunity.");
        }

        OpportunityApplicationEntity app = new OpportunityApplicationEntity();
        app.setCitizenCode(citizenCode);
        app.setOpportunity(opportunity);
        app.setApplicationStatus("SUBMITTED");
        app.setSubmittedAt(java.time.OffsetDateTime.now());
        app.setUpdatedAt(java.time.OffsetDateTime.now());

        opportunityApplicationRepository.save(app);
    }

    @Override
    @Transactional
    public OpportunityApplicationDto updateApplicationStatus(java.util.UUID applicationId, String officerCode, 
                                                            String status, String notes) {
        OpportunityApplicationEntity app = opportunityApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found with ID: " + applicationId));

        app.setApplicationStatus(status.toUpperCase());
        app.setAssignedOfficerCode(officerCode);
        if (notes != null) {
            app.setOfficerNotes(notes);
        }
        app.setUpdatedAt(java.time.OffsetDateTime.now());

        OpportunityApplicationEntity saved = opportunityApplicationRepository.save(app);
        return mapToApplicationDto(saved);
    }

    @Override
    public List<OpportunityApplicationDto> getApplicationsForOfficer(String officerCode) {
        return opportunityApplicationRepository.findAllByAssignedOfficerCode(officerCode)
                .stream()
                .map(this::mapToApplicationDto)
                .collect(Collectors.toList());
    }

    private OpportunityApplicationDto mapToApplicationDto(OpportunityApplicationEntity app) {
        return new OpportunityApplicationDto(
                app.getId(),
                app.getCitizenCode(),
                app.getOpportunity().getId(),
                app.getOpportunity().getTitle(),
                app.getApplicationStatus(),
                app.getAssignedOfficerCode(),
                app.getOfficerNotes(),
                app.getSubmittedAt(),
                app.getUpdatedAt()
        );
    }
}
