package com.goldbyte.citieye_server.citizen_profile.internal.service;

import com.goldbyte.citieye_server.citizen_profile.internal.dto.VocationalProfileRequest;
import com.goldbyte.citieye_server.citizen_profile.internal.dto.VocationalProfileResponse;
import com.goldbyte.citieye_server.citizen_profile.internal.model.Profile;
import com.goldbyte.citieye_server.citizen_profile.internal.model.VocationalProfile;
import com.goldbyte.citieye_server.citizen_profile.internal.model.enums.EmploymentStatus;
import com.goldbyte.citieye_server.citizen_profile.internal.repo.ProfileRepository;
import com.goldbyte.citieye_server.citizen_profile.internal.repo.VocationalProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class VocationalProfileServiceTest {

    @Mock
    private VocationalProfileRepository vocationalProfileRepository;

    @Mock
    private ProfileRepository profileRepository;

    @InjectMocks
    private VocationalProfileService vocationalProfileService;

    private Profile artisanCitizen;
    private Profile unemployedCitizen;
    private Profile employedCitizen;
    private Profile businessOwnerCitizen;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        artisanCitizen = new Profile();
        artisanCitizen.setCitizenCode("CIT-ED-ORE-00001");
        artisanCitizen.setCurrentEmploymentStatus(EmploymentStatus.artisan);

        unemployedCitizen = new Profile();
        unemployedCitizen.setCitizenCode("CIT-ED-ORE-00002");
        unemployedCitizen.setCurrentEmploymentStatus(EmploymentStatus.unemployed);

        employedCitizen = new Profile();
        employedCitizen.setCitizenCode("CIT-ED-ORE-00003");
        employedCitizen.setCurrentEmploymentStatus(EmploymentStatus.employed);

        businessOwnerCitizen = new Profile();
        businessOwnerCitizen.setCitizenCode("CIT-ED-ORE-00004");
        businessOwnerCitizen.setCurrentEmploymentStatus(EmploymentStatus.business_owner);
    }

    @Test
    @DisplayName("R2: Successfully create vocational profile for citizen with 'artisan' status")
    void testCreateVocationalProfile_asArtisan_success() {
        when(profileRepository.findById("CIT-ED-ORE-00001")).thenReturn(Optional.of(artisanCitizen));
        when(vocationalProfileRepository.findByCitizenCode("CIT-ED-ORE-00001")).thenReturn(Optional.empty());

        VocationalProfile savedEntity = new VocationalProfile();
        savedEntity.setId(1L);
        savedEntity.setCitizenCode("CIT-ED-ORE-00001");
        savedEntity.setTradeSpecialization("Carpentry & Cabinet Making");
        when(vocationalProfileRepository.save(any(VocationalProfile.class))).thenReturn(savedEntity);

        VocationalProfileRequest request = new VocationalProfileRequest();
        request.setTradeSpecialization("Carpentry & Cabinet Making");
        request.setAcquiredSkills(List.of("Joinery", "Wood Carving"));
        request.setProficiencyLevel("INTERMEDIATE");
        request.setCareerTrajectories("Establish modern furniture enterprise");
        request.setPlannedApprenticeships("6-month master craftsman mentorship");
        request.setVocationalDevelopmentGoals("Obtain national trade certification");

        VocationalProfileResponse response = vocationalProfileService.createVocationalProfile("CIT-ED-ORE-00001", request);

        assertNotNull(response);
        assertEquals("CIT-ED-ORE-00001", response.getCitizenCode());
        assertEquals("Carpentry & Cabinet Making", response.getTradeSpecialization());
        verify(vocationalProfileRepository, times(1)).save(any(VocationalProfile.class));
    }

    @Test
    @DisplayName("R2: Successfully create vocational profile for citizen with 'unemployed' status")
    void testCreateVocationalProfile_asUnemployed_success() {
        when(profileRepository.findById("CIT-ED-ORE-00002")).thenReturn(Optional.of(unemployedCitizen));
        when(vocationalProfileRepository.findByCitizenCode("CIT-ED-ORE-00002")).thenReturn(Optional.empty());

        VocationalProfile savedEntity = new VocationalProfile();
        savedEntity.setId(2L);
        savedEntity.setCitizenCode("CIT-ED-ORE-00002");
        savedEntity.setTradeSpecialization("Solar PV Installation");
        when(vocationalProfileRepository.save(any(VocationalProfile.class))).thenReturn(savedEntity);

        VocationalProfileRequest request = new VocationalProfileRequest();
        request.setTradeSpecialization("Solar PV Installation");
        request.setAcquiredSkills(List.of("Inverter Wiring", "Panel Mounting"));
        request.setProficiencyLevel("BEGINNER");

        VocationalProfileResponse response = vocationalProfileService.createVocationalProfile("CIT-ED-ORE-00002", request);

        assertNotNull(response);
        assertEquals("CIT-ED-ORE-00002", response.getCitizenCode());
        verify(vocationalProfileRepository, times(1)).save(any(VocationalProfile.class));
    }

    @Test
    @DisplayName("R2 Gated Access: Reject citizen with 'employed' status with 400/422")
    void testCreateVocationalProfile_asEmployed_throwsGatedAccessException() {
        when(profileRepository.findById("CIT-ED-ORE-00003")).thenReturn(Optional.of(employedCitizen));

        VocationalProfileRequest request = new VocationalProfileRequest();
        request.setTradeSpecialization("Electrical Installation");

        assertThrows(ResponseStatusException.class, () ->
                vocationalProfileService.createVocationalProfile("CIT-ED-ORE-00003", request));

        verify(vocationalProfileRepository, never()).save(any());
    }

    @Test
    @DisplayName("R2 Gated Access: Reject citizen with 'business_owner' status with 400/422")
    void testCreateVocationalProfile_asBusinessOwner_throwsGatedAccessException() {
        when(profileRepository.findById("CIT-ED-ORE-00004")).thenReturn(Optional.of(businessOwnerCitizen));

        VocationalProfileRequest request = new VocationalProfileRequest();
        request.setTradeSpecialization("Welding");

        assertThrows(ResponseStatusException.class, () ->
                vocationalProfileService.createVocationalProfile("CIT-ED-ORE-00004", request));

        verify(vocationalProfileRepository, never()).save(any());
    }

    @Test
    @DisplayName("R2: Retrieve vocational profile independently from demographic fields")
    void testGetVocationalProfile_success() {
        VocationalProfile existing = new VocationalProfile();
        existing.setId(1L);
        existing.setCitizenCode("CIT-ED-ORE-00001");
        existing.setTradeSpecialization("Tailoring & Fashion");
        existing.setAcquiredSkills(List.of("Pattern Drafting", "Embroidery"));
        existing.setProficiencyLevel("ADVANCED");

        when(profileRepository.findById("CIT-ED-ORE-00001")).thenReturn(Optional.of(artisanCitizen));
        when(vocationalProfileRepository.findByCitizenCode("CIT-ED-ORE-00001")).thenReturn(Optional.of(existing));

        VocationalProfileResponse response = vocationalProfileService.getVocationalProfile("CIT-ED-ORE-00001");

        assertNotNull(response);
        assertEquals("Tailoring & Fashion", response.getTradeSpecialization());
        assertEquals("ADVANCED", response.getProficiencyLevel());
    }

    @Test
    @DisplayName("R2: Update vocational profile fields without modifying demographic fields")
    void testUpdateVocationalProfile_independentUpdate() {
        VocationalProfile existing = new VocationalProfile();
        existing.setId(1L);
        existing.setCitizenCode("CIT-ED-ORE-00001");
        existing.setTradeSpecialization("Automobile Mechanics");

        when(profileRepository.findById("CIT-ED-ORE-00001")).thenReturn(Optional.of(artisanCitizen));
        when(vocationalProfileRepository.findByCitizenCode("CIT-ED-ORE-00001")).thenReturn(Optional.of(existing));
        when(vocationalProfileRepository.save(any(VocationalProfile.class))).thenReturn(existing);

        VocationalProfileRequest updateRequest = new VocationalProfileRequest();
        updateRequest.setTradeSpecialization("Automobile Mechanics & Diagnostics");
        updateRequest.setVocationalDevelopmentGoals("Acquire OBD-II computer diagnostic scanner");

        vocationalProfileService.updateVocationalProfile("CIT-ED-ORE-00001", updateRequest);

        ArgumentCaptor<VocationalProfile> captor = ArgumentCaptor.forClass(VocationalProfile.class);
        verify(vocationalProfileRepository, times(1)).save(captor.capture());
        assertEquals("Automobile Mechanics & Diagnostics", captor.getValue().getTradeSpecialization());

        // Demographic profile repository must NEVER be saved during vocational update
        verify(profileRepository, never()).save(any(Profile.class));
    }
}
