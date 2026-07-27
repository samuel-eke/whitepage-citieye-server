package com.goldbyte.citieye_server.health.internal.repo;

import com.goldbyte.citieye_server.health.internal.model.HealthRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HealthRecordRepository extends JpaRepository<HealthRecord, Long> {
    List<HealthRecord> findAllByCitizenCode(String citizenCode);
    List<HealthRecord> findAllByOfficerCode(String officerCode);
}
