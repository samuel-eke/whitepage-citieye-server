package com.goldbyte.citieye_server.admin_content_cms.internal.repo;

import com.goldbyte.citieye_server.admin_content_cms.internal.entity.OpportunityApplicationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OpportunityApplicationRepository extends JpaRepository<OpportunityApplicationEntity, UUID> {

    Optional<OpportunityApplicationEntity> findByCitizenCodeAndOpportunityId(String citizenCode, UUID opportunityId);

    List<OpportunityApplicationEntity> findAllByCitizenCode(String citizenCode);

    List<OpportunityApplicationEntity> findAllByAssignedOfficerCode(String assignedOfficerCode);
}
