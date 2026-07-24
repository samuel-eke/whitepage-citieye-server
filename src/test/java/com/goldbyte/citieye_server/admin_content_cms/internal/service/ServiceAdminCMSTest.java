package com.goldbyte.citieye_server.admin_content_cms.internal.service;

import com.goldbyte.citieye_server.admin_content_cms.PostDto;
import com.goldbyte.citieye_server.admin_content_cms.internal.entity.AdminCMSContentEntity;
import com.goldbyte.citieye_server.admin_content_cms.internal.repo.AdminCMSContentEntityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServiceAdminCMSTest {

    @Mock
    private AdminCMSContentEntityRepository repository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private ServiceAdminCMS serviceAdminCMS;

    @BeforeEach
    void setUp() {
        serviceAdminCMS = new ServiceAdminCMS(repository, eventPublisher);
    }

    @Test
    void getTargetedPosts_delegatesToRepositoryAndMapsToDto() {
        UUID postId = UUID.randomUUID();
        AdminCMSContentEntity entity = new AdminCMSContentEntity();
        entity.setId(postId);
        entity.setTitle("Community Health Workshop");
        entity.setCmsContent("Health workshop details...");
        entity.setAuthor("Admin-01");
        entity.setContentCategory("HEALTH");
        entity.setDatePublished(OffsetDateTime.now());
        entity.setTargetCohortId(1);
        entity.setTargetStateResidence("Lagos");
        entity.setTargetStateOrigin("Kano");
        entity.setTargetDesiredSupport("HEALTHCARE");

        when(repository.findTargetedPosts(1, "Lagos", "Kano", "HEALTHCARE"))
                .thenReturn(List.of(entity));

        List<PostDto> result = serviceAdminCMS.getTargetedPosts(1, "Lagos", "Kano", "HEALTHCARE");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(postId);
        assertThat(result.get(0).getTitle()).isEqualTo("Community Health Workshop");
        assertThat(result.get(0).getTargetCohortId()).isEqualTo(1);
        assertThat(result.get(0).getTargetStateResidence()).isEqualTo("Lagos");

        verify(repository, times(1)).findTargetedPosts(1, "Lagos", "Kano", "HEALTHCARE");
    }

    @Test
    void getTargetedPosts_handlesNullParameters() {
        when(repository.findTargetedPosts(null, null, null, null))
                .thenReturn(List.of());

        List<PostDto> result = serviceAdminCMS.getTargetedPosts(null, null, null, null);

        assertThat(result).isEmpty();
        verify(repository, times(1)).findTargetedPosts(null, null, null, null);
    }
}
