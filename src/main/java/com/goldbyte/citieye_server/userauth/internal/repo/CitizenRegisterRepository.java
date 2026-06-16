package com.goldbyte.citieye_server.userauth.internal.repo;

import com.goldbyte.citieye_server.userauth.internal.entity.CitizenRegister;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CitizenRegisterRepository extends JpaRepository<CitizenRegister, UUID> {
    Optional<CitizenRegister> findByEmail(String email);
    Optional<CitizenRegister> findByPhoneNumber(String phoneNumber);
}