package com.goldbyte.citieye_server.education.internal.repo;

import com.goldbyte.citieye_server.education.internal.model.StudentRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface StudentRecordRepository extends JpaRepository<StudentRecord, Long> {

    List<StudentRecord> findAllBySchoolId(Long schoolId);

    List<StudentRecord> findAllBySchoolIdIn(Collection<Long> schoolIds);

    List<StudentRecord> findAllByLgaIgnoreCase(String lga);

    List<StudentRecord> findAllBySchoolIdAndLgaIgnoreCase(Long schoolId, String lga);

    List<StudentRecord> findAllByCitizenCode(String citizenCode);
}
