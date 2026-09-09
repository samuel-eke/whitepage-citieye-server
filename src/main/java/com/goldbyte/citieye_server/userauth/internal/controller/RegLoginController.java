package com.goldbyte.citieye_server.userauth.internal.controller;

import com.goldbyte.citieye_server.userauth.CitizenRegisteredEvent;
import com.goldbyte.citieye_server.userauth.FetchUserAuthoritiesEvent;
import com.goldbyte.citieye_server.userauth.internal.dto.*;
import com.goldbyte.citieye_server.userauth.internal.model.CitizenAuthEntity;
import com.goldbyte.citieye_server.userauth.internal.model.enums.GenderType;
import com.goldbyte.citieye_server.userauth.internal.model.enums.NigerianState;
import com.goldbyte.citieye_server.userauth.internal.model.enums.UserRole;
import com.goldbyte.citieye_server.userauth.internal.repo.CitizenRegRepo;
import com.goldbyte.citieye_server.userauth.internal.service.AdminLoginService;
import com.goldbyte.citieye_server.userauth.internal.service.CitizenLoginService;
import com.goldbyte.citieye_server.userauth.internal.service.JwtTokenService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.Period;
import java.util.Map;
import java.util.Optional;
import com.goldbyte.citieye_server.userauth.internal.service.RefreshTokenService;
import com.goldbyte.citieye_server.userauth.internal.model.RefreshToken;
import com.goldbyte.citieye_server.userauth.internal.repo.RefreshTokenRepository;
import com.goldbyte.citieye_server.userauth.internal.service.StateLgaService;
import com.goldbyte.citieye_server.userauth.internal.repo.AdminUserRepository;
import com.goldbyte.citieye_server.userauth.internal.model.AdminUser;
import org.springframework.http.CacheControl;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.concurrent.TimeUnit;
import java.util.List;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.context.SecurityContextHolder;

@RestController
@RequestMapping("/api/auth")
public class RegLoginController {

    @Autowired
    private CitizenRegRepo citizenRegRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private CitizenLoginService citizenLoginService;

    @Autowired
    private AdminLoginService adminLoginService;

    @Autowired
    private JwtTokenService jwtTokenService;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private StateLgaService stateLgaService;

    @Autowired
    private AdminUserRepository adminUserRepository;

