package com.goldbyte.citieye_server.cohort_placement.internal.repo;

import com.goldbyte.citieye_server.cohort_placement.internal.model.CitizenMilestone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CitizenMilestoneRepository extends JpaRepository<CitizenMilestone, Long> {
}
