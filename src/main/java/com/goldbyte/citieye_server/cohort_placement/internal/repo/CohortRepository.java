package com.goldbyte.citieye_server.cohort_placement.internal.repo;

import com.goldbyte.citieye_server.cohort_placement.internal.model.Cohort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CohortRepository extends JpaRepository<Cohort, Integer> {

    @Query("SELECT c FROM Cohort c WHERE :age >= c.minAge AND (c.maxAge IS NULL OR :age <= c.maxAge)")
    List<Cohort> findMatchingCohorts(@Param("age") Integer age);
}
