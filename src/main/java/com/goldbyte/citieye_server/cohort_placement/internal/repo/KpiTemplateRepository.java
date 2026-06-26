package com.goldbyte.citieye_server.cohort_placement.internal.repo;

import com.goldbyte.citieye_server.cohort_placement.internal.model.KpiTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KpiTemplateRepository extends JpaRepository<KpiTemplate, Integer> {
}
