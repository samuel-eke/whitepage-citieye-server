package com.goldbyte.citieye_server.education.internal.repo;

import com.goldbyte.citieye_server.education.internal.model.AcademicStatusTransition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AcademicStatusTransitionRepository extends JpaRepository<AcademicStatusTransition, Long> {

    List<AcademicStatusTransition> findAllByCitizenCodeOrderByTransitionTimestampDesc(String citizenCode);

    List<AcademicStatusTransition> findAllByUpdateIdOrderByTransitionTimestampDesc(Long updateId);

    List<AcademicStatusTransition> findAllByCitizenCodeOrderByTransitionTimestampAsc(String citizenCode);
}
