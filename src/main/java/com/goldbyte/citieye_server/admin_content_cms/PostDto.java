package com.goldbyte.citieye_server.admin_content_cms;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Data Transfer Object for admin CMS posts served to the citizen dashboard.
 * <p>
 * Includes targeting metadata so the frontend can render badges showing
 * which criterion matched (e.g. "Cohort", "Lagos (Residence)", "Business Capital").
 * A post with all target fields null is a broadcast to all citizens.
 */
public class PostDto {
    private final UUID id;
    private final String title;
    private final String content;
    private final String author;
    private final String contentCategory;
    private final OffsetDateTime datePublished;
    private final String pictureUrl;

    // Targeting metadata — nullable (post may target by a single criterion, or none for broadcast)
    private final Integer targetCohortId;
    private final String targetStateResidence;
    private final String targetStateOrigin;
    private final String targetDesiredSupport;

    public PostDto(UUID id, String title, String content, String author,
                   String contentCategory, OffsetDateTime datePublished, String pictureUrl,
                   Integer targetCohortId, String targetStateResidence,
                   String targetStateOrigin, String targetDesiredSupport) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.author = author;
        this.contentCategory = contentCategory;
        this.datePublished = datePublished;
        this.pictureUrl = pictureUrl;
        this.targetCohortId = targetCohortId;
        this.targetStateResidence = targetStateResidence;
        this.targetStateOrigin = targetStateOrigin;
        this.targetDesiredSupport = targetDesiredSupport;
    }

    public UUID getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getAuthor() {
        return author;
    }

    public String getContentCategory() {
        return contentCategory;
    }

    public OffsetDateTime getDatePublished() {
        return datePublished;
    }

    public String getPictureUrl() {
        return pictureUrl;
    }

    public Integer getTargetCohortId() {
        return targetCohortId;
    }

    public String getTargetStateResidence() {
        return targetStateResidence;
    }

    public String getTargetStateOrigin() {
        return targetStateOrigin;
    }

    public String getTargetDesiredSupport() {
        return targetDesiredSupport;
    }
}
