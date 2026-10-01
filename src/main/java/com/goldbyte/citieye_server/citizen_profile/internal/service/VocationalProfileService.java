package com.goldbyte.citieye_server.citizen_profile.internal.service;

import com.goldbyte.citieye_server.citizen_profile.internal.dto.VocationalProfileRequest;
import com.goldbyte.citieye_server.citizen_profile.internal.dto.VocationalProfileRequestDto;
import com.goldbyte.citieye_server.citizen_profile.internal.dto.VocationalProfileResponse;
import com.goldbyte.citieye_server.citizen_profile.internal.dto.VocationalProfileResponseDto;
import com.goldbyte.citieye_server.citizen_profile.internal.model.Profile;
import com.goldbyte.citieye_server.citizen_profile.internal.model.VocationalProfile;
import com.goldbyte.citieye_server.citizen_profile.internal.model.enums.EmploymentStatus;
import com.goldbyte.citieye_server.citizen_profile.internal.repo.ProfileRepository;
import com.goldbyte.citieye_server.citizen_profile.internal.repo.VocationalProfileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;

/**
 * Service managing vocational profiles for artisan and unemployed citizens.
 * Enforces strict economic status eligibility checks.
 */
@Service
public class VocationalProfileService {

    private static final Logger log = LoggerFactory.getLogger(VocationalProfileService.class);

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private VocationalProfileRepository vocationalProfileRepository;

    /**
     * Validates that the citizen exists and holds an employment status of either
     * 'artisan' or 'unemployed'.
     *
     * @param citizenCode the citizen's unique identifier
     * @throws ResponseStatusException if the profile does not exist (400) or status is ineligible (422)
     */
    public void validateVocationalEligibility(String citizenCode) {
        Profile profile = profileRepository.findById(citizenCode)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Citizen profile not found for code: " + citizenCode));

        EmploymentStatus status = profile.getCurrentEmploymentStatus();
        if (status != EmploymentStatus.artisan && status != EmploymentStatus.unemployed) {
            String current = status != null ? status.name() : "unspecified";
            log.warn("Citizen {} denied vocational access. Employment status '{}' is not artisan or unemployed.", citizenCode, current);
            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "Vocational profiling is only available for citizens with status 'artisan' or 'unemployed'. Current status: " + current);
        }
    }

    /**
     * Retrieves the vocational profile for an eligible citizen.
     */
    @Transactional(readOnly = true)
    public VocationalProfileResponse getVocationalProfile(String citizenCode) {
        validateVocationalEligibility(citizenCode);

        return vocationalProfileRepository.findByCitizenCode(citizenCode)
                .map(this::mapToResponse)
                .orElse(null);
    }

    /**
     * Creates a vocational profile for an eligible citizen.
     */
    @Transactional
    public VocationalProfileResponse createVocationalProfile(String citizenCode, VocationalProfileRequest request) {
        validateVocationalEligibility(citizenCode);

        VocationalProfile profile = vocationalProfileRepository.findByCitizenCode(citizenCode)
                .orElseGet(() -> {
                    VocationalProfile vp = new VocationalProfile();
                    vp.setCitizenCode(citizenCode);
                    vp.setCreatedAt(OffsetDateTime.now());
                    return vp;
                });

        applyRequestData(profile, request);

        profile.setUpdatedAt(OffsetDateTime.now());
        VocationalProfile saved = vocationalProfileRepository.save(profile);
        log.info("Successfully persisted vocational profile for citizen: {}", citizenCode);
        return mapToResponse(saved);
    }

    /**
     * Updates an existing vocational profile without modifying demographic data.
     */
    @Transactional
    public VocationalProfileResponse updateVocationalProfile(String citizenCode, VocationalProfileRequest request) {
        return createVocationalProfile(citizenCode, request);
    }

    /**
     * Compatibility method for VocationalProfileRequestDto.
     */
    @Transactional
    public VocationalProfileResponseDto createOrUpdateVocationalProfile(String citizenCode, VocationalProfileRequestDto request) {
        VocationalProfileResponse response = createVocationalProfile(citizenCode, request);
        return toDto(response);
    }

    private void applyRequestData(VocationalProfile profile, VocationalProfileRequest request) {
        if (request != null) {
            if (request.getTradeSpecialization() != null) {
                profile.setTradeSpecialization(request.getTradeSpecialization());
            }
            if (request.getAcquiredSkills() != null) {
                profile.setAcquiredSkills(request.getAcquiredSkillsAsString());
            }
            if (request.getProficiencyLevel() != null) {
                profile.setProficiencyLevel(request.getProficiencyLevel());
            }
            if (request.getCareerTrajectories() != null) {
                profile.setCareerTrajectories(request.getCareerTrajectories());
            }
            if (request.getPlannedApprenticeships() != null) {
                profile.setPlannedApprenticeships(request.getPlannedApprenticeships());
            }
            if (request.getVocationalDevelopmentGoals() != null) {
                profile.setVocationalDevelopmentGoals(request.getVocationalDevelopmentGoals());
            }
            if (request.getCertifications() != null) {
                profile.setCertifications(request.getCertifications());
            }
        }
    }

    private VocationalProfileResponse mapToResponse(VocationalProfile entity) {
        return VocationalProfileResponse.builder()
                .id(entity.getId())
                .citizenCode(entity.getCitizenCode())
                .tradeSpecialization(entity.getTradeSpecialization())
                .acquiredSkills(entity.getAcquiredSkills())
                .proficiencyLevel(entity.getProficiencyLevel())
                .careerTrajectories(entity.getCareerTrajectories())
                .plannedApprenticeships(entity.getPlannedApprenticeships())
                .vocationalDevelopmentGoals(entity.getVocationalDevelopmentGoals())
                .certifications(entity.getCertifications())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private VocationalProfileResponseDto toDto(VocationalProfileResponse resp) {
        if (resp == null) return null;
        VocationalProfileResponseDto dto = new VocationalProfileResponseDto();
        dto.setId(resp.getId());
        dto.setCitizenCode(resp.getCitizenCode());
        dto.setTradeSpecialization(resp.getTradeSpecialization());
        dto.setAcquiredSkills(resp.getAcquiredSkills());
        dto.setProficiencyLevel(resp.getProficiencyLevel());
        dto.setCareerTrajectories(resp.getCareerTrajectories());
        dto.setPlannedApprenticeships(resp.getPlannedApprenticeships());
        dto.setVocationalDevelopmentGoals(resp.getVocationalDevelopmentGoals());
        dto.setCertifications(resp.getCertifications());
        dto.setCreatedAt(resp.getCreatedAt());
        dto.setUpdatedAt(resp.getUpdatedAt());
        return dto;
    }
}
