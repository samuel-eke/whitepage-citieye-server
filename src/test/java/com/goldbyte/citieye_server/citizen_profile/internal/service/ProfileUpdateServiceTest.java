package com.goldbyte.citieye_server.citizen_profile.internal.service;

import com.goldbyte.citieye_server.citizen_profile.internal.model.Profile;
import com.goldbyte.citieye_server.citizen_profile.internal.repo.ProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProfileUpdateServiceTest {

    @Mock
    private ProfileRepository profileRepository;

    @InjectMocks
    private ProfileUpdateService profileUpdateService;

    private Profile profile;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        profile = new Profile();
        profile.setCitizenCode("CTI-ED-LGA-00001");
        when(profileRepository.findById("CTI-ED-LGA-00001")).thenReturn(Optional.of(profile));
    }

    @Test
    void testUpdateProfile_successCoercion() {
        // Arrange
        Map<String, Object> updates = Map.of(
                "school_name", "Edo High School",
                "finished_primary_6", "true",
                "literacy_score_percent", 85,
                "commute_distance_km", "4.5",
                "cardio_screening_date", "2026-05-12",
                "nassp_welfare_registered", true // Testing the custom mapper override
        );

        // Act
        profileUpdateService.updateProfile("CTI-ED-LGA-00001", updates);

        // Assert
        ArgumentCaptor<Profile> profileCaptor = ArgumentCaptor.forClass(Profile.class);
        verify(profileRepository, times(1)).save(profileCaptor.capture());
        
        Profile savedProfile = profileCaptor.getValue();
        assertEquals("Edo High School", savedProfile.getSchoolName());
        assertTrue(savedProfile.getFinishedPrimary6());
        assertEquals(85, savedProfile.getLiteracyScorePercent());
        assertEquals(new BigDecimal("4.5"), savedProfile.getCommuteDistanceKm());
        assertEquals(LocalDate.of(2026, 5, 12), savedProfile.getCardioScreeningDate());
        assertTrue(savedProfile.getNassWelfareRegistered()); // Verified override
        assertNotNull(savedProfile.getUpdatedAt());
    }

    @Test
    void testUpdateProfile_ignoresSystemImmutableFields() {
        // Arrange
        profile.setAge(25);
        Map<String, Object> updates = Map.of(
                "citizen_code", "MALICIOUS_CODE",
                "age", 99,
                "school_name", "Valid School"
        );

        // Act
        profileUpdateService.updateProfile("CTI-ED-LGA-00001", updates);

        // Assert
        ArgumentCaptor<Profile> profileCaptor = ArgumentCaptor.forClass(Profile.class);
        verify(profileRepository, times(1)).save(profileCaptor.capture());

        Profile savedProfile = profileCaptor.getValue();
        assertEquals("CTI-ED-LGA-00001", savedProfile.getCitizenCode()); // Unchanged
        assertEquals(25, savedProfile.getAge()); // Unchanged
        assertEquals("Valid School", savedProfile.getSchoolName()); // Updated
    }

    @Test
    void testUpdateProfile_invalidFieldTypeCoercion_throwsException() {
        // Arrange
        Map<String, Object> updates = Map.of("cgpa", "not-a-number");

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                profileUpdateService.updateProfile("CTI-ED-LGA-00001", updates)
        );
        assertTrue(exception.getMessage().contains("Invalid decimal value"));
        verify(profileRepository, never()).save(any());
    }

    @Test
    void testUpdateProfile_unknownField_throwsException() {
        // Arrange
        Map<String, Object> updates = Map.of("completely_made_up_column", "value");

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                profileUpdateService.updateProfile("CTI-ED-LGA-00001", updates)
        );
        assertTrue(exception.getMessage().contains("Unknown profile field"));
        verify(profileRepository, never()).save(any());
    }
}
