package com.goldbyte.citieye_server.userauth.internal.service;

import com.goldbyte.citieye_server.userauth.internal.model.RefreshToken;
import com.goldbyte.citieye_server.userauth.internal.repo.RefreshTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(refreshTokenService, "refreshExpirationSeconds", 604800L);
    }

    @Test
    void testCreateRefreshToken_success() {
        Mockito.when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RefreshToken token = refreshTokenService.createRefreshToken("CIT123");

        assertNotNull(token);
        assertEquals("CIT123", token.getCitizenCode());
        assertNotNull(token.getToken());
        assertFalse(token.isRevoked());
        assertTrue(token.getExpiryDate().isAfter(Instant.now()));
    }

    @Test
    void testVerifyExpiration_validToken() {
        RefreshToken token = RefreshToken.builder()
                .expiryDate(Instant.now().plusSeconds(100))
                .build();

        RefreshToken verified = refreshTokenService.verifyExpiration(token);
        assertEquals(token, verified);
    }

    @Test
    void testVerifyExpiration_expiredToken_throwsException() {
        RefreshToken token = RefreshToken.builder()
                .expiryDate(Instant.now().minusSeconds(100))
                .build();

        assertThrows(RuntimeException.class, () -> refreshTokenService.verifyExpiration(token));
        Mockito.verify(refreshTokenRepository).delete(token);
    }

    @Test
    void testRevokeToken_success() {
        RefreshToken token = RefreshToken.builder()
                .token("test_token")
                .revoked(false)
                .build();

        Mockito.when(refreshTokenRepository.findByToken("test_token")).thenReturn(Optional.of(token));

        refreshTokenService.revokeToken("test_token");

        assertTrue(token.isRevoked());
        Mockito.verify(refreshTokenRepository).save(token);
    }
}
