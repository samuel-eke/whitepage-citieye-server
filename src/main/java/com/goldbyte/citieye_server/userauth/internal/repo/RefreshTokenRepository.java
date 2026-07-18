package com.goldbyte.citieye_server.userauth.internal.repo;

import com.goldbyte.citieye_server.userauth.internal.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    void deleteByCitizenCode(String citizenCode);
}
