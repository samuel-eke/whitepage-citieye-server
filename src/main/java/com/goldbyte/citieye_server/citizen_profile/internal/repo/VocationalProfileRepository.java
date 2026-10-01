package com.goldbyte.citieye_server.citizen_profile.internal.repo;

import com.goldbyte.citieye_server.citizen_profile.internal.model.VocationalProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for managing {@link VocationalProfile} records.
 */
@Repository
public interface VocationalProfileRepository extends JpaRepository<VocationalProfile, Long> {

    Optional<VocationalProfile> findByCitizenCode(String citizenCode);

    boolean existsByCitizenCode(String citizenCode);

    void deleteByCitizenCode(String citizenCode);
}
