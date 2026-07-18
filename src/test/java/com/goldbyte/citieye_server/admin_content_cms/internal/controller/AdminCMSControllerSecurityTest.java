package com.goldbyte.citieye_server.admin_content_cms.internal.controller;

import com.goldbyte.citieye_server.userauth.internal.service.JwtTokenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class AdminCMSControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenService jwtTokenService;

    @Test
    public void getPosts_unauthenticated_returnsForbiddenOrUnauthorized() throws Exception {
        mockMvc.perform(get("/api/admin/content"))
                .andExpect(status().isForbidden());
    }

    @Test
    public void getPosts_citizenRole_returnsForbidden() throws Exception {
        String citizenToken = jwtTokenService.generateToken("cit-123456", List.of("citizen", "READ_PROFILE"));
        
        mockMvc.perform(get("/api/admin/content")
                        .header(HttpHeaders.AUTHORIZATION, citizenToken))
                .andExpect(status().isForbidden());
    }

    @Test
    public void getPosts_agencyAdminRole_returnsOk() throws Exception {
        String agencyAdminToken = jwtTokenService.generateToken("cit-adm-123456", List.of("agency_admin", "PUBLISH_CMS"));
        
        mockMvc.perform(get("/api/admin/content")
                        .header(HttpHeaders.AUTHORIZATION, agencyAdminToken))
                .andExpect(status().isOk());
    }

    @Test
    public void getPosts_superAdminRole_returnsOk() throws Exception {
        String superAdminToken = jwtTokenService.generateToken("cit-super-123456", List.of("super_admin", "MANAGE_USERS"));
        
        mockMvc.perform(get("/api/admin/content")
                        .header(HttpHeaders.AUTHORIZATION, superAdminToken))
                .andExpect(status().isOk());
    }

    @Test
    public void createPost_superAdminRole_returnsOk() throws Exception {
        String superAdminToken = jwtTokenService.generateToken("cit-super-123456", List.of("super_admin", "MANAGE_USERS"));
        
        String requestBody = """
                {
                    "title": "Test Title",
                    "cmsContent": "Test Content",
                    "author": "Super Admin",
                    "contentCategory": "Announcement",
                    "datePublished": "2026-07-18T00:00:00Z"
                }
                """;

        mockMvc.perform(post("/api/admin/content/new")
                        .header(HttpHeaders.AUTHORIZATION, superAdminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk());
    }
}
