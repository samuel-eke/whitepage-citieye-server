package com.goldbyte.citieye_server.education.internal.repo;

import com.goldbyte.citieye_server.education.internal.model.OfficerSchoolAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OfficerSchoolAssignmentRepository extends JpaRepository<OfficerSchoolAssignment, Long> {

    List<OfficerSchoolAssignment> findAllByOfficerCodeAndActiveTrue(String officerCode);

    List<OfficerSchoolAssignment> findAllByActiveTrue();

    List<OfficerSchoolAssignment> findAllByOfficerCode(String officerCode);

    boolean existsByOfficerCodeAndSchoolIdAndActiveTrue(String officerCode, Long schoolId);

    boolean existsByOfficerCodeAndLgaIgnoreCaseAndActiveTrue(String officerCode, String lga);

    Optional<OfficerSchoolAssignment> findByOfficerCodeAndSchoolIdAndActiveTrue(String officerCode, Long schoolId);
}
