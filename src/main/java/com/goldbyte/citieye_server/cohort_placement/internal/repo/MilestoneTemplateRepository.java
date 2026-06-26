package com.goldbyte.citieye_server.cohort_placement.internal.repo;

import com.goldbyte.citieye_server.cohort_placement.internal.model.MilestoneTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MilestoneTemplateRepository extends JpaRepository<MilestoneTemplate, Integer> {
}
