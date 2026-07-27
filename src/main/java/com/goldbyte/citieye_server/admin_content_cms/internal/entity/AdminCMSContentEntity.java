package com.goldbyte.citieye_server.admin_content_cms.internal.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "cms_content", schema = "admins")
public class AdminCMSContentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "content_id", nullable = false)
    private UUID id;

    @Size(max = 255)
    @NotNull
    @Column(name = "title", nullable = false)
    private String title;

    @NotNull
    @Column(name = "cms_content", nullable = false, length = Integer.MAX_VALUE)
    private String cmsContent;

    @Size(max = 100)
    @NotNull
    @Column(name = "author", nullable = false, length = 100)
    private String author;

    @Size(max = 100)
    @NotNull
    @Column(name = "content_category", nullable = false, length = 100)
    private String contentCategory;

    @NotNull
    @ColumnDefault("now()")
    @Column(name = "date_published", nullable = false)
    private OffsetDateTime datePublished;

    @Size(max = 255)
    @Column(name = "picture_url")
    private String pictureUrl;

    @Column(name = "target_cohort_id")
    private Integer targetCohortId;

    @Convert(converter = NigerianStateTargetConverter.class)
    @Column(name = "target_state_residence", columnDefinition = "auth.nigerian_state")
    private String targetStateResidence;

    @Convert(converter = NigerianStateTargetConverter.class)
    @Column(name = "target_state_origin", columnDefinition = "auth.nigerian_state")
    private String targetStateOrigin;
}