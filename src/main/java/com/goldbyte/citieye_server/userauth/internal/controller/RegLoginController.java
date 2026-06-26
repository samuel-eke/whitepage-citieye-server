package com.goldbyte.citieye_server.userauth.internal.controller;

import com.goldbyte.citieye_server.userauth.CitizenRegisteredEvent;
import com.goldbyte.citieye_server.userauth.internal.dto.CitizenRegRequest;
import com.goldbyte.citieye_server.userauth.internal.dto.CitizenRegResponse;
import com.goldbyte.citieye_server.userauth.internal.model.CitizenAuthEntity;
import com.goldbyte.citieye_server.userauth.internal.model.enums.GenderType;
import com.goldbyte.citieye_server.userauth.internal.model.enums.NigerianState;
import com.goldbyte.citieye_server.userauth.internal.model.enums.UserRole;
import com.goldbyte.citieye_server.userauth.internal.repo.CitizenRegRepo;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.Period;

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
        citizenAuthEntity.setPassword(citizenHashPassword);
        citizenAuthEntity.setNin(request.getNin());
        citizenAuthEntity.setCreatedAt(OffsetDateTime.now());

        // Map stateOfResidence (String to Enum)
        if (request.getStateOfResidence() != null) {
            NigerianState residenceState = NigerianState.valueOf(request.getStateOfResidence().trim().replace(' ', '_'));
            citizenAuthEntity.setStateOfResidence(residenceState);
        }

        // Map stateOfOrigin (String to Enum)
        if (request.getStateOfOrigin() != null) {
            NigerianState originState = NigerianState.valueOf(request.getStateOfOrigin().trim().replace(' ', '_'));
            citizenAuthEntity.setStateOfOrigin(originState);
        } else if (request.getStateOfResidence() != null) {
            // Fallback to state of residence if state of origin is not provided
            NigerianState residenceState = NigerianState.valueOf(request.getStateOfResidence().trim().replace(' ', '_'));
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
                request.getDesiredSupport(),
                newlyCreatedCitizen.getGender().name()
        );
        eventPublisher.publishEvent(registrationEvent);

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
                .message("Citizen created successfully")
                .build();

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(responseBody);
    }
}
