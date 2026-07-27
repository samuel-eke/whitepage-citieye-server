package com.goldbyte.citieye_server.userauth.internal.repo;

import com.goldbyte.citieye_server.userauth.internal.model.AdminUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdminUserRepository extends JpaRepository<AdminUser, Long> {
    Optional<AdminUser> findFirstByCitizenCode(String citizenCode);
    Optional<AdminUser> findFirstByEmail(String email);
}