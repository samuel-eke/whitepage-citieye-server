package com.goldbyte.citieye_server.education.internal.repo;

import com.goldbyte.citieye_server.education.internal.model.School;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface SchoolRepository extends JpaRepository<School, Long> {

    List<School> findAllByStateIgnoreCase(String state);

    List<School> findAllByStateIgnoreCaseAndLgaIgnoreCase(String state, String lga);

    List<School> findAllByLgaIgnoreCase(String lga);

    List<School> findByIdIn(Collection<Long> ids);

    boolean existsBySchoolNameIgnoreCaseAndLgaIgnoreCase(String schoolName, String lga);
}
