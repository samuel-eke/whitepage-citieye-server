package com.goldbyte.citieye_server.userauth.internal.controller;

import com.goldbyte.citieye_server.userauth.CitizenRegisteredEvent;
import com.goldbyte.citieye_server.userauth.internal.dto.CitizenLoginRequest;
import com.goldbyte.citieye_server.userauth.internal.dto.CitizenLoginResponse;
import com.goldbyte.citieye_server.userauth.internal.dto.CitizenRegRequest;
import com.goldbyte.citieye_server.userauth.internal.dto.CitizenRegResponse;
import com.goldbyte.citieye_server.userauth.internal.model.CitizenAuthEntity;
import com.goldbyte.citieye_server.userauth.internal.model.enums.GenderType;
import com.goldbyte.citieye_server.userauth.internal.model.enums.NigerianState;
import com.goldbyte.citieye_server.userauth.internal.model.enums.UserRole;
import com.goldbyte.citieye_server.userauth.internal.repo.CitizenRegRepo;
import com.goldbyte.citieye_server.userauth.internal.service.CitizenLoginService;
import com.goldbyte.citieye_server.userauth.internal.service.JwtTokenService;
import com.goldbyte.citieye_server.userauth.internal.service.RefreshTokenService;
import com.goldbyte.citieye_server.userauth.internal.repo.RefreshTokenRepository;
import com.goldbyte.citieye_server.userauth.internal.model.RefreshToken;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

class RegLoginControllerTest {

    @Mock
    private CitizenRegRepo citizenRegRepo;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private EntityManager entityManager;

    @Mock
    private CitizenLoginService citizenLoginService;

    @Mock
    private JwtTokenService jwtTokenService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private RegLoginController regLoginController;

