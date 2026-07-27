package com.goldbyte.citieye_server.admin_content_cms;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Event published asynchronously when a new targeted content post is published.
 * Listened to by the notification module to dispatch updates via SSE and Web Push.
 */
public class PostPublishedEvent {
    private final UUID postId;
    private final String title;
    private final String content;
    private final String author;
    private final String contentCategory;
    private final OffsetDateTime datePublished;
    private final String pictureUrl;
    private final Integer targetCohortId;
    private final String targetStateResidence;
    private final String targetStateOrigin;

    public PostPublishedEvent(UUID postId, String title, String content, String author, 
                                String contentCategory, OffsetDateTime datePublished, String pictureUrl,
                                Integer targetCohortId, String targetStateResidence, 
                                String targetStateOrigin) {
        this.postId = postId;
        this.title = title;
        this.content = content;
        this.author = author;
        this.contentCategory = contentCategory;
        this.datePublished = datePublished;
        this.pictureUrl = pictureUrl;
        this.targetCohortId = targetCohortId;
        this.targetStateResidence = targetStateResidence;
        this.targetStateOrigin = targetStateOrigin;
    }

    public UUID getPostId() {
        return postId;
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
}
