package com.goldbyte.citieye_server.userauth.internal.service;

import com.goldbyte.citieye_server.userauth.CitizenRegisteredEvent;
import com.goldbyte.citieye_server.userauth.internal.dto.CitizenLoginRequest;
import com.goldbyte.citieye_server.userauth.internal.dto.CitizenLoginResponse;
import com.goldbyte.citieye_server.userauth.internal.model.CitizenAuthEntity;
import com.goldbyte.citieye_server.userauth.internal.model.enums.GenderType;
import com.goldbyte.citieye_server.userauth.internal.model.enums.NigerianState;
import com.goldbyte.citieye_server.userauth.internal.model.enums.UserRole;
import com.goldbyte.citieye_server.userauth.internal.repo.CitizenRegRepo;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class CitizenLoginServiceIntegrationTest {

    @Autowired
    private CitizenLoginService citizenLoginService;

    @Autowired
    private CitizenRegRepo citizenRegRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private EntityManager entityManager;

    private CitizenAuthEntity citizenUser;
    private CitizenAuthEntity officerUser;

    @BeforeEach
    void setUp() {
        // Create a Citizen
        citizenUser = new CitizenAuthEntity();
        citizenUser.setFirstName("Jane");
        citizenUser.setLastName("Doe");
        citizenUser.setEmail("jane.doe@example.com");
        citizenUser.setPhoneNumber("09012345678");
        citizenUser.setPassword(passwordEncoder.encode("securePass123"));
        citizenUser.setNin("98765432109");
        citizenUser.setDateOfBirth(LocalDate.of(1998, 4, 15));
        citizenUser.setAddress("456 Citizen Lane, Lagos");
        citizenUser.setStateOfResidence(NigerianState.Lagos);
        citizenUser.setStateOfOrigin(NigerianState.Lagos);
        citizenUser.setGender(GenderType.female);
        citizenUser.setRole(UserRole.citizen);
        citizenUser.setCreatedAt(OffsetDateTime.now());

        citizenUser = citizenRegRepo.saveAndFlush(citizenUser);
        entityManager.refresh(citizenUser);

        // Publish event to generate profile/cohort details
        CitizenRegisteredEvent event = new CitizenRegisteredEvent(
                citizenUser.getCitizenCode(),
                citizenUser.getDateOfBirth(),
                "skill_acquisition",
                citizenUser.getGender().name()
        );
        eventPublisher.publishEvent(event);
        entityManager.flush();

        // Create a Field Officer (which does not have profile/cohort)
        officerUser = new CitizenAuthEntity();
        officerUser.setFirstName("Officer");
        officerUser.setLastName("Smith");
        officerUser.setEmail("officer.smith@example.com");
        officerUser.setPhoneNumber("07012345678");
        officerUser.setPassword(passwordEncoder.encode("officerPass123"));
        officerUser.setNin("11223344556");
        officerUser.setDateOfBirth(LocalDate.of(1990, 8, 20));
        officerUser.setAddress("789 Station Road, Lagos");
        officerUser.setStateOfResidence(NigerianState.Lagos);
        officerUser.setStateOfOrigin(NigerianState.Lagos);
        officerUser.setGender(GenderType.male);
        officerUser.setRole(UserRole.field_officer);
        officerUser.setCreatedAt(OffsetDateTime.now());

        officerUser = citizenRegRepo.saveAndFlush(officerUser);
        entityManager.refresh(officerUser);
        entityManager.flush();
    }

    @Test
    void testLoginWithEmail_success() {
        CitizenLoginRequest request = new CitizenLoginRequest();
        request.setIdentifier("jane.doe@example.com");
        request.setPassword("securePass123");

        CitizenLoginResponse response = citizenLoginService.login(request);

        assertNotNull(response);
        assertEquals(citizenUser.getId(), response.getId());
        assertEquals("jane.doe@example.com", response.getEmail());
        assertEquals("citizen", response.getRole());
        assertEquals("Login successful", response.getMessage());
        
        // Assert profile properties fetched
        assertNotNull(response.getAge());
        assertEquals("skill_acquisition", response.getDesiredSupport());
        
        // Assert cohort placement details fetched
        assertNotNull(response.getCohortName());

        // Assert permissions fetched from RBAC module
        assertNotNull(response.getPermissions());
        assertTrue(response.getPermissions().contains("READ_PROFILE"));
        assertTrue(response.getPermissions().contains("TAKE_ASSESSMENT"));
        assertTrue(response.getPermissions().contains("INTERACT_CMS"));
    }

    @Test
    void testLoginWithPhoneNumber_success() {
        CitizenLoginRequest request = new CitizenLoginRequest();
        request.setIdentifier("09012345678");
        request.setPassword("securePass123");

        CitizenLoginResponse response = citizenLoginService.login(request);

        assertNotNull(response);
        assertEquals(citizenUser.getId(), response.getId());
        assertEquals("jane.doe@example.com", response.getEmail());
    }

    @Test
    void testLoginWithNin_success() {
        CitizenLoginRequest request = new CitizenLoginRequest();
        request.setIdentifier("98765432109");
        request.setPassword("securePass123");

        CitizenLoginResponse response = citizenLoginService.login(request);

        assertNotNull(response);
        assertEquals(citizenUser.getId(), response.getId());
        assertEquals("jane.doe@example.com", response.getEmail());
    }

    @Test
    void testLoginWithCitizenCode_success() {
        CitizenLoginRequest request = new CitizenLoginRequest();
        request.setIdentifier(citizenUser.getCitizenCode());
        request.setPassword("securePass123");

        CitizenLoginResponse response = citizenLoginService.login(request);

        assertNotNull(response);
        assertEquals(citizenUser.getId(), response.getId());
        assertEquals("jane.doe@example.com", response.getEmail());
    }

    @Test
    void testLoginFieldOfficer_success() {
        CitizenLoginRequest request = new CitizenLoginRequest();
        request.setIdentifier("officer.smith@example.com");
        request.setPassword("officerPass123");

        CitizenLoginResponse response = citizenLoginService.login(request);

        assertNotNull(response);
        assertEquals(officerUser.getId(), response.getId());
        assertEquals("officer.smith@example.com", response.getEmail());
        assertEquals("field_officer", response.getRole());
        
        // Profile/Cohort info should be null for non-citizen
        assertNull(response.getAge());
        assertNull(response.getCohortName());

        // Assert permissions fetched from RBAC module
        assertNotNull(response.getPermissions());
        assertTrue(response.getPermissions().contains("READ_ASSIGNED_COHORTS"));
        assertTrue(response.getPermissions().contains("SUBMIT_SOCIO_ECONOMIC_REPORT"));
        assertTrue(response.getPermissions().contains("OFFLINE_SYNC"));
    }

    @Test
    void testLogin_invalidPassword_throwsBadCredentialsException() {
        CitizenLoginRequest request = new CitizenLoginRequest();
        request.setIdentifier("jane.doe@example.com");
        request.setPassword("wrongpassword");

        assertThrows(BadCredentialsException.class, () -> citizenLoginService.login(request));
    }

    @Test
    void testLogin_nonExistentUser_throwsBadCredentialsException() {
        CitizenLoginRequest request = new CitizenLoginRequest();
        request.setIdentifier("doesnotexist@example.com");
        request.setPassword("password");

        assertThrows(BadCredentialsException.class, () -> citizenLoginService.login(request));
    }
}
