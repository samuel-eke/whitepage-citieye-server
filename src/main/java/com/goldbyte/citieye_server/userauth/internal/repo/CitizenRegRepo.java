package com.goldbyte.citieye_server.userauth.internal.repo;

import com.goldbyte.citieye_server.userauth.internal.model.CitizenAuthEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CitizenRegRepo extends JpaRepository<CitizenAuthEntity, Long> {
    List<CitizenAuthEntity> findByNin(String nin);
    List<CitizenAuthEntity> findByEmail(String email);
    List<CitizenAuthEntity> findByPhoneNumber(String phoneNumber);
}