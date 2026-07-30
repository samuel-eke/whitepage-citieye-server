package com.goldbyte.citieye_server.admin_content_cms.internal.repo;

import com.goldbyte.citieye_server.admin_content_cms.internal.entity.OpportunityEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.UUID;

public interface OpportunityRepository extends JpaRepository<OpportunityEntity, UUID> {

    @Query("SELECT o FROM OpportunityEntity o WHERE " +
            "(o.targetCohortId IS NULL OR o.targetCohortId = :cohortId) AND " +
            "(o.targetStateResidence IS NULL OR o.targetStateResidence = :stateResidence) AND " +
            "(o.targetStateOrigin IS NULL OR o.targetStateOrigin = :stateOrigin) " +
            "ORDER BY o.createdAt DESC")
    List<OpportunityEntity> findTargetedOpportunities(@Param("cohortId") Integer cohortId,
                                                     @Param("stateResidence") String stateResidence,
                                                     @Param("stateOrigin") String stateOrigin);
}
