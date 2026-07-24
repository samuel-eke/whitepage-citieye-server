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
import com.goldbyte.citieye_server.userauth.internal.service.RefreshTokenService;
import com.goldbyte.citieye_server.userauth.internal.model.RefreshToken;
import com.goldbyte.citieye_server.userauth.internal.repo.RefreshTokenRepository;
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

    @PostMapping("/register/citizen")
    @Transactional
    public ResponseEntity<CitizenRegResponse> reg(@Valid @RequestBody CitizenRegRequest request) {
        CitizenAuthEntity citizenAuthEntity = new CitizenAuthEntity();
        citizenAuthEntity.setFirstName(request.getFirstName());
        citizenAuthEntity.setMiddleName(request.getMiddleName());
        citizenAuthEntity.setLastName(request.getLastName());
        citizenAuthEntity.setEmail(request.getEmail());
        citizenAuthEntity.setPhoneNumber(request.getPhoneNumber());
        citizenAuthEntity.setDateOfBirth(request.getDateOfBirth());
        citizenAuthEntity.setAddress(request.getAddress());

        String citizenHashPassword = passwordEncoder.encode(request.getPassword());
//         String citizenHashNin = passwordEncoder.encode(request.getNin());
        citizenAuthEntity.setPassword(citizenHashPassword);
//        citizenAuthEntity.setNin(citizenHashNin);
        citizenAuthEntity.setNin(request.getNin());
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

        // Publish application event to trigger profile creation and cohort placement in
        // the same transaction
        CitizenRegisteredEvent registrationEvent = new CitizenRegisteredEvent(
                newlyCreatedCitizen.getCitizenCode(),
                request.getDateOfBirth(),
                request.getDesiredSupport(),
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

        int age = Period.between(newlyCreatedCitizen.getDateOfBirth(), LocalDate.now()).getYears();

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

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(HttpServletRequest request) {
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
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Refresh Token Cookie is missing"));
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
}

