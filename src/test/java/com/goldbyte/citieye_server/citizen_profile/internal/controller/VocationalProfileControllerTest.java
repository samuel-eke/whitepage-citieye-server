package com.goldbyte.citieye_server.citizen_profile.internal.controller;

import com.goldbyte.citieye_server.citizen_profile.internal.dto.VocationalProfileRequest;
import com.goldbyte.citieye_server.citizen_profile.internal.dto.VocationalProfileResponse;
import com.goldbyte.citieye_server.citizen_profile.internal.service.VocationalProfileService;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class VocationalProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenService jwtTokenService;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private VocationalProfileService vocationalProfileService;

    @Test
    @DisplayName("R2 POST: Artisan citizen successfully records vocational profile (HTTP 201)")
    void testCreateVocationalProfile_artisan_returnsCreated() throws Exception {
        String token = jwtTokenService.generateToken("CIT-ED-ORE-00001", List.of("citizen", "READ_PROFILE"));

        VocationalProfileRequest request = new VocationalProfileRequest();
        request.setTradeSpecialization("Bricklaying & Masonry");
        request.setAcquiredSkills(List.of("Plastering", "Tiling"));
        request.setProficiencyLevel("INTERMEDIATE");

        VocationalProfileResponse response = new VocationalProfileResponse();
        response.setId(1L);
        response.setCitizenCode("CIT-ED-ORE-00001");
        response.setTradeSpecialization("Bricklaying & Masonry");

        when(vocationalProfileService.createVocationalProfile(eq("CIT-ED-ORE-00001"), any(VocationalProfileRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/profile/vocational")
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.citizenCode").value("CIT-ED-ORE-00001"))
                .andExpect(jsonPath("$.tradeSpecialization").value("Bricklaying & Masonry"));
    }

    @Test
    @DisplayName("R2 POST Gated: Employed citizen rejected with 400 or 422")
    void testCreateVocationalProfile_employed_returnsBadRequestOrUnprocessable() throws Exception {
        String token = jwtTokenService.generateToken("CIT-ED-ORE-00003", List.of("citizen", "READ_PROFILE"));

        VocationalProfileRequest request = new VocationalProfileRequest();
        request.setTradeSpecialization("Plumbing");

        when(vocationalProfileService.createVocationalProfile(eq("CIT-ED-ORE-00003"), any(VocationalProfileRequest.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Vocational profile only accessible for artisan or unemployed citizens"));

        mockMvc.perform(post("/api/profile/vocational")
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("R2 GET: Retrieve vocational profile successfully (HTTP 200)")
    void testGetVocationalProfile_success() throws Exception {
        String token = jwtTokenService.generateToken("CIT-ED-ORE-00001", List.of("citizen", "READ_PROFILE"));

        VocationalProfileResponse response = new VocationalProfileResponse();
        response.setId(1L);
        response.setCitizenCode("CIT-ED-ORE-00001");
        response.setTradeSpecialization("Fashion Design");

        when(vocationalProfileService.getVocationalProfile("CIT-ED-ORE-00001")).thenReturn(response);

        mockMvc.perform(get("/api/profile/vocational")
                        .header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tradeSpecialization").value("Fashion Design"));
    }

    @Test
    @DisplayName("R2 Security: Unauthenticated request rejected (HTTP 401/403)")
    void testVocationalEndpoints_unauthenticated_rejected() throws Exception {
        mockMvc.perform(get("/api/profile/vocational"))
                .andExpect(status().isForbidden());
    }
}