    @GetMapping("/states-lgas")
    public ResponseEntity<Map<String, List<String>>> getStatesAndLgas() {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(365, TimeUnit.DAYS).cachePublic())
                .body(stateLgaService.getStatesAndLgas());
    }

    @PostMapping("/register/citizen")
    @Transactional
    public ResponseEntity<CitizenRegResponse> reg(@Valid @RequestBody CitizenRegRequest request) {
        int age = Period.between(request.getDateOfBirth(), LocalDate.now()).getYears();
        String nin = request.getNin();
        if (nin != null && nin.isBlank()) {
            nin = null;
        }

        if (nin != null) {
            Optional<CitizenAuthEntity> existing = citizenRegRepo.findFirstByNin(nin.trim());
            if (existing.isPresent()) {
                return ResponseEntity.badRequest().body(
                    CitizenRegResponse.builder().message("NIN already registered").build()
                );
            }
        }

        CitizenAuthEntity citizenAuthEntity = new CitizenAuthEntity();
        citizenAuthEntity.setFirstName(request.getFirstName());
        citizenAuthEntity.setMiddleName(request.getMiddleName());
        citizenAuthEntity.setLastName(request.getLastName());
        citizenAuthEntity.setEmail(request.getEmail());
        citizenAuthEntity.setPhoneNumber(request.getPhoneNumber());
        citizenAuthEntity.setDateOfBirth(request.getDateOfBirth());
        citizenAuthEntity.setAddress(request.getAddress());
        citizenAuthEntity.setLga(request.getLga());
        citizenAuthEntity.setResidenceLga(request.getResidenceLga());

        String parentNin = request.getParentNin();
        if (parentNin != null && parentNin.isBlank()) {
            parentNin = null;
        }
        citizenAuthEntity.setParentNin(parentNin);

        String citizenHashPassword = passwordEncoder.encode(request.getPassword());
        citizenAuthEntity.setPassword(citizenHashPassword);
        citizenAuthEntity.setNin(nin);
        citizenAuthEntity.setCreatedAt(OffsetDateTime.now());

        // Map stateOfResidence (String to Enum)
        if (request.getStateOfResidence() != null) {
            NigerianState residenceState = NigerianState
                    .valueOf(request.getStateOfResidence().trim().replace(' ', '_'));
            citizenAuthEntity.setStateOfResidence(residenceState);
        }

        // Map stateOfOrigin (String to Enum)
        if (request.getStateOfOrigin() != null) {
            NigerianState originState = NigerianState.valueOf(request.getStateOfOrigin().trim().replace(' ', '_'));
            citizenAuthEntity.setStateOfOrigin(originState);
        } else if (request.getStateOfResidence() != null) {
            // Fallback to state of residence if state of origin is not provided
            NigerianState residenceState = NigerianState
                    .valueOf(request.getStateOfResidence().trim().replace(' ', '_'));
            citizenAuthEntity.setStateOfOrigin(residenceState);
        }

        // Map gender (String to Enum)
        if (request.getGender() != null) {
            GenderType genderEnum = GenderType.valueOf(request.getGender().trim().toLowerCase());
            citizenAuthEntity.setGender(genderEnum);
        }

        // Assign default role 'citizen'
        citizenAuthEntity.setRole(UserRole.citizen);

        CitizenAuthEntity newlyCreatedCitizen = citizenRegRepo.saveAndFlush(citizenAuthEntity);

        // Refresh the entity to fetch the trigger-generated citizenCode
        entityManager.refresh(newlyCreatedCitizen);

        // Publish application event to trigger profile creation and cohort placement in the same transaction
        CitizenRegisteredEvent registrationEvent = new CitizenRegisteredEvent(
                newlyCreatedCitizen.getCitizenCode(),
                request.getDateOfBirth(),
                newlyCreatedCitizen.getGender().name());
        eventPublisher.publishEvent(registrationEvent);

        // Fetch authorities from RBAC module to populate in the JWT token
        FetchUserAuthoritiesEvent authorityEvent = new FetchUserAuthoritiesEvent(newlyCreatedCitizen.getRole().name(), newlyCreatedCitizen.getCitizenCode());
        eventPublisher.publishEvent(authorityEvent);

        java.util.List<String> tokenAuthorities = new java.util.ArrayList<>();
        if (newlyCreatedCitizen.getRole() != null) {
            tokenAuthorities.add(newlyCreatedCitizen.getRole().name());
        }
        if (authorityEvent.getAuthorities() != null) {
            tokenAuthorities.addAll(authorityEvent.getAuthorities());
        }
        String token = jwtTokenService.generateToken(newlyCreatedCitizen.getCitizenCode(), tokenAuthorities);

        CitizenRegResponse responseBody = CitizenRegResponse.builder()
                .id(newlyCreatedCitizen.getId())
                .email(newlyCreatedCitizen.getEmail())
                .phoneNumber(newlyCreatedCitizen.getPhoneNumber())
                .nin(newlyCreatedCitizen.getNin())
                .firstName(newlyCreatedCitizen.getFirstName())
                .middleName(newlyCreatedCitizen.getMiddleName())
                .lastName(newlyCreatedCitizen.getLastName())
                .gender(newlyCreatedCitizen.getGender().name())
                .address(newlyCreatedCitizen.getAddress())
                .stateOfResidence(newlyCreatedCitizen.getStateOfResidence().name())
                .lga(newlyCreatedCitizen.getLga())
                .residenceLga(newlyCreatedCitizen.getResidenceLga())
                .citizenCode(newlyCreatedCitizen.getCitizenCode())
                .userRole(newlyCreatedCitizen.getRole().name())
                .cohortPlacement(registrationEvent.getCohortName())
                .age(age)
                .data(token)
                .message("Citizen created successfully")
                .build();

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(newlyCreatedCitizen.getCitizenCode());
        HttpHeaders cookieHeaders = createCookieHeaders(refreshToken.getToken(), 604800);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .headers(cookieHeaders)
                .body(responseBody);
    }

    /**
     * Authenticates a citizen using a flexible identifier (email, NIN, phone number, or citizen code)
     * and returns a comprehensive response containing auth data, profile information,
     * cohort placement details.
     *
     * @param request the login request containing the identifier and password
     * @return ResponseEntity with full citizen profile data
     */
    @PostMapping("/citizen/login")
    public ResponseEntity<?> citizenLogin(@Valid @RequestBody CitizenLoginRequest request) {
        try {
            CitizenLoginResponse response = citizenLoginService.login(request);
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(response.getCitizenCode());
            HttpHeaders cookieHeaders = createCookieHeaders(refreshToken.getToken(), 604800);
            return ResponseEntity.ok()
                    .headers(cookieHeaders)
                    .body(response);
        } catch (BadCredentialsException e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * Authenticates an admin user (agency_admin or super_admin) using their
     * CitiEye citizen code and password.
     *
     * @param loginReq the login request containing the citizen code and password
     * @return ResponseEntity with admin profile data and a signed JWT token
     */
    @PostMapping("/admin/login")
    public ResponseEntity<?> adminLogin(@Valid @RequestBody AdminUserLoginReq loginReq) {
        try {
            AdminUserLoginResp response = adminLoginService.login(loginReq);
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(response.getCitizenCode());
            HttpHeaders cookieHeaders = createCookieHeaders(refreshToken.getToken(), 604800);
            return ResponseEntity.ok()
                    .headers(cookieHeaders)
                    .body(response);
        } catch (BadCredentialsException e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    public ResponseEntity<?> refreshToken(HttpServletRequest request) {
        return refreshToken(request, null);
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(
            HttpServletRequest request,
            @RequestBody(required = false) Map<String, String> body) {
        String refreshTokenStr = null;
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("refreshToken".equals(cookie.getName())) {
                    refreshTokenStr = cookie.getValue();
                    break;
                }
            }
        }

        if (refreshTokenStr == null || refreshTokenStr.isEmpty()) {
            if (body != null && body.containsKey("refreshToken")) {
                refreshTokenStr = body.get("refreshToken");
            } else if (request.getHeader("X-Refresh-Token") != null) {
                refreshTokenStr = request.getHeader("X-Refresh-Token");
            }
        }

        if (refreshTokenStr == null || refreshTokenStr.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Refresh Token Cookie or Body is missing"));
        }

        final String tokenVal = refreshTokenStr;
        return refreshTokenRepository.findByToken(tokenVal)
                .map(refreshTokenService::verifyExpiration)
                .filter(token -> !token.isRevoked())
                .map(token -> {
                    String citizenCode = token.getCitizenCode();
                    String role = "citizen";
                    if (citizenCode.startsWith("cit-fo-")) {
                        role = "field_officer";
                    } else if (citizenCode.startsWith("cit-adm-")) {
                        role = "agency_admin";
                    } else if (citizenCode.startsWith("cit-super-")) {
                        role = "super_admin";
                    }

                    FetchUserAuthoritiesEvent authorityEvent = new FetchUserAuthoritiesEvent(role, citizenCode);
                    eventPublisher.publishEvent(authorityEvent);

                    java.util.List<String> tokenAuthorities = new java.util.ArrayList<>();
                    if (authorityEvent.getAuthorities() != null) {
                        tokenAuthorities.addAll(authorityEvent.getAuthorities());
                    }

                    String newAccessToken = jwtTokenService.generateToken(token.getCitizenCode(), tokenAuthorities);

                    return ResponseEntity.ok()
                            .body(Map.of("accessToken", newAccessToken));
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Invalid or revoked Refresh Token")));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("refreshToken".equals(cookie.getName())) {
                    refreshTokenService.revokeToken(cookie.getValue());
                    break;
                }
            }
        }

        SecurityContextHolder.clearContext();

        HttpHeaders headers = createCookieHeaders("", 0);

        return ResponseEntity.ok()
                .headers(headers)
                .body(Map.of("message", "Logged out successfully"));
    }

    private HttpHeaders createCookieHeaders(String refreshTokenValue, long maxAgeSeconds) {
        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshTokenValue)
                .httpOnly(true)
                .secure(true)
                .path("/api/auth")
                .maxAge(maxAgeSeconds)
                .sameSite("Strict")
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, cookie.toString());
        return headers;
    }

    @PostMapping("/register/officer")
    @Transactional
    public ResponseEntity<OfficerRegResponse> registerOfficer(@Valid @RequestBody OfficerRegRequest request) {
        // Enforce state conversion
        NigerianState residenceState;
        NigerianState originState;
        try {
            residenceState = NigerianState.valueOf(request.getStateOfResidence().trim().replace(' ', '_'));
            originState = NigerianState.valueOf(request.getStateOfOrigin().trim().replace(' ', '_'));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid state of origin or residence");
        }

        // Enforce gender conversion
        GenderType genderEnum;
        try {
            genderEnum = GenderType.valueOf(request.getGender().trim().toLowerCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid gender. Must be male or female");
        }

        // Validate specialty and determine mapped role
        String specialtyStr = request.getSpecialty().trim().toUpperCase();
        UserRole mappedRole;
        if ("MEDICAL".equals(specialtyStr) || "HEALTH".equals(specialtyStr)) {
            mappedRole = UserRole.field_officer_health;
        } else if ("EDUCATION".equals(specialtyStr)) {
            mappedRole = UserRole.field_officer_education;
        } else {
            mappedRole = UserRole.field_officer; // Fallback default specialty
        }

        // Create AdminUser
        AdminUser admin = new AdminUser();
        admin.setFirstName(request.getFirstName().trim());
        admin.setMiddleName(request.getMiddleName() != null ? request.getMiddleName().trim() : null);
        admin.setLastName(request.getLastName().trim());
        admin.setEmail(request.getEmail().trim().toLowerCase());
        admin.setPhoneNumber(request.getPhoneNumber().trim());
        admin.setNin(request.getNin().trim());
        admin.setAddress(request.getAddress().trim());
        admin.setLga(request.getLga().trim());
        admin.setResidenceLga(request.getResidenceLga().trim());
        admin.setDateOfBirth(request.getDateOfBirth());
        admin.setSpecialty(specialtyStr);
        admin.setCreatedAt(OffsetDateTime.now());
        admin.setUpdatedAt(OffsetDateTime.now());
        admin.setStateOfResidence(residenceState);
        admin.setStateOfOrigin(originState);
        admin.setGender(genderEnum);
        admin.setRole(mappedRole);

        // Hashing password
        admin.setPassword(passwordEncoder.encode(request.getPassword()));

        // Save
        AdminUser newlyCreatedOfficer = adminUserRepository.saveAndFlush(admin);

        // Refresh entity to get the trigger-generated citizenCode
        entityManager.refresh(newlyCreatedOfficer);

        // Fetch authorities from RBAC module for immediate login session token
        FetchUserAuthoritiesEvent authorityEvent = new FetchUserAuthoritiesEvent(
                newlyCreatedOfficer.getRole().name(),
                newlyCreatedOfficer.getCitizenCode()
        );
        eventPublisher.publishEvent(authorityEvent);

        java.util.List<String> tokenAuthorities = new java.util.ArrayList<>();
        tokenAuthorities.add(newlyCreatedOfficer.getRole().name());
        if (authorityEvent.getAuthorities() != null) {
            tokenAuthorities.addAll(authorityEvent.getAuthorities());
        }
        String token = jwtTokenService.generateToken(newlyCreatedOfficer.getCitizenCode(), tokenAuthorities);

        OfficerRegResponse responseBody = OfficerRegResponse.builder()
                .id(newlyCreatedOfficer.getId())
                .email(newlyCreatedOfficer.getEmail())
                .phoneNumber(newlyCreatedOfficer.getPhoneNumber())
                .nin(newlyCreatedOfficer.getNin())
                .firstName(newlyCreatedOfficer.getFirstName())
                .middleName(newlyCreatedOfficer.getMiddleName())
                .lastName(newlyCreatedOfficer.getLastName())
                .gender(newlyCreatedOfficer.getGender().name())
                .address(newlyCreatedOfficer.getAddress())
                .stateOfResidence(newlyCreatedOfficer.getStateOfResidence().name())
                .stateOfOrigin(newlyCreatedOfficer.getStateOfOrigin().name())
                .lga(newlyCreatedOfficer.getLga())
                .residenceLga(newlyCreatedOfficer.getResidenceLga())
                .citizenCode(newlyCreatedOfficer.getCitizenCode())
                .userRole(newlyCreatedOfficer.getRole().name())
                .specialty(newlyCreatedOfficer.getSpecialty())
                .token(token)
                .message("Field Officer registered successfully")
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(responseBody);
    }
}