    private Validator validator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testRequestValidation_withValidData_hasNoViolations() {
        CitizenRegRequest request = new CitizenRegRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail("john.doe@example.com");
        request.setPhoneNumber("08012345678");
        request.setPassword("password123");
        request.setNin("12345678901");
        request.setDateOfBirth(LocalDate.of(2000, 1, 1));
        request.setLga("Aba North");

        Set<ConstraintViolation<CitizenRegRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty(), "Valid request should have no validation violations");
    }

    @Test
    void testRequestValidation_withBlankRequiredFields_hasViolations() {
        CitizenRegRequest request = new CitizenRegRequest();
        // leave required fields blank

        Set<ConstraintViolation<CitizenRegRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "Request with blank fields should have validation violations");

        boolean hasFirstNameViolation = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("firstName"));
        boolean hasLastNameViolation = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("lastName"));
        boolean hasDateOfBirthViolation = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("dateOfBirth"));
        boolean hasLgaViolation = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("lga"));

        assertTrue(hasFirstNameViolation, "Should violate first name constraint");
        assertTrue(hasLastNameViolation, "Should violate last name constraint");
        assertTrue(hasDateOfBirthViolation, "Should violate dateOfBirth constraint");
        assertTrue(hasLgaViolation, "Should violate lga constraint");
    }

    @Test
    void testRequestValidation_withInvalidEmailAndNin_hasViolations() {
        CitizenRegRequest request = new CitizenRegRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail("invalid-email");
        request.setPhoneNumber("08012345678");
        request.setPassword("password123");
        request.setNin("123"); // should be exactly 11 characters
        request.setDateOfBirth(LocalDate.of(2000, 1, 1));
        request.setLga("Aba North");

        Set<ConstraintViolation<CitizenRegRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());

        boolean hasEmailViolation = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("email") && v.getMessage().contains("Invalid email format"));
        boolean hasNinViolation = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("nin") && v.getMessage().contains("NIN must be exactly 11 characters"));

        assertTrue(hasEmailViolation, "Should detect invalid email format");
        assertTrue(hasNinViolation, "Should detect invalid NIN length");
    }

    @Test
    void testRegisterCitizen_success() {
        CitizenRegRequest request = new CitizenRegRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail("john.doe@example.com");
        request.setPhoneNumber("08012345678");
        request.setPassword("password123");
        request.setNin("12345678901");
        request.setDateOfBirth(LocalDate.of(1995, 5, 10));
        request.setLga("Aba North");
        request.setGender("male");
        request.setStateOfResidence("Lagos");
        request.setStateOfOrigin("Lagos");

        CitizenAuthEntity savedModel = new CitizenAuthEntity();
        savedModel.setId(1L);
        savedModel.setFirstName("John");
        savedModel.setLastName("Doe");
        savedModel.setEmail("john.doe@example.com");
        savedModel.setPhoneNumber("08012345678");
        savedModel.setPassword("hashed_password");
        savedModel.setNin("12345678901");
        savedModel.setLga("Aba North");
        savedModel.setCitizenCode("CIT123");
        savedModel.setDateOfBirth(LocalDate.of(1995, 5, 10));
        savedModel.setGender(GenderType.male);
        savedModel.setStateOfResidence(NigerianState.Lagos);
        savedModel.setStateOfOrigin(NigerianState.Lagos);
        savedModel.setRole(UserRole.citizen);
        savedModel.setCreatedAt(OffsetDateTime.now());

        Mockito.when(passwordEncoder.encode("password123")).thenReturn("hashed_password");
        Mockito.when(citizenRegRepo.saveAndFlush(any(CitizenAuthEntity.class))).thenReturn(savedModel);
        Mockito.when(jwtTokenService.generateToken(any(), any())).thenReturn("mocked_jwt_token");
        RefreshToken dummyToken = RefreshToken.builder().token("dummy_refresh_token").build();
        Mockito.when(refreshTokenService.createRefreshToken(any())).thenReturn(dummyToken);

        // Populate mutable event cohort name during publication
        Mockito.doAnswer(invocation -> {
            CitizenRegisteredEvent event = invocation.getArgument(0);
            event.setCohortName("Workforce Integration");
            return null;
        }).when(eventPublisher).publishEvent(any(CitizenRegisteredEvent.class));

        ResponseEntity<CitizenRegResponse> response = regLoginController.reg(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Citizen created successfully", response.getBody().getMessage());
        assertEquals(1L, response.getBody().getId());
        assertEquals("john.doe@example.com", response.getBody().getEmail());
        assertEquals("CIT123", response.getBody().getCitizenCode());
        assertEquals("Workforce Integration", response.getBody().getCohortPlacement());
        assertEquals(31, response.getBody().getAge());
        assertEquals("mocked_jwt_token", response.getBody().getData());

        Mockito.verify(passwordEncoder).encode("password123");
        Mockito.verify(citizenRegRepo).saveAndFlush(any(CitizenAuthEntity.class));
        Mockito.verify(entityManager).refresh(savedModel);
        Mockito.verify(eventPublisher).publishEvent(any(CitizenRegisteredEvent.class));
    }

    @Test
    void testLogin_success() {
        CitizenLoginRequest request = new CitizenLoginRequest();
        request.setIdentifier("john.doe@example.com");
        request.setPassword("password123");

        CitizenLoginResponse expectedResponse = CitizenLoginResponse.builder()
                .id(1L)
                .email("john.doe@example.com")
                .role("citizen")
                .citizenCode("CIT123")
                .message("Login successful")
                .build();

        RefreshToken dummyToken = RefreshToken.builder().token("dummy_refresh_token").build();
        Mockito.when(refreshTokenService.createRefreshToken("CIT123")).thenReturn(dummyToken);
        Mockito.when(citizenLoginService.login(any(CitizenLoginRequest.class))).thenReturn(expectedResponse);

        ResponseEntity<?> response = regLoginController.citizenLogin(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof CitizenLoginResponse);
        CitizenLoginResponse body = (CitizenLoginResponse) response.getBody();
        assertEquals("john.doe@example.com", body.getEmail());
        assertEquals("Login successful", body.getMessage());
    }

    @Test
    void testLogin_failure() {
        CitizenLoginRequest request = new CitizenLoginRequest();
        request.setIdentifier("john.doe@example.com");
        request.setPassword("wrongpassword");

        Mockito.when(citizenLoginService.login(any(CitizenLoginRequest.class)))
                .thenThrow(new BadCredentialsException("Invalid identifier or password"));

        ResponseEntity<?> response = regLoginController.citizenLogin(request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof java.util.Map);
        java.util.Map<?, ?> body = (java.util.Map<?, ?>) response.getBody();
        assertEquals("Invalid identifier or password", body.get("message"));

    }

    @Test
    void testRefreshToken_success() {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Cookie cookie = new Cookie("refreshToken", "valid_token");
        Mockito.when(request.getCookies()).thenReturn(new Cookie[]{cookie});

        RefreshToken mockToken = RefreshToken.builder()
                .token("valid_token")
                .citizenCode("CIT123")
                .revoked(false)
                .build();

        Mockito.when(refreshTokenRepository.findByToken("valid_token")).thenReturn(java.util.Optional.of(mockToken));
        Mockito.when(refreshTokenService.verifyExpiration(any())).thenReturn(mockToken);
        Mockito.when(jwtTokenService.generateToken(any(), any())).thenReturn("new_access_token");

        ResponseEntity<?> response = regLoginController.refreshToken(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof java.util.Map);
        java.util.Map<?, ?> body = (java.util.Map<?, ?>) response.getBody();
        assertEquals("new_access_token", body.get("accessToken"));
    }

    @Test
    void testLogout_success() {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Cookie cookie = new Cookie("refreshToken", "valid_token");
        Mockito.when(request.getCookies()).thenReturn(new Cookie[]{cookie});

        ResponseEntity<?> response = regLoginController.logout(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof java.util.Map);
        java.util.Map<?, ?> body = (java.util.Map<?, ?>) response.getBody();
        assertEquals("Logged out successfully", body.get("message"));
        
        Mockito.verify(refreshTokenService).revokeToken("valid_token");
    }

    @Test
    void testRefreshToken_adminSuccess() {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Cookie cookie = new Cookie("refreshToken", "valid_token");
        Mockito.when(request.getCookies()).thenReturn(new Cookie[]{cookie});

        RefreshToken mockToken = RefreshToken.builder()
                .token("valid_token")
                .citizenCode("cit-adm-123456")
                .revoked(false)
                .build();

        Mockito.when(refreshTokenRepository.findByToken("valid_token")).thenReturn(java.util.Optional.of(mockToken));
        Mockito.when(refreshTokenService.verifyExpiration(any())).thenReturn(mockToken);
        Mockito.when(jwtTokenService.generateToken(any(), any())).thenReturn("new_access_token");

        ResponseEntity<?> response = regLoginController.refreshToken(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof java.util.Map);
        java.util.Map<?, ?> body = (java.util.Map<?, ?>) response.getBody();
        assertEquals("new_access_token", body.get("accessToken"));
    }
}

