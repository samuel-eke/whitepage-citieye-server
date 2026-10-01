package com.goldbyte.citieye_server.citizen_profile.internal.controller;

import com.goldbyte.citieye_server.citizen_profile.internal.dto.VocationalProfileRequest;
import com.goldbyte.citieye_server.citizen_profile.internal.dto.VocationalProfileResponse;
import com.goldbyte.citieye_server.citizen_profile.internal.service.VocationalProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

/**
 * REST controller for managing vocational profile records for artisans and unemployed citizens.
 * Exposes endpoints at {@code /api/profile/vocational}.
 */
@RestController
@RequestMapping("/api/profile/vocational")
public class VocationalProfileController {

    @Autowired
    private VocationalProfileService vocationalProfileService;

    private String resolveCitizenCode(String requestedCitizenCode) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equalsIgnoreCase(auth.getName())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized access. Please log in.");
        }

        String callerCode = auth.getName();
        boolean hasAdminPrivilege = auth.getAuthorities().stream().anyMatch(a ->
                "super_admin".equalsIgnoreCase(a.getAuthority()) ||
                "ROLE_super_admin".equalsIgnoreCase(a.getAuthority()) ||
                "EDIT_CITIZEN_PROFILE".equalsIgnoreCase(a.getAuthority()) ||
                "VIEW_CITIZEN_PROFILE".equalsIgnoreCase(a.getAuthority())
        );

        if (hasAdminPrivilege && requestedCitizenCode != null && !requestedCitizenCode.isBlank()) {
            return requestedCitizenCode.trim();
        }

        return callerCode;
    }

    /**
     * GET /api/profile/vocational
     * Retrieves the vocational profile for the authenticated citizen (or target citizen if admin).
     */
    @GetMapping
    public ResponseEntity<?> getVocationalProfile(
            @RequestParam(value = "citizenCode", required = false) String requestedCitizenCode) {

        String citizenCode = resolveCitizenCode(requestedCitizenCode);
        VocationalProfileResponse profile = vocationalProfileService.getVocationalProfile(citizenCode);
        if (profile == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Vocational profile not found for citizen: " + citizenCode));
        }
        return ResponseEntity.ok(profile);
    }

    /**
     * POST /api/profile/vocational
     * Creates or records a vocational profile for the authenticated citizen.
     */
    @PostMapping
    public ResponseEntity<?> createVocationalProfile(
            @RequestParam(value = "citizenCode", required = false) String requestedCitizenCode,
            @RequestBody(required = false) VocationalProfileRequest request) {

        String citizenCode = resolveCitizenCode(requestedCitizenCode);
        VocationalProfileResponse created = vocationalProfileService.createVocationalProfile(
                citizenCode,
                request != null ? request : new VocationalProfileRequest()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * PUT /api/profile/vocational
     * Updates an existing vocational profile for the authenticated citizen.
     */
    @PutMapping
    public ResponseEntity<?> updateVocationalProfile(
            @RequestParam(value = "citizenCode", required = false) String requestedCitizenCode,
            @RequestBody(required = false) VocationalProfileRequest request) {

        String citizenCode = resolveCitizenCode(requestedCitizenCode);
        VocationalProfileResponse updated = vocationalProfileService.updateVocationalProfile(
                citizenCode,
                request != null ? request : new VocationalProfileRequest()
        );
        return ResponseEntity.ok(updated);
    }
}
