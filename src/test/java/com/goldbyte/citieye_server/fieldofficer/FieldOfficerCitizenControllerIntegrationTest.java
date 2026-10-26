package com.goldbyte.citieye_server.fieldofficer;

import com.goldbyte.citieye_server.userauth.internal.service.JwtTokenService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class FieldOfficerCitizenControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenService jwtTokenService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private String healthOfficerCode;
    private String eduOfficerCode;
    private String genOfficerCode;

    private String edoCitizenCode;
    private String lagosCitizenCode;

    private String healthOfficerToken;
    private String eduOfficerToken;
    private String genOfficerToken;

    @BeforeEach
    public void setUp() {
        cleanUp();

        // 1. Seed Field Officers in auth.admin_user (capture trigger-computed citizen_code)
        healthOfficerCode = jdbcTemplate.queryForObject(
                "INSERT INTO auth.admin_user (first_name, last_name, phone_number, email, state_of_origin, state_of_residence, gender, nin, address, role, password, lga, residence_lga, specialty) " +
                "VALUES ('Health', 'Officer', '08088880001', 'health.officer.test@citieye.gov.ng', 'Edo'::auth.nigerian_state, 'Edo'::auth.nigerian_state, 'female'::auth.gender_type, '88888888001', 'Edo Health Office', 'field_officer_health'::auth.user_role, 'hashedpassword', 'Oredo', 'Oredo', 'HEALTH') " +
                "RETURNING citizen_code",
                String.class
        );

        eduOfficerCode = jdbcTemplate.queryForObject(
                "INSERT INTO auth.admin_user (first_name, last_name, phone_number, email, state_of_origin, state_of_residence, gender, nin, address, role, password, lga, residence_lga, specialty) " +
                "VALUES ('Edu', 'Officer', '08088880002', 'edu.officer.test@citieye.gov.ng', 'Edo'::auth.nigerian_state, 'Edo'::auth.nigerian_state, 'male'::auth.gender_type, '88888888002', 'Edo Edu Office', 'field_officer_education'::auth.user_role, 'hashedpassword', 'Oredo', 'Oredo', 'EDUCATION') " +
                "RETURNING citizen_code",
                String.class
        );

        genOfficerCode = jdbcTemplate.queryForObject(
                "INSERT INTO auth.admin_user (first_name, last_name, phone_number, email, state_of_origin, state_of_residence, gender, nin, address, role, password, lga, residence_lga, specialty) " +
                "VALUES ('General', 'Officer', '08088880003', 'gen.officer.test@citieye.gov.ng', 'Lagos'::auth.nigerian_state, 'Lagos'::auth.nigerian_state, 'female'::auth.gender_type, '88888888003', 'Lagos Field Office', 'field_officer'::auth.user_role, 'hashedpassword', 'Ikeja', 'Ikeja', 'GENERAL') " +
                "RETURNING citizen_code",
                String.class
        );

        // 2. Seed Test Citizens in auth.users_auth & citizen_profile.profiles
        // Edo Citizen
        edoCitizenCode = jdbcTemplate.queryForObject(
                "INSERT INTO auth.users_auth (first_name, middle_name, last_name, email, phone_number, date_of_birth, state_of_residence, state_of_origin, lga, residence_lga, gender, nin, address, role, password, created_at) " +
                "VALUES ('Osas', 'Paul', 'Ighodaro', 'osas.ighodaro@example.com', '08099990001', '1995-04-12', 'Edo'::auth.nigerian_state, 'Edo'::auth.nigerian_state, 'Oredo', 'Oredo', 'female'::auth.gender_type, '99999990001', '10 Airport Road Benin', 'citizen'::auth.user_role, 'hashedpw', NOW()) " +
                "RETURNING citizen_code",
                String.class
        );
        jdbcTemplate.execute(
                "INSERT INTO citizen_profile.profiles (citizen_code, school_name, current_educational_level, is_pregnant, blood_group, blood_genotype, current_employment_status, place_of_employment_business) " +
                "VALUES ('" + edoCitizenCode + "', 'Edo College', 'Tertiary', true, 'O+', 'AA', 'employed'::citizen_profile.employment_status_type, 'Benin Tech Hub')"
        );

        // Lagos Citizen
        lagosCitizenCode = jdbcTemplate.queryForObject(
                "INSERT INTO auth.users_auth (first_name, middle_name, last_name, email, phone_number, date_of_birth, state_of_residence, state_of_origin, lga, residence_lga, gender, nin, address, role, password, created_at) " +
                "VALUES ('Babatunde', 'A.', 'Adeleke', 'babatunde.adeleke@example.com', '08099990002', '1998-08-20', 'Lagos'::auth.nigerian_state, 'Lagos'::auth.nigerian_state, 'Ikeja', 'Ikeja', 'male'::auth.gender_type, '99999990002', '20 Allen Avenue Ikeja', 'citizen'::auth.user_role, 'hashedpw', NOW()) " +
                "RETURNING citizen_code",
                String.class
        );
        jdbcTemplate.execute(
                "INSERT INTO citizen_profile.profiles (citizen_code, school_name, current_educational_level, is_pregnant, blood_group, blood_genotype, current_employment_status, place_of_employment_business) " +
                "VALUES ('" + lagosCitizenCode + "', 'Lagos Academy', 'Secondary', false, 'A+', 'AS', 'unemployed'::citizen_profile.employment_status_type, NULL)"
        );

        // 3. Refresh materialized view so the seeded data is queryable in analytics.citizen_summary
        jdbcTemplate.execute("REFRESH MATERIALIZED VIEW analytics.citizen_summary");

        // 4. Generate JWT tokens with trigger-assigned officer codes
        healthOfficerToken = jwtTokenService.generateToken(healthOfficerCode, List.of("field_officer_health"));
        eduOfficerToken    = jwtTokenService.generateToken(eduOfficerCode,    List.of("field_officer_education"));
        genOfficerToken    = jwtTokenService.generateToken(genOfficerCode,    List.of("field_officer"));
    }

    @AfterEach
    public void tearDown() {
        cleanUp();
        jdbcTemplate.execute("REFRESH MATERIALIZED VIEW analytics.citizen_summary");
    }

    private void cleanUp() {
        jdbcTemplate.execute("DELETE FROM citizen_profile.profiles WHERE citizen_code IN (SELECT citizen_code FROM auth.users_auth WHERE email IN ('osas.ighodaro@example.com', 'babatunde.adeleke@example.com'))");
        jdbcTemplate.execute("DELETE FROM auth.users_auth WHERE email IN ('osas.ighodaro@example.com', 'babatunde.adeleke@example.com') OR phone_number IN ('08099990001', '08099990002') OR nin IN ('99999990001', '99999990002')");
        jdbcTemplate.execute("DELETE FROM auth.admin_user WHERE email IN ('health.officer.test@citieye.gov.ng', 'edu.officer.test@citieye.gov.ng', 'gen.officer.test@citieye.gov.ng') OR phone_number IN ('08088880001', '08088880002', '08088880003') OR nin IN ('88888888001', '88888888002', '88888888003')");
    }

    @Test
    @DisplayName("Health Officer: listAllCitizens returns only Edo citizens with health data")
    public void testListCitizens_asHealthOfficer_returnsOnlyEdoWithHealthData() throws Exception {
        mockMvc.perform(get("/api/field-officers/citizens")
                        .header(HttpHeaders.AUTHORIZATION, healthOfficerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.content[*].stateOfResidence", everyItem(is("Edo"))))
                // Confirm Edo citizen is present with health fields
                .andExpect(jsonPath("$.content[?(@.citizenCode == '" + edoCitizenCode + "')].bloodGroup").value(hasItem("O+")))
                .andExpect(jsonPath("$.content[?(@.citizenCode == '" + edoCitizenCode + "')].isPregnant").value(hasItem(true)))
                // Confirm education-specific fields are NOT present for Health Officer
                .andExpect(jsonPath("$.content[?(@.citizenCode == '" + edoCitizenCode + "')].schoolName").doesNotExist())
                .andExpect(jsonPath("$.content[?(@.citizenCode == '" + edoCitizenCode + "')].educationalLevel").doesNotExist());
    }

    @Test
    @DisplayName("Education Officer: listAllCitizens returns only Edo citizens with education data")
    public void testListCitizens_asEducationOfficer_returnsOnlyEdoWithEducationData() throws Exception {
        mockMvc.perform(get("/api/field-officers/citizens")
                        .header(HttpHeaders.AUTHORIZATION, eduOfficerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.content[*].stateOfResidence", everyItem(is("Edo"))))
                // Confirm Edo citizen is present with education fields
                .andExpect(jsonPath("$.content[?(@.citizenCode == '" + edoCitizenCode + "')].schoolName").value(hasItem("Edo College")))
                .andExpect(jsonPath("$.content[?(@.citizenCode == '" + edoCitizenCode + "')].educationalLevel").value(hasItem("Tertiary")))
                // Confirm health-specific fields are NOT present for Education Officer
                .andExpect(jsonPath("$.content[?(@.citizenCode == '" + edoCitizenCode + "')].bloodGroup").doesNotExist())
                .andExpect(jsonPath("$.content[?(@.citizenCode == '" + edoCitizenCode + "')].isPregnant").doesNotExist());
    }

    @Test
    @DisplayName("General Officer: listAllCitizens returns only Lagos citizens with general employment data")
    public void testListCitizens_asGeneralOfficer_returnsOnlyLagosWithEmploymentData() throws Exception {
        mockMvc.perform(get("/api/field-officers/citizens")
                        .header(HttpHeaders.AUTHORIZATION, genOfficerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.content[*].stateOfResidence", everyItem(is("Lagos"))))
                // Confirm Lagos citizen is present with employment fields
                .andExpect(jsonPath("$.content[?(@.citizenCode == '" + lagosCitizenCode + "')].employmentStatus").value(hasItem("unemployed")))
                // Confirm specialized fields are NOT present
                .andExpect(jsonPath("$.content[?(@.citizenCode == '" + lagosCitizenCode + "')].bloodGroup").doesNotExist())
                .andExpect(jsonPath("$.content[?(@.citizenCode == '" + lagosCitizenCode + "')].schoolName").doesNotExist());
    }

    @Test
    @DisplayName("Filter citizens by LGA within officer state")
    public void testListCitizens_filterByLga() throws Exception {
        // Query for Oredo (should return Edo citizen)
        mockMvc.perform(get("/api/field-officers/citizens?lga=Oredo")
                        .header(HttpHeaders.AUTHORIZATION, healthOfficerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.content[0].lga", is("Oredo")));

        // Query for non-existent LGA (should return empty content)
        mockMvc.perform(get("/api/field-officers/citizens?lga=NonExistentLgaXYZ")
                        .header(HttpHeaders.AUTHORIZATION, healthOfficerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.totalElements", is(0)));
    }

    @Test
    @DisplayName("Get citizen detail: allows within officer state, forbids outside officer state")
    public void testGetCitizenDetail_stateIsolation() throws Exception {
        // 1. Edo Health officer viewing Edo citizen -> 200 OK with health fields
        mockMvc.perform(get("/api/field-officers/citizens/" + edoCitizenCode)
                        .header(HttpHeaders.AUTHORIZATION, healthOfficerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.citizenCode", is(edoCitizenCode)))
                .andExpect(jsonPath("$.bloodGroup", is("O+")))
                .andExpect(jsonPath("$.isPregnant", is(true)))
                .andExpect(jsonPath("$.schoolName").doesNotExist());

        // 2. Edo Health officer viewing Lagos citizen -> 403 Forbidden
        mockMvc.perform(get("/api/field-officers/citizens/" + lagosCitizenCode)
                        .header(HttpHeaders.AUTHORIZATION, healthOfficerToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message", containsString("Forbidden. You can only view citizens in your state of residence")));
    }
}
