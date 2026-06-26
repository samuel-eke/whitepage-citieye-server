package com.goldbyte.citieye_server.citizen_profile.internal.repo;

import com.goldbyte.citieye_server.citizen_profile.internal.model.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the {@link Profile} entity.
 */
@Repository
public interface ProfileRepository extends JpaRepository<Profile, String> {
}
