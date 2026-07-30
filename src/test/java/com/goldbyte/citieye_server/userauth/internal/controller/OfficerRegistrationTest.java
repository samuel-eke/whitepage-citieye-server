package com.goldbyte.citieye_server.userauth.internal.controller;

import com.goldbyte.citieye_server.userauth.internal.dto.OfficerRegRequest;
import com.goldbyte.citieye_server.userauth.internal.model.AdminUser;
import com.goldbyte.citieye_server.userauth.internal.repo.AdminUserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class OfficerRegistrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AdminUserRepository adminUserRepository;

    @Test
    public void testOfficerSelfRegistration_healthSpecialty() throws Exception {
        String requestBody = """
                {
                    "firstName": "Chidi",
                    "middleName": "Obinna",
                    "lastName": "Eze",
                    "email": "chidi.eze@citieye.gov.ng",
                    "phoneNumber": "08099887766",
                    "password": "securepassword123",
                    "nin": "98765432109",
                    "lga": "Oredo",
                    "gender": "male",
                    "address": "12 Hospital Road, Benin",
                    "stateOfResidence": "Edo",
                    "residenceLga": "Oredo",
                    "stateOfOrigin": "Edo",
                    "dateOfBirth": "1990-06-15",
                    "specialty": "medical"
                }
                """;

        MvcResult result = mockMvc.perform(post("/api/auth/register/officer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andReturn();

        String responseString = result.getResponse().getContentAsString();
        assertTrue(responseString.contains("CIT-OFR-ED-ORE-"));
        assertTrue(responseString.contains("Field Officer registered successfully"));

        // Retrieve from database and check mapping
        Optional<AdminUser> officerOpt = adminUserRepository.findFirstByEmail("chidi.eze@citieye.gov.ng");
        assertTrue(officerOpt.isPresent());
        
        AdminUser officer = officerOpt.get();
        assertEquals("Chidi", officer.getFirstName());
        assertEquals("Obinna", officer.getMiddleName());
        assertEquals("Eze", officer.getLastName());
        assertEquals("98765432109", officer.getNin());
        assertEquals("Oredo", officer.getLga());
        assertEquals("MEDICAL", officer.getSpecialty());
        assertEquals("field_officer_health", officer.getRole().name()); // Verify specialty role mapping
        assertNotNull(officer.getCitizenCode());
        assertTrue(officer.getCitizenCode().startsWith("CIT-OFR-ED-ORE-"));
    }

    @Test
    public void testOfficerSelfRegistration_educationSpecialty() throws Exception {
        String requestBody = """
                {
                    "firstName": "Amina",
                    "lastName": "Yusuf",
                    "email": "amina.yusuf@citieye.gov.ng",
                    "phoneNumber": "08011223344",
                    "password": "securepassword123",
                    "nin": "11223344556",
                    "lga": "Wuse",
                    "gender": "female",
                    "address": "45 School Lane, Abuja",
                    "stateOfResidence": "FCT",
                    "residenceLga": "Wuse",
                    "stateOfOrigin": "FCT",
                    "dateOfBirth": "1988-10-20",
                    "specialty": "education"
                }
                """;

        MvcResult result = mockMvc.perform(post("/api/auth/register/officer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andReturn();

        String responseString = result.getResponse().getContentAsString();
        assertTrue(responseString.contains("CIT-OFR-FC-WUS-"));

        // Retrieve from database
        Optional<AdminUser> officerOpt = adminUserRepository.findFirstByEmail("amina.yusuf@citieye.gov.ng");
        assertTrue(officerOpt.isPresent());

        AdminUser officer = officerOpt.get();
        assertEquals("EDUCATION", officer.getSpecialty());
        assertEquals("field_officer_education", officer.getRole().name()); // Verify specialty role mapping
        assertTrue(officer.getCitizenCode().startsWith("CIT-OFR-FC-WUS-"));
    }
}
