package com.goldbyte.citieye_server.admin_content_cms.internal.repo;

import com.goldbyte.citieye_server.admin_content_cms.internal.entity.AdminCMSContentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.UUID;

public interface AdminCMSContentEntityRepository extends JpaRepository<AdminCMSContentEntity, UUID> {

        @Query("SELECT c FROM AdminCMSContentEntity c WHERE " +
                        "(c.targetCohortId IS NULL OR c.targetCohortId = :cohortId) AND " +
                        "(c.targetStateResidence IS NULL OR c.targetStateResidence = :stateResidence) AND " +
                        "(c.targetStateOrigin IS NULL OR c.targetStateOrigin = :stateOrigin) " +
                        "ORDER BY c.datePublished DESC")
        List<AdminCMSContentEntity> findTargetedPosts(@Param("cohortId") Integer cohortId,
                        @Param("stateResidence") String stateResidence,
                        @Param("stateOrigin") String stateOrigin);
}