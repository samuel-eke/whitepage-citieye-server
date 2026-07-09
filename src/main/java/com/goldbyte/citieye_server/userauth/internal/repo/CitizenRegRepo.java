package com.goldbyte.citieye_server.userauth.internal.repo;

import com.goldbyte.citieye_server.userauth.internal.model.CitizenAuthEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CitizenRegRepo extends JpaRepository<CitizenAuthEntity, Long> {
    Optional<CitizenAuthEntity> findFirstByNin(String nin);
    Optional<CitizenAuthEntity> findFirstByEmail(String email);
    Optional<CitizenAuthEntity> findFirstByPhoneNumber(String phoneNumber);
    Optional<CitizenAuthEntity> findFirstByCitizenCode(String citizenCode);
}
