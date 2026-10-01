package com.goldbyte.citieye_server.education.internal.controller;

import com.goldbyte.citieye_server.education.internal.model.StudentAcademicUpdate;
import com.goldbyte.citieye_server.education.internal.service.StudentAcademicService;
import com.goldbyte.citieye_server.education.internal.service.StudentAcademicService.AcademicProgressionResponse;
import com.goldbyte.citieye_server.userauth.internal.service.JwtTokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class StudentAcademicControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenService jwtTokenService;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private StudentAcademicService studentAcademicService;

    @Test
    @DisplayName("R1 POST (Multipart): Successful academic update with result card file returns 201 Created")
    void testSubmitAcademicUpdate_multipartWithFile_returnsCreated() throws Exception {
        String citizenCode = "CIT-ED-ORE-00100";
        String token = jwtTokenService.generateToken(citizenCode, List.of("citizen", "SUBMIT_ACADEMIC_RECORD"));

        MockMultipartFile file = new MockMultipartFile(
                "file", "report_card.png", "image/png", "file-bytes".getBytes()
        );

        StudentAcademicUpdate created = new StudentAcademicUpdate();
        created.setId(50L);
        created.setCitizenCode(citizenCode);
        created.setStatus("SUBMITTED");
        created.setAcademicYear("2024/2025");
        created.setAcademicLevel("SECONDARY");
        created.setResultCardUrl("/uploads/result-cards/report_card_123.png");

        when(studentAcademicService.submitAcademicUpdate(eq(citizenCode), eq("SECONDARY"), eq("2024/2025"), any(), any(), any(), any(), any()))
                .thenReturn(created);

        mockMvc.perform(multipart("/api/education/student/academic-updates")
                        .file(file)
                        .param("academicLevel", "SECONDARY")
                        .param("academicYear", "2024/2025")
                        .param("institutionName", "Western Boys High School")
                        .param("classGrade", "SSS 2")
                        .header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(50L))
                .andExpect(jsonPath("$.status").value("SUBMITTED"))
                .andExpect(jsonPath("$.resultCardUrl").value("/uploads/result-cards/report_card_123.png"));
    }

    @Test
    @DisplayName("R1 POST (JSON): Successful academic update with dynamic subject-grade mappings returns 201 Created")
    void testSubmitAcademicUpdate_jsonWithSubjectGrades_returnsCreated() throws Exception {
        String citizenCode = "CIT-ED-ORE-00100";
        String token = jwtTokenService.generateToken(citizenCode, List.of("citizen", "SUBMIT_ACADEMIC_RECORD"));

        StudentAcademicController.AcademicUpdateJsonRequest request = new StudentAcademicController.AcademicUpdateJsonRequest();
        request.setAcademicLevel("TERTIARY");
        request.setAcademicYear("2024/2025");
        request.setInstitutionName("University of Benin");
        request.setClassGrade("300 Level");
        request.setSubjectGrades(Map.of("CSC301", "A", "CSC302", "B"));

        StudentAcademicUpdate created = new StudentAcademicUpdate();
        created.setId(51L);
        created.setCitizenCode(citizenCode);
        created.setStatus("SUBMITTED");
        created.setSubjectGrades(Map.of("CSC301", "A", "CSC302", "B"));

        when(studentAcademicService.submitAcademicUpdate(eq(citizenCode), eq("TERTIARY"), eq("2024/2025"), any(), any(), any(), any(), any()))
                .thenReturn(created);

        mockMvc.perform(post("/api/education/student/academic-updates/json")
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(51L))
                .andExpect(jsonPath("$.status").value("SUBMITTED"));
    }

    @Test
    @DisplayName("R1 IDOR Defense: Citizen attempting to submit academic update for another citizen returns 403 Forbidden")
    void testSubmitAcademicUpdate_citizenAttemptsIdor_returnsForbidden() throws Exception {
        String citizenCode = "CIT-ED-ORE-00100";
        String token = jwtTokenService.generateToken(citizenCode, List.of("citizen"));

        StudentAcademicController.AcademicUpdateJsonRequest request = new StudentAcademicController.AcademicUpdateJsonRequest();
        request.setCitizenCode("CIT-ED-ORE-00999"); // Tampering: attempting to submit for different citizen
        request.setAcademicLevel("SECONDARY");
        request.setAcademicYear("2024/2025");
        request.setSubjectGrades(Map.of("English", "A1"));

        mockMvc.perform(post("/api/education/student/academic-updates/json")
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("R1 IDOR Privileged: Officer submitting academic update for another citizen is permitted (201 Created)")
    void testSubmitAcademicUpdate_officerCanSpecifyTargetCitizen_success() throws Exception {
        String officerCode = "CIT-OFR-ED-ORE-0001";
        String officerToken = jwtTokenService.generateToken(officerCode, List.of("field_officer_education", "EDIT_EDUCATION_RECORD"));

        StudentAcademicController.AcademicUpdateJsonRequest request = new StudentAcademicController.AcademicUpdateJsonRequest();
        request.setCitizenCode("CIT-ED-ORE-00999"); // Officer submitting for student
        request.setAcademicLevel("SECONDARY");
        request.setAcademicYear("2024/2025");
        request.setSubjectGrades(Map.of("English", "A1"));

        StudentAcademicUpdate created = new StudentAcademicUpdate();
        created.setId(52L);
        created.setCitizenCode("CIT-ED-ORE-00999");
        created.setStatus("SUBMITTED");

        when(studentAcademicService.submitAcademicUpdate(eq("CIT-ED-ORE-00999"), eq("SECONDARY"), eq("2024/2025"), any(), any(), any(), any(), any()))
                .thenReturn(created);

        mockMvc.perform(post("/api/education/student/academic-updates/json")
                        .header(HttpHeaders.AUTHORIZATION, officerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.citizenCode").value("CIT-ED-ORE-00999"));
    }

    @Test
    @DisplayName("R1 POST: Compulsory submission validation failure returns 400 Bad Request")
    void testSubmitAcademicUpdate_missingBothFileAndGrades_returnsBadRequest() throws Exception {
        String citizenCode = "CIT-ED-ORE-00100";
        String token = jwtTokenService.generateToken(citizenCode, List.of("citizen", "SUBMIT_ACADEMIC_RECORD"));

        StudentAcademicController.AcademicUpdateJsonRequest request = new StudentAcademicController.AcademicUpdateJsonRequest();
        request.setAcademicLevel("PRIMARY");
        request.setAcademicYear("2024/2025");
        request.setInstitutionName("Emotan Primary School");
        request.setClassGrade("Primary 4");
        request.setSubjectGrades(null);

        when(studentAcademicService.submitAcademicUpdate(eq(citizenCode), eq("PRIMARY"), eq("2024/2025"), any(), any(), any(), any(), any()))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Compulsory submission requirement: either a result card document/image or subject grades mapping must be provided."));

        mockMvc.perform(post("/api/education/student/academic-updates/json")
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("R1 PATCH: Officer transitions status SUBMITTED -> VERIFIED returns 200 OK")
    void testTransitionStatus_officer_returnsOk() throws Exception {
        String officerToken = jwtTokenService.generateToken("CIT-OFR-ED-ORE-0001",
                List.of("field_officer_education", "EDIT_EDUCATION_RECORD"));

        StudentAcademicController.StatusTransitionRequest request = new StudentAcademicController.StatusTransitionRequest();
        request.setStatus("VERIFIED");
        request.setRemarks("Academic credentials confirmed");

        StudentAcademicUpdate updated = new StudentAcademicUpdate();
        updated.setId(50L);
        updated.setStatus("VERIFIED");

        when(studentAcademicService.updateStatus(eq(50L), eq("VERIFIED"), any(), eq("CIT-OFR-ED-ORE-0001")))
                .thenReturn(updated);

        mockMvc.perform(patch("/api/education/academic-updates/50/status")
                        .header(HttpHeaders.AUTHORIZATION, officerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("VERIFIED"));
    }

    @Test
    @DisplayName("R1 GET: Student queries academic progression returning full series (200 OK)")
    void testGetAcademicProgression_owner_returnsFullSeries() throws Exception {
        String citizenCode = "CIT-ED-ORE-00100";
        String token = jwtTokenService.generateToken(citizenCode, List.of("citizen", "READ_PROFILE"));

        AcademicProgressionResponse progression = new AcademicProgressionResponse(
                citizenCode, List.of(new StudentAcademicUpdate()), List.of()
        );

        when(studentAcademicService.getAcademicProgression(citizenCode)).thenReturn(progression);

        mockMvc.perform(get("/api/education/student/academic-progression/" + citizenCode)
                        .header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.citizenCode").value(citizenCode));
    }

    @Test
    @DisplayName("R1 GET: Student querying another citizen's academic progression returns 403 Forbidden")
    void testGetAcademicProgression_otherStudent_returnsForbidden() throws Exception {
        String studentAToken = jwtTokenService.generateToken("CIT-ED-ORE-00100", List.of("citizen"));

        mockMvc.perform(get("/api/education/student/academic-progression/CIT-ED-ORE-00200")
                        .header(HttpHeaders.AUTHORIZATION, studentAToken))
                .andExpect(status().isForbidden());
    }
}
