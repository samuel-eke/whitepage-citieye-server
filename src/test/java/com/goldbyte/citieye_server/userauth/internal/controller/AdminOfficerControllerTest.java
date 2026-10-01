package com.goldbyte.citieye_server.userauth.internal.controller;

import com.goldbyte.citieye_server.userauth.internal.model.AdminUser;
import com.goldbyte.citieye_server.userauth.internal.model.enums.GenderType;
import com.goldbyte.citieye_server.userauth.internal.model.enums.NigerianState;
import com.goldbyte.citieye_server.userauth.internal.model.enums.UserRole;
import com.goldbyte.citieye_server.userauth.internal.repo.AdminUserRepository;
import com.goldbyte.citieye_server.userauth.internal.service.JwtTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class AdminOfficerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AdminUserRepository adminUserRepository;

    @Autowired
    private JwtTokenService jwtTokenService;

    @jakarta.persistence.PersistenceContext
    private jakarta.persistence.EntityManager entityManager;

    private AdminUser eduOfficer;
    private AdminUser healthOfficer;
    private String superAdminToken;
    private String agencyAdminToken;
    private String citizenToken;

    @BeforeEach
    void setUp() {
        superAdminToken = jwtTokenService.generateToken("cit-super-001", List.of("super_admin"));
        agencyAdminToken = jwtTokenService.generateToken("cit-adm-001", List.of("agency_admin"));
        citizenToken = jwtTokenService.generateToken("cit-usr-001", List.of("citizen"));

        // Seed Education Field Officer
        eduOfficer = new AdminUser();
        eduOfficer.setFirstName("Fatima");
        eduOfficer.setLastName("Yusuf");
        eduOfficer.setEmail("fatima.yusuf@citieye.gov.ng");
        eduOfficer.setPhoneNumber("08011112233");
        eduOfficer.setPassword("encodedPassword123");
        eduOfficer.setNin("11122233344");
        eduOfficer.setAddress("14 Broad Street, Lagos");
        eduOfficer.setCreatedAt(OffsetDateTime.now());
        eduOfficer.setUpdatedAt(OffsetDateTime.now());
        eduOfficer.setStateOfOrigin(NigerianState.Lagos);
        eduOfficer.setStateOfResidence(NigerianState.Lagos);
        eduOfficer.setGender(GenderType.female);
        eduOfficer.setRole(UserRole.field_officer_education);
        eduOfficer.setLga("Ikeja");
        eduOfficer.setResidenceLga("Ikeja");
        eduOfficer.setDateOfBirth(LocalDate.of(1988, 4, 12));
        eduOfficer.setSpecialty("EDUCATION");
        eduOfficer = adminUserRepository.saveAndFlush(eduOfficer);
        entityManager.refresh(eduOfficer);

        // Seed Health Field Officer
        healthOfficer = new AdminUser();
        healthOfficer.setFirstName("Emeka");
        healthOfficer.setLastName("Okonkwo");
        healthOfficer.setEmail("emeka.okonkwo@citieye.gov.ng");
        healthOfficer.setPhoneNumber("08044445566");
        healthOfficer.setPassword("encodedPassword123");
        healthOfficer.setNin("55566677788");
        healthOfficer.setAddress("25 Ring Road, Benin City");
        healthOfficer.setCreatedAt(OffsetDateTime.now());
        healthOfficer.setUpdatedAt(OffsetDateTime.now());
        healthOfficer.setStateOfOrigin(NigerianState.Edo);
        healthOfficer.setStateOfResidence(NigerianState.Edo);
        healthOfficer.setGender(GenderType.male);
        healthOfficer.setRole(UserRole.field_officer_health);
        healthOfficer.setLga("Oredo");
        healthOfficer.setResidenceLga("Oredo");
        healthOfficer.setDateOfBirth(LocalDate.of(1992, 8, 20));
        healthOfficer.setSpecialty("HEALTH");
        healthOfficer = adminUserRepository.saveAndFlush(healthOfficer);
        entityManager.refresh(healthOfficer);
    }

    @Test
    @DisplayName("Admin: List officers without token returns 403 Forbidden")
    void testListOfficers_unauthorized() throws Exception {
        mockMvc.perform(get("/api/admin/officers"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Admin: Citizen role accessing admin officers returns 403 Forbidden")
    void testListOfficers_forbiddenForCitizen() throws Exception {
        mockMvc.perform(get("/api/admin/officers")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + citizenToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Admin: Super Admin can list all registered field officers")
    void testListOfficers_superAdminSuccess() throws Exception {
        mockMvc.perform(get("/api/admin/officers")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + superAdminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", isA(List.class)))
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))))
                .andExpect(jsonPath("$[*].citizenCode", hasItems(eduOfficer.getCitizenCode(), healthOfficer.getCitizenCode())));
    }

    @Test
    @DisplayName("Admin: Agency Admin can list all registered field officers")
    void testListOfficers_agencyAdminSuccess() throws Exception {
        mockMvc.perform(get("/api/admin/officers")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + agencyAdminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))));
    }

    @Test
    @DisplayName("Admin: Filter officers by role (field_officer_education)")
    void testListOfficers_filterByRole() throws Exception {
        mockMvc.perform(get("/api/admin/officers?role=field_officer_education")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + superAdminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].role", everyItem(is("field_officer_education"))))
                .andExpect(jsonPath("$[*].citizenCode", hasItem(eduOfficer.getCitizenCode())))
                .andExpect(jsonPath("$[*].citizenCode", not(hasItem(healthOfficer.getCitizenCode()))));
    }

    @Test
    @DisplayName("Admin: Filter officers by state (Edo)")
    void testListOfficers_filterByState() throws Exception {
        mockMvc.perform(get("/api/admin/officers?state=Edo")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + superAdminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].stateOfResidence", everyItem(is("Edo"))))
                .andExpect(jsonPath("$[*].citizenCode", hasItem(healthOfficer.getCitizenCode())));
    }

    @Test
    @DisplayName("Admin: Filter officers by query search")
    void testListOfficers_filterByQuery() throws Exception {
        mockMvc.perform(get("/api/admin/officers?query=Fatima")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + superAdminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].fullName", is("Fatima Yusuf")))
                .andExpect(jsonPath("$[0].email", is("fatima.yusuf@citieye.gov.ng")));
    }

    @Test
    @DisplayName("Admin: Retrieve single field officer by citizen code")
    void testGetOfficerByCode_success() throws Exception {
        mockMvc.perform(get("/api/admin/officers/" + eduOfficer.getCitizenCode())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + superAdminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.citizenCode", is(eduOfficer.getCitizenCode())))
                .andExpect(jsonPath("$.firstName", is("Fatima")))
                .andExpect(jsonPath("$.lastName", is("Yusuf")))
                .andExpect(jsonPath("$.role", is("field_officer_education")))
                .andExpect(jsonPath("$.specialty", is("EDUCATION")))
                .andExpect(jsonPath("$.residenceLga", is("Ikeja")));
    }

    @Test
    @DisplayName("Admin: Retrieve non-existent field officer code returns 404")
    void testGetOfficerByCode_notFound() throws Exception {
        mockMvc.perform(get("/api/admin/officers/NON-EXISTENT-CODE")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + superAdminToken))
                .andExpect(status().isNotFound());
    }
}
