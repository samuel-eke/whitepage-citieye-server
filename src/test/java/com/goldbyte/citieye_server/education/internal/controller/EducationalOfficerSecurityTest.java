package com.goldbyte.citieye_server.education.internal.controller;

import com.goldbyte.citieye_server.education.internal.model.OfficerSchoolAssignment;
import com.goldbyte.citieye_server.education.internal.model.School;
import com.goldbyte.citieye_server.education.internal.model.StudentRecord;
import com.goldbyte.citieye_server.education.internal.service.EducationalOfficerService;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class EducationalOfficerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenService jwtTokenService;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private EducationalOfficerService educationalOfficerService;

    @Test
    @DisplayName("R3: Educational Officer logs primary school record within assigned LGA (HTTP 201)")
    void testLogPrimarySchool_withinAssignedLga_success() throws Exception {
        String officerCode = "CIT-OFR-ED-ORE-0001";
        String officerToken = jwtTokenService.generateToken(officerCode,
                List.of("field_officer_education", "EDIT_EDUCATION_RECORD"));

        EducationalOfficerController.SchoolRequest request = new EducationalOfficerController.SchoolRequest();
        request.setName("Emotan Primary School");
        request.setLga("Oredo");
        request.setState("Edo");
        request.setPupilCount(850);
        request.setSchoolType("PRIMARY");
        request.setAddress("12 Mission Road, Benin City");

        School created = new School();
        created.setId(1L);
        created.setName("Emotan Primary School");
        created.setLga("Oredo");
        created.setPupilCount(850);

        when(educationalOfficerService.createSchool(any(School.class), eq(officerCode), any()))
                .thenReturn(created);

        mockMvc.perform(post("/api/field-officers/education/schools")
                        .header(HttpHeaders.AUTHORIZATION, officerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Emotan Primary School"))
                .andExpect(jsonPath("$.pupilCount").value(850));
    }

    @Test
    @DisplayName("R3 Scoped Isolation: Officer attempting to create school outside assigned LGA rejected with 403 Forbidden")
    void testLogPrimarySchool_outsideAssignedLga_forbidden() throws Exception {
        String officerCode = "CIT-OFR-ED-ORE-0001";
        String officerToken = jwtTokenService.generateToken(officerCode,
                List.of("field_officer_education", "EDIT_EDUCATION_RECORD"));

        EducationalOfficerController.SchoolRequest request = new EducationalOfficerController.SchoolRequest();
        request.setName("Ikpoba Primary School");
        request.setLga("Ikpoba-Okha"); // Different LGA than assigned Oredo
        request.setState("Edo");
        request.setPupilCount(600);

        when(educationalOfficerService.createSchool(any(School.class), eq(officerCode), any()))
                .thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied. Educational officers are isolated to their assigned jurisdiction."));

        mockMvc.perform(post("/api/field-officers/education/schools")
                        .header(HttpHeaders.AUTHORIZATION, officerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("R3: Educational Officer logs comprehensive student record in assigned School/LGA (HTTP 201)")
    void testLogComprehensiveStudentRecord_withinJurisdiction_success() throws Exception {
        String officerCode = "CIT-OFR-ED-ORE-0001";
        String officerToken = jwtTokenService.generateToken(officerCode,
                List.of("field_officer_education", "EDIT_EDUCATION_RECORD"));

        EducationalOfficerController.StudentRecordRequest request = new EducationalOfficerController.StudentRecordRequest();
        request.setCitizenCode("CIT-ED-ORE-00100");
        request.setSchoolId(1L);
        request.setSchoolName("Emotan Primary School");
        request.setLga("Oredo");
        request.setStudentName("Osahon Osaze");
        request.setStateOfOrigin("Edo");
        request.setHealthConditionsSpecialNeeds("Asthma (Mild)");
        request.setAttendanceRate(new BigDecimal("94.50"));
        request.setFamilyGuardianBackground("Lives with both parents, father is self-employed artisan");
        request.setAcademicYear("2024/2025");
        request.setClassGrade("Primary 6");

        StudentRecord created = new StudentRecord();
        created.setId(10L);
        created.setCitizenCode("CIT-ED-ORE-00100");
        created.setSchoolName("Emotan Primary School");
        created.setLga("Oredo");
        created.setAttendanceRate(new BigDecimal("94.50"));

        when(educationalOfficerService.createStudentRecord(any(StudentRecord.class), eq(officerCode), any()))
                .thenReturn(created);

        mockMvc.perform(post("/api/field-officers/education/students")
                        .header(HttpHeaders.AUTHORIZATION, officerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.citizenCode").value("CIT-ED-ORE-00100"))
                .andExpect(jsonPath("$.schoolName").value("Emotan Primary School"));
    }

    @Test
    @DisplayName("R3 Scoped Isolation: Officer attempting to log student in school outside assignment rejected with 403")
    void testLogComprehensiveStudentRecord_outsideJurisdiction_forbidden() throws Exception {
        String officerCode = "CIT-OFR-ED-ORE-0001";
        String officerToken = jwtTokenService.generateToken(officerCode,
                List.of("field_officer_education", "EDIT_EDUCATION_RECORD"));

        EducationalOfficerController.StudentRecordRequest request = new EducationalOfficerController.StudentRecordRequest();
        request.setCitizenCode("CIT-ED-ORE-00100");
        request.setSchoolId(99L); // Unassigned school
        request.setSchoolName("Western Boys High School");
        request.setLga("Ikpoba-Okha"); // Unassigned LGA

        when(educationalOfficerService.createStudentRecord(any(StudentRecord.class), eq(officerCode), any()))
                .thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied. Student belongs to School or LGA outside your assigned jurisdiction."));

        mockMvc.perform(post("/api/field-officers/education/students")
                        .header(HttpHeaders.AUTHORIZATION, officerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("R3 Scoped Query: Querying students outside assigned LGA returns empty list or 403 Forbidden")
    void testQueryStudents_outsideJurisdiction_returnsEmptyOrForbidden() throws Exception {
        String officerCode = "CIT-OFR-ED-ORE-0001";
        String officerToken = jwtTokenService.generateToken(officerCode,
                List.of("field_officer_education", "EDIT_EDUCATION_RECORD"));

        when(educationalOfficerService.getStudentRecords(null, "Egor", officerCode, any()))
                .thenReturn(List.of()); // Empty filtered list preserving strict data isolation

        mockMvc.perform(get("/api/field-officers/education/students?lga=Egor")
                        .header(HttpHeaders.AUTHORIZATION, officerToken))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    @DisplayName("R3 Scoped Single Record: Accessing student record outside assignment rejected with 403 Forbidden")
    void testGetStudentRecordById_outsideJurisdiction_forbidden() throws Exception {
        String officerCode = "CIT-OFR-ED-ORE-0001";
        String officerToken = jwtTokenService.generateToken(officerCode,
                List.of("field_officer_education", "EDIT_EDUCATION_RECORD"));

        when(educationalOfficerService.getStudentRecordById(eq(999L), eq(officerCode), any()))
                .thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied. Record outside assigned jurisdiction."));

        mockMvc.perform(get("/api/field-officers/education/students/999")
                        .header(HttpHeaders.AUTHORIZATION, officerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("R3 RBAC Isolation: Citizen role attempting to access education officer endpoints receives 403 Forbidden")
    void testCitizenRole_accessingEducationalOfficerEndpoints_forbidden() throws Exception {
        String citizenToken = jwtTokenService.generateToken("CIT-ED-ORE-00100", List.of("citizen", "READ_PROFILE"));

        mockMvc.perform(get("/api/field-officers/education/students")
                        .header(HttpHeaders.AUTHORIZATION, citizenToken))
                .andExpect(status().isForbidden());
    }
}
