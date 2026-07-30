package com.goldbyte.citieye_server.admin_content_cms.internal.controller;

import com.goldbyte.citieye_server.admin_content_cms.internal.entity.OpportunityApplicationEntity;
import com.goldbyte.citieye_server.admin_content_cms.internal.repo.OpportunityApplicationRepository;
import com.goldbyte.citieye_server.citizen_profile.CitizenProfilePublicService;
import com.goldbyte.citieye_server.citizen_profile.CitizenTargetingParams;
import com.goldbyte.citieye_server.userauth.internal.service.JwtTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDate;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class OpportunitiesIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenService jwtTokenService;

    @Autowired
    private OpportunityApplicationRepository opportunityApplicationRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockitoBean
    private CitizenProfilePublicService citizenProfilePublicService;

    private String adminToken;
    private String citizenToken;
    private String officerToken;
    private String citizenCode;

    @BeforeEach
    public void setUp() {
        // Generate static security tokens for admin
        adminToken = jwtTokenService.generateToken("admin-001", List.of("super_admin", "MANAGE_USERS"));

        // Clean tables
        jdbcTemplate.execute("DELETE FROM admins.opportunity_applications");
        jdbcTemplate.execute("DELETE FROM admins.opportunities");
        jdbcTemplate.execute("DELETE FROM citizen_profile.profiles WHERE citizen_code IN (SELECT citizen_code FROM auth.users_auth WHERE email = 'citizen.test@citieye.gov.ng')");
        jdbcTemplate.execute("DELETE FROM auth.users_auth WHERE email = 'citizen.test@citieye.gov.ng' OR phone_number = '08055555555' OR nin = '55555555555'");
        jdbcTemplate.execute("DELETE FROM auth.admin_user WHERE email = 'aisha.bello@citieye.gov.ng' OR phone_number = '08099999999' OR nin = '99999999999'");

        // Seed a field officer in auth.admin_user so names can be resolved
        jdbcTemplate.execute("INSERT INTO auth.admin_user (first_name, last_name, phone_number, email, state_of_origin, state_of_residence, gender, nin, address, role, password, citizen_code, lga, residence_lga, specialty) " +
                "VALUES ('Aisha', 'Bello', '08099999999', 'aisha.bello@citieye.gov.ng', 'Edo'::auth.nigerian_state, 'Edo'::auth.nigerian_state, 'female'::auth.gender_type, '99999999999', 'Officer Address', 'field_officer_health'::auth.user_role, 'hashedpassword', NULL, 'Oredo', 'Oredo', 'HEALTH')");

        // Retrieve generated officer code
        String officerCode = jdbcTemplate.queryForObject("SELECT citizen_code FROM auth.admin_user WHERE email = 'aisha.bello@citieye.gov.ng'", String.class);

        // Generate officerToken dynamically using the actual code
        officerToken = jwtTokenService.generateToken(officerCode, List.of("field_officer_health", "SUBMIT_HEALTH_REPORT"));

        // Insert a citizen into auth.users_auth
        jdbcTemplate.execute("INSERT INTO auth.users_auth (first_name, last_name, phone_number, email, date_of_birth, state_of_residence, state_of_origin, lga, gender, nin, address, role, password, residence_lga) " +
                "VALUES ('Test', 'Citizen', '08055555555', 'citizen.test@citieye.gov.ng', '1996-01-01', 'Edo'::auth.nigerian_state, 'Edo'::auth.nigerian_state, 'Oredo', 'male'::auth.gender_type, '55555555555', 'Citizen Address', 'citizen'::auth.user_role, '$2a$12$somehashedpasswordhere', 'Oredo')");

        // Get the automatically generated citizen code
        citizenCode = jdbcTemplate.queryForObject("SELECT citizen_code FROM auth.users_auth WHERE email = 'citizen.test@citieye.gov.ng'", String.class);

        // Seed the matching profiles record
        jdbcTemplate.update("INSERT INTO citizen_profile.profiles (citizen_code, age, updated_at) VALUES (?, 30, CURRENT_TIMESTAMP)", citizenCode);

        // Generate citizenToken dynamically using the actual code
        citizenToken = jwtTokenService.generateToken(citizenCode, List.of("citizen", "READ_PROFILE"));

        // Stub citizen profile service mocks
        Mockito.when(citizenProfilePublicService.profileExists(citizenCode)).thenReturn(true);
        Mockito.when(citizenProfilePublicService.getTargetingParams(citizenCode))
                .thenReturn(new CitizenTargetingParams(5, "Edo", "Edo", LocalDate.of(1996, 1, 1))); // Age 30 (Workforce Integration)
    }

    @Test
    public void testOpportunitiesEndToEndFlow() throws Exception {
        // 1. Admin creates a new targeted opportunity
        String opportunityPayload = """
                {
                    "title": "CitiEye National Civic Welfare Support Program",
                    "opportunityType": "GENERAL_WELFARE",
                    "status": "OPEN",
                    "brief": "Monthly basic support stipend for eligible lower-income households.",
                    "fullDescription": "### Program Overview\\nThis initiative supports...",
                    "requiredDocuments": ["CitiEye citizen code", "State residency certificate"],
                    "actionLabel": "Claim General Support voucher",
                    "targetCohortId": 5,
                    "targetStateResidence": "Edo",
                    "targetStateOrigin": "Edo"
                }
                """;

        String responseContent = mockMvc.perform(post("/api/admin/content/opportunities/new")
                        .header(HttpHeaders.AUTHORIZATION, adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(opportunityPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("CitiEye National Civic Welfare Support Program"))
                .andReturn().getResponse().getContentAsString();

        // Extract generated opportunity ID
        String opportunityId = responseContent.split("\"id\":\"")[1].split("\"")[0];

        // 2. Citizen queries targeted opportunities (verifies it shows up with NOT_APPLIED status)
        mockMvc.perform(get("/api/citizen/opportunities")
                        .header(HttpHeaders.AUTHORIZATION, citizenToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(opportunityId))
                .andExpect(jsonPath("$[0].title").value("CitiEye National Civic Welfare Support Program"))
                .andExpect(jsonPath("$[0].applicationStatus").value("NOT_APPLIED"))
                .andExpect(jsonPath("$[0].requiredDocuments[0]").value("CitiEye citizen code"))
                .andExpect(jsonPath("$[0].requiredDocuments[1]").value("State residency certificate"));

        // 3. Citizen applies for the opportunity
        mockMvc.perform(post("/api/citizen/opportunities/" + opportunityId + "/apply")
                        .header(HttpHeaders.AUTHORIZATION, citizenToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Application submitted successfully."));

        // Try applying again (should fail)
        mockMvc.perform(post("/api/citizen/opportunities/" + opportunityId + "/apply")
                        .header(HttpHeaders.AUTHORIZATION, citizenToken))
                .andExpect(status().isBadRequest());

        // Verify status changed to SUBMITTED
        mockMvc.perform(get("/api/citizen/opportunities")
                        .header(HttpHeaders.AUTHORIZATION, citizenToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].applicationStatus").value("SUBMITTED"));

        // 4. Retrieve the application ID from database for reviewing
        List<OpportunityApplicationEntity> apps = opportunityApplicationRepository.findAllByCitizenCode(citizenCode);
        String applicationId = apps.get(0).getId().toString();

        // 5. Field Officer updates the application status to APPROVED and leaves notes
        String reviewPayload = """
                {
                    "status": "APPROVED",
                    "officerNotes": "NIN details verified."
                }
                """;

        mockMvc.perform(put("/api/field-officers/opportunities/applications/" + applicationId)
                        .header(HttpHeaders.AUTHORIZATION, officerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reviewPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.applicationStatus").value("APPROVED"))
                .andExpect(jsonPath("$.officerNotes").value("NIN details verified."));

        // 6. Field Officer lists their assigned applications
        mockMvc.perform(get("/api/field-officers/opportunities/applications")
                        .header(HttpHeaders.AUTHORIZATION, officerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(applicationId))
                .andExpect(jsonPath("$[0].applicationStatus").value("APPROVED"));

        // 7. Citizen queries targeted opportunities again (verifies updated status, officer notes, and officer name)
        mockMvc.perform(get("/api/citizen/opportunities")
                        .header(HttpHeaders.AUTHORIZATION, citizenToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].applicationStatus").value("APPROVED"))
                .andExpect(jsonPath("$[0].assignedOfficerName").value("Aisha Bello"))
                .andExpect(jsonPath("$[0].officerNotes").value("NIN details verified."));
    }
}
