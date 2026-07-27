package com.goldbyte.citieye_server.citizen_profile.internal.controller;

import com.goldbyte.citieye_server.citizen_profile.internal.service.ProfileUpdateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

class ProfileControllerTest {

    @Mock
    private ProfileUpdateService profileUpdateService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private ProfileController profileController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void testUpdateProgressiveProfile_success() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("CTI-ED-LGA-00001");
        
        Map<String, Object> updates = Map.of("school_name", "Gov College");
        doNothing().when(profileUpdateService).updateProfile("CTI-ED-LGA-00001", updates);

        // Act
        ResponseEntity<?> response = profileController.updateProgressiveProfile(updates);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Profile updated successfully.", ((Map<?, ?>) response.getBody()).get("message"));
        verify(profileUpdateService, times(1)).updateProfile("CTI-ED-LGA-00001", updates);
    }

    @Test
    void testUpdateProgressiveProfile_unauthorized() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("anonymousUser");

        Map<String, Object> updates = Map.of("school_name", "Gov College");

        // Act
        ResponseEntity<?> response = profileController.updateProgressiveProfile(updates);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Unauthorized access. Please log in to complete your profile.", ((Map<?, ?>) response.getBody()).get("message"));
        verify(profileUpdateService, never()).updateProfile(anyString(), anyMap());
    }

    @Test
    void testUpdateProgressiveProfile_invalidFieldException() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("CTI-ED-LGA-00001");

        Map<String, Object> updates = Map.of("invalid_field", "value");
        doThrow(new IllegalArgumentException("Unknown profile field: invalidField"))
                .when(profileUpdateService).updateProfile("CTI-ED-LGA-00001", updates);

        // Act
        ResponseEntity<?> response = profileController.updateProgressiveProfile(updates);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Unknown profile field: invalidField", ((Map<?, ?>) response.getBody()).get("message"));
    }
}
