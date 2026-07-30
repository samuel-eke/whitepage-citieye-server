package com.goldbyte.citieye_server.fieldofficer;

import com.goldbyte.citieye_server.citizen_profile.CitizenProfilePublicService;
import com.goldbyte.citieye_server.citizen_profile.CitizenTargetingParams;
import com.goldbyte.citieye_server.health.internal.repo.HealthRecordRepository;
import com.goldbyte.citieye_server.education.internal.repo.EducationRecordRepository;
import com.goldbyte.citieye_server.health.internal.model.HealthRecord;
import com.goldbyte.citieye_server.education.internal.model.EducationRecord;
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

import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.test.context.event.ApplicationEvents;
import com.goldbyte.citieye_server.health.HealthRecordLoggedEvent;
import static org.assertj.core.api.Assertions.assertThat;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@RecordApplicationEvents
public class FieldOfficerDomainApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ApplicationEvents applicationEvents;

    @Autowired
    private JwtTokenService jwtTokenService;

    @Autowired
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    @MockitoBean
    private CitizenProfilePublicService citizenProfilePublicService;

    @MockitoBean
    private HealthRecordRepository healthRecordRepository;

    @MockitoBean
    private EducationRecordRepository educationRecordRepository;

    @BeforeEach
    public void setUp() {
        // Clear and seed mock field officers in DB
        jdbcTemplate.execute("DELETE FROM auth.admin_user WHERE citizen_code IN ('CIT-OFR-ED-ORE-000001', 'CIT-OFR-ED-ORE-000002') " +
                "OR email IN ('health.officer@citieye.gov.ng', 'education.officer@citieye.gov.ng') " +
                "OR phone_number IN ('08011111111', '08022222222') " +
                "OR nin IN ('11111111111', '22222222222')");
        
        jdbcTemplate.execute("INSERT INTO auth.admin_user (first_name, last_name, phone_number, email, state_of_origin, state_of_residence, gender, nin, address, role, password, citizen_code, lga, residence_lga, specialty) " +
                "VALUES ('Test', 'Health', '08011111111', 'health.officer@citieye.gov.ng', 'Edo'::auth.nigerian_state, 'Edo'::auth.nigerian_state, 'male'::auth.gender_type, '11111111111', 'Address 1', 'field_officer_health'::auth.user_role, 'hashedpassword', NULL, 'Oredo', 'Oredo', 'HEALTH')");
        jdbcTemplate.execute("UPDATE auth.admin_user SET citizen_code = 'CIT-OFR-ED-ORE-000001' WHERE email = 'health.officer@citieye.gov.ng'");
                
        jdbcTemplate.execute("INSERT INTO auth.admin_user (first_name, last_name, phone_number, email, state_of_origin, state_of_residence, gender, nin, address, role, password, citizen_code, lga, residence_lga, specialty) " +
                "VALUES ('Test', 'Education', '08022222222', 'education.officer@citieye.gov.ng', 'Edo'::auth.nigerian_state, 'Edo'::auth.nigerian_state, 'male'::auth.gender_type, '22222222222', 'Address 2', 'field_officer_education'::auth.user_role, 'hashedpassword', NULL, 'Oredo', 'Oredo', 'EDUCATION')");
        jdbcTemplate.execute("UPDATE auth.admin_user SET citizen_code = 'CIT-OFR-ED-ORE-000002' WHERE email = 'education.officer@citieye.gov.ng'");

        // Mock that the test citizen profile exists
        Mockito.when(citizenProfilePublicService.profileExists("CTI-ED-ORE-000123")).thenReturn(true);
        Mockito.when(citizenProfilePublicService.profileExists("CTI-HEALTH-EARLY-001")).thenReturn(true);
        Mockito.when(citizenProfilePublicService.profileExists("CTI-HEALTH-SCHOOL-001")).thenReturn(true);
        Mockito.when(citizenProfilePublicService.profileExists("CTI-HEALTH-ADULT-001")).thenReturn(true);
        Mockito.when(citizenProfilePublicService.profileExists("NON-EXISTENT-CITIZEN")).thenReturn(false);

        // Mock targeting params to match the officer's state of residence (Edo)
        Mockito.when(citizenProfilePublicService.getTargetingParams("CTI-ED-ORE-000123"))
                .thenReturn(new CitizenTargetingParams(null, "Edo", "Edo", java.time.LocalDate.of(2010, 1, 1))); // Age 16 (Adolescent)
        Mockito.when(citizenProfilePublicService.getTargetingParams("CTI-HEALTH-EARLY-001"))
                .thenReturn(new CitizenTargetingParams(null, "Edo", "Edo", java.time.LocalDate.now().minusYears(3))); // Age 3 (Early Childhood)
        Mockito.when(citizenProfilePublicService.getTargetingParams("CTI-HEALTH-SCHOOL-001"))
                .thenReturn(new CitizenTargetingParams(null, "Edo", "Edo", java.time.LocalDate.now().minusYears(8))); // Age 8 (School Age)
        Mockito.when(citizenProfilePublicService.getTargetingParams("CTI-HEALTH-ADULT-001"))
                .thenReturn(new CitizenTargetingParams(null, "Edo", "Edo", java.time.LocalDate.now().minusYears(30))); // Age 30 (Adult)
        Mockito.when(citizenProfilePublicService.getTargetingParams("NON-EXISTENT-CITIZEN"))
                .thenReturn(new CitizenTargetingParams(null, null, null, null));

        // Mock repository saves to return the saved entity rather than null
        Mockito.when(healthRecordRepository.save(Mockito.any(HealthRecord.class)))
                .thenAnswer(invocation -> {
                    HealthRecord r = invocation.getArgument(0);
                    r.setId(1L);
                    return r;
                });

        Mockito.when(educationRecordRepository.save(Mockito.any(EducationRecord.class)))
                .thenAnswer(invocation -> {
                    EducationRecord r = invocation.getArgument(0);
                    r.setId(1L);
                    return r;
                });
    }

    @Test
    public void logHealthRecord_withHealthOfficerRole_returnsCreated() throws Exception {
        applicationEvents.clear();
        String healthOfficerToken = jwtTokenService.generateToken("CIT-OFR-ED-ORE-000001", List.of("field_officer_health", "SUBMIT_HEALTH_REPORT"));

        String requestBody = """
                {
                    "citizenCode": "CTI-ED-ORE-000123",
                    "recordType": "VACCINATION",
                    "description": "BCG administered",
                    "notes": "Healthy child"
                }
                """;

        mockMvc.perform(post("/api/field-officers/health/records")
                        .header(HttpHeaders.AUTHORIZATION, healthOfficerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated());

        // Verify event was published
        long count = applicationEvents.stream(HealthRecordLoggedEvent.class).count();
        assertThat(count).isEqualTo(1);

        HealthRecordLoggedEvent event = applicationEvents.stream(HealthRecordLoggedEvent.class).findFirst().orElseThrow();
        assertThat(event.getCitizenCode()).isEqualTo("CTI-ED-ORE-000123");
        assertThat(event.getRecordType()).isEqualTo("VACCINATION");
        assertThat(event.getOfficerCode()).isEqualTo("CIT-OFR-ED-ORE-000001");
    }

    @Test
    public void logHealthRecord_withEducationOfficerRole_returnsForbidden() throws Exception {
        String educationOfficerToken = jwtTokenService.generateToken("CIT-OFR-ED-ORE-000002", List.of("field_officer_education", "SUBMIT_EDUCATION_REPORT"));

        String requestBody = """
                {
                    "citizenCode": "CTI-ED-ORE-000123",
                    "recordType": "VACCINATION",
                    "description": "BCG administered"
                }
                """;

        mockMvc.perform(post("/api/field-officers/health/records")
                        .header(HttpHeaders.AUTHORIZATION, educationOfficerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isForbidden());
    }

    @Test
    public void logEducationRecord_withEducationOfficerRole_returnsCreated() throws Exception {
        String educationOfficerToken = jwtTokenService.generateToken("CIT-OFR-ED-ORE-000002", List.of("field_officer_education", "SUBMIT_EDUCATION_REPORT"));

        String requestBody = """
                {
                    "citizenCode": "CTI-ED-ORE-000123",
                    "recordType": "EXAM_RESULT",
                    "institutionName": "Government Secondary School",
                    "academicYear": "2025/2026",
                    "classGrade": "SSS 3",
                    "subjectGrades": {
                        "Mathematics": "A1",
                        "English": "B2"
                    },
                    "attendanceRate": 95.00
                }
                """;

        mockMvc.perform(post("/api/field-officers/education/records")
                        .header(HttpHeaders.AUTHORIZATION, educationOfficerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated());
    }

    @Test
    public void logEducationRecord_withHealthOfficerRole_returnsForbidden() throws Exception {
        String healthOfficerToken = jwtTokenService.generateToken("CIT-OFR-ED-ORE-000001", List.of("field_officer_health", "SUBMIT_HEALTH_REPORT"));

        String requestBody = """
                {
                    "citizenCode": "CTI-ED-ORE-000123",
                    "recordType": "EXAM_RESULT",
                    "institutionName": "Government Secondary School",
                    "academicYear": "2025/2026"
                }
                """;

        mockMvc.perform(post("/api/field-officers/education/records")
                        .header(HttpHeaders.AUTHORIZATION, healthOfficerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isForbidden());
    }

    @Test
    public void logHealthRecord_withNonExistentCitizen_returnsBadRequest() throws Exception {
        String healthOfficerToken = jwtTokenService.generateToken("CIT-OFR-ED-ORE-000001", List.of("field_officer_health", "SUBMIT_HEALTH_REPORT"));

        String requestBody = """
                {
                    "citizenCode": "NON-EXISTENT-CITIZEN",
                    "recordType": "VACCINATION",
                    "description": "BCG administered"
                }
                """;

        mockMvc.perform(post("/api/field-officers/health/records")
                        .header(HttpHeaders.AUTHORIZATION, healthOfficerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void getEducationQuestions_primary_returnsOk() throws Exception {
        String educationOfficerToken = jwtTokenService.generateToken("CIT-OFR-ED-ORE-000002", List.of("field_officer_education", "SUBMIT_EDUCATION_REPORT"));

        mockMvc.perform(get("/api/field-officers/education/questions")
                        .header(HttpHeaders.AUTHORIZATION, educationOfficerToken)
                        .param("type", "primary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].fieldName").value("primaryClassGrade"))
                .andExpect(jsonPath("$[0].dataType").value("STRING"))
                .andExpect(jsonPath("$[0].ndprSensitive").value(false));
    }

    @Test
    public void getEducationQuestions_secondary_returnsOk() throws Exception {
        String educationOfficerToken = jwtTokenService.generateToken("CIT-OFR-ED-ORE-000002", List.of("field_officer_education", "SUBMIT_EDUCATION_REPORT"));

        mockMvc.perform(get("/api/field-officers/education/questions")
                        .header(HttpHeaders.AUTHORIZATION, educationOfficerToken)
                        .param("type", "secondary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].fieldName").value("secClassGrade"))
                .andExpect(jsonPath("$[0].dataType").value("STRING"))
                .andExpect(jsonPath("$[0].ndprSensitive").value(false));
    }

    @Test
    public void getEducationQuestions_invalidType_returnsBadRequest() throws Exception {
        String educationOfficerToken = jwtTokenService.generateToken("CIT-OFR-ED-ORE-000002", List.of("field_officer_education", "SUBMIT_EDUCATION_REPORT"));

        mockMvc.perform(get("/api/field-officers/education/questions")
                        .header(HttpHeaders.AUTHORIZATION, educationOfficerToken)
                        .param("type", "university"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid education category type. Must be 'primary' or 'secondary'."));
    }

    @Test
    public void logEducationRecord_withQuestionnaireFields_returnsCreated() throws Exception {
        String educationOfficerToken = jwtTokenService.generateToken("CIT-OFR-ED-ORE-000002", List.of("field_officer_education", "SUBMIT_EDUCATION_REPORT"));

        String requestBody = """
                {
                    "citizenCode": "CTI-ED-ORE-000123",
                    "recordType": "EXAM_RESULT",
                    "institutionName": "Government Secondary School",
                    "academicYear": "2025/2026",
                    "primaryClassGrade": "Primary 5",
                    "primaryEnglishLiteracyPass": true,
                    "primaryBasicNumeracyPass": true,
                    "primaryAbsentDueToHunger": false,
                    "secSchoolType": "public",
                    "secEnrolled": true,
                    "secFeesPaidInFull": true,
                    "secDigitalLiteracyCheck": true
                }
                """;

        mockMvc.perform(post("/api/field-officers/education/records")
                        .header(HttpHeaders.AUTHORIZATION, educationOfficerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated());
    }

    @Test
    public void getHealthQuestions_resolvesBasedOnCitizenAge_returnsOk() throws Exception {
        String healthOfficerToken = jwtTokenService.generateToken("CIT-OFR-ED-ORE-000001", List.of("field_officer_health", "SUBMIT_HEALTH_REPORT"));

        // Get questions for Early Childhood (Age 3)
        mockMvc.perform(get("/api/field-officers/health/questions")
                        .header(HttpHeaders.AUTHORIZATION, healthOfficerToken)
                        .param("citizenCode", "CTI-HEALTH-EARLY-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].fieldName").value("commonBloodGroup"))
                .andExpect(jsonPath("$[8].fieldName").value("earlyHasReceivedVaccines"))
                .andExpect(jsonPath("$[8].dataType").value("BOOLEAN"));

        // Get questions for Adolescent (Age 16)
        mockMvc.perform(get("/api/field-officers/health/questions")
                        .header(HttpHeaders.AUTHORIZATION, healthOfficerToken)
                        .param("citizenCode", "CTI-ED-ORE-000123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].fieldName").value("commonBloodGroup"))
                .andExpect(jsonPath("$[8].fieldName").value("adolescentHpvCervicalCancerVaccine"))
                .andExpect(jsonPath("$[8].ndprSensitive").value(true));
    }

    @Test
    public void getHealthQuestions_forAdultCitizen_returnsCommonQuestions() throws Exception {
        String healthOfficerToken = jwtTokenService.generateToken("CIT-OFR-ED-ORE-000001", List.of("field_officer_health", "SUBMIT_HEALTH_REPORT"));

        mockMvc.perform(get("/api/field-officers/health/questions")
                        .header(HttpHeaders.AUTHORIZATION, healthOfficerToken)
                        .param("citizenCode", "CTI-HEALTH-ADULT-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(8))
                .andExpect(jsonPath("$[0].fieldName").value("commonBloodGroup"))
                .andExpect(jsonPath("$[7].fieldName").value("commonNhisSeniorPlanActive"));
    }

    @Test
    public void logHealthRecord_withCommonFields_returnsCreated() throws Exception {
        String healthOfficerToken = jwtTokenService.generateToken("CIT-OFR-ED-ORE-000001", List.of("field_officer_health", "SUBMIT_HEALTH_REPORT"));

        String requestBody = """
                {
                    "citizenCode": "CTI-HEALTH-ADULT-001",
                    "recordType": "SCREENING",
                    "description": "Logged common health indicators",
                    "commonBloodGroup": "O+",
                    "commonBloodGenotype": "AA",
                    "commonIsExpectantParent": false,
                    "commonIsPregnant": false,
                    "commonHistoryOfDiabetes": true,
                    "commonHereditaryDisease": "None",
                    "commonNhisEnrolled": true,
                    "commonNhisSeniorPlanActive": false
                }
                """;

        mockMvc.perform(post("/api/field-officers/health/records")
                        .header(HttpHeaders.AUTHORIZATION, healthOfficerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated());
    }

    @Test
    public void logHealthRecord_withValidAgeGroupFields_returnsCreated() throws Exception {
        String healthOfficerToken = jwtTokenService.generateToken("CIT-OFR-ED-ORE-000001", List.of("field_officer_health", "SUBMIT_HEALTH_REPORT"));

        String requestBody = """
                {
                    "citizenCode": "CTI-HEALTH-EARLY-001",
                    "recordType": "VACCINATION",
                    "description": "Logged childhood vaccination info",
                    "earlyHasReceivedVaccines": true,
                    "earlyBcgVaccinationCompleted": true
                }
                """;

        mockMvc.perform(post("/api/field-officers/health/records")
                        .header(HttpHeaders.AUTHORIZATION, healthOfficerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated());
    }

    @Test
    public void logHealthRecord_withMismatchingAgeGroupFields_returnsBadRequest() throws Exception {
        String healthOfficerToken = jwtTokenService.generateToken("CIT-OFR-ED-ORE-000001", List.of("field_officer_health", "SUBMIT_HEALTH_REPORT"));

        // Submit adolescent fields (adolescentSexuallyActive) for early childhood citizen (age 3)
        String requestBody = """
                {
                    "citizenCode": "CTI-HEALTH-EARLY-001",
                    "recordType": "VACCINATION",
                    "description": "Logged childhood vaccination info",
                    "adolescentSexuallyActive": true
                }
                """;

        mockMvc.perform(post("/api/field-officers/health/records")
                        .header(HttpHeaders.AUTHORIZATION, healthOfficerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid fields. Citizen age matches Early Childhood group. School age or Adolescent fields cannot be logged."));
    }
}

