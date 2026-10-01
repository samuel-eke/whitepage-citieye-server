package com.goldbyte.citieye_server.userauth.internal.repo;

import com.goldbyte.citieye_server.userauth.internal.model.AdminUser;
import com.goldbyte.citieye_server.userauth.internal.model.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface AdminUserRepository extends JpaRepository<AdminUser, Long>, JpaSpecificationExecutor<AdminUser> {
    Optional<AdminUser> findFirstByCitizenCode(String citizenCode);
    Optional<AdminUser> findFirstByEmail(String email);
    List<AdminUser> findByRoleIn(Collection<UserRole> roles);
    Optional<AdminUser> findByCitizenCodeAndRoleIn(String citizenCode, Collection<UserRole> roles);
}