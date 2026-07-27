package com.goldbyte.citieye_server.fieldofficer;

import com.goldbyte.citieye_server.citizen_profile.CitizenProfilePublicService;
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

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class FieldOfficerDomainApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenService jwtTokenService;

    @MockitoBean
    private CitizenProfilePublicService citizenProfilePublicService;

    @MockitoBean
    private HealthRecordRepository healthRecordRepository;

    @MockitoBean
    private EducationRecordRepository educationRecordRepository;

    @BeforeEach
    public void setUp() {
        // Mock that the test citizen profile exists
        Mockito.when(citizenProfilePublicService.profileExists("CTI-ED-ORE-000123")).thenReturn(true);
        Mockito.when(citizenProfilePublicService.profileExists("NON-EXISTENT-CITIZEN")).thenReturn(false);

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
}

