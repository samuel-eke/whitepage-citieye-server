package com.goldbyte.citieye_server.userauth.internal.controller;

import com.goldbyte.citieye_server.userauth.CitizenRegisteredEvent;
import com.goldbyte.citieye_server.userauth.FetchUserAuthoritiesEvent;
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
    private JwtTokenService jwtTokenService;

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

        return ResponseEntity
                .status(HttpStatus.CREATED)
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
    @PostMapping("/login")
    public ResponseEntity<?> citizenLogin(@Valid @RequestBody CitizenLoginRequest request) {
        try {
            CitizenLoginResponse response = citizenLoginService.login(request);
            return ResponseEntity.ok(response);
        } catch (BadCredentialsException e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", e.getMessage()));
        }
    }
}

