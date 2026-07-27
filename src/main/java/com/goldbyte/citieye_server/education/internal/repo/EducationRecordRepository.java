package com.goldbyte.citieye_server.education.internal.repo;

import com.goldbyte.citieye_server.education.internal.model.EducationRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EducationRecordRepository extends JpaRepository<EducationRecord, Long> {
    List<EducationRecord> findAllByCitizenCode(String citizenCode);
    List<EducationRecord> findAllByOfficerCode(String officerCode);
}
