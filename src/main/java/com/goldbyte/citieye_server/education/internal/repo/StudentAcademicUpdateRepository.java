package com.goldbyte.citieye_server.education.internal.repo;

import com.goldbyte.citieye_server.education.internal.model.StudentAcademicUpdate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentAcademicUpdateRepository extends JpaRepository<StudentAcademicUpdate, Long> {

    List<StudentAcademicUpdate> findAllByCitizenCodeOrderBySubmittedAtDesc(String citizenCode);

    List<StudentAcademicUpdate> findAllByCitizenCode(String citizenCode);

    Optional<StudentAcademicUpdate> findByIdAndCitizenCode(Long id, String citizenCode);
}
