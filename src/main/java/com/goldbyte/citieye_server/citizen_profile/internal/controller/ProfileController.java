package com.goldbyte.citieye_server.citizen_profile.internal.controller;

import com.goldbyte.citieye_server.citizen_profile.internal.service.ProfileUpdateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Controller exposing REST endpoints for managing citizen profiles.
 */
@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    @Autowired
    private ProfileUpdateService profileUpdateService;

    /**
     * Endpoint for updating dynamic progressive profiling forms filled by citizens.
     * Checks user session to guarantee citizens can only update their own profile records,
     * unless the caller has super_admin/EDIT_CITIZEN_PROFILE administrative privileges.
     *
     * @param targetCitizenCode optional administrative override to specify a target citizen code
     * @param updates a map of progressive profile questions and user responses
     * @return HTTP status and confirmation message
     */
    @PostMapping("/progressive-update")
    public ResponseEntity<?> updateProgressiveProfile(@RequestBody Map<String, Object> updates) {
        
        String callerCode = SecurityContextHolder.getContext().getAuthentication().getName();

        if (callerCode == null || "anonymousUser".equalsIgnoreCase(callerCode)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Unauthorized access. Please log in to complete your profile."));
        }

        String targetCode = callerCode;

        // Verify if caller has superadmin or custom edit privileges
        boolean hasEditPrivileges = SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                .stream()
                .anyMatch(a -> a.getAuthority().equals("super_admin") || a.getAuthority().equals("EDIT_CITIZEN_PROFILE"));

        if (hasEditPrivileges) {
            String targetCitizenCode = null;
            org.springframework.web.context.request.RequestAttributes requestAttributes =
                    org.springframework.web.context.request.RequestContextHolder.getRequestAttributes();
            if (requestAttributes instanceof org.springframework.web.context.request.ServletRequestAttributes servletRequestAttributes) {
                targetCitizenCode = servletRequestAttributes.getRequest().getParameter("citizenCode");
            }

            if (targetCitizenCode != null && !targetCitizenCode.isBlank()) {
                targetCode = targetCitizenCode.trim();
            } else if (updates.containsKey("citizenCode")) {
                Object val = updates.get("citizenCode");
                if (val instanceof String codeStr && !codeStr.isBlank()) {
                    targetCode = codeStr.trim();
                }
            }
        }

        try {
            profileUpdateService.updateProfile(targetCode, updates);
            return ResponseEntity.ok(Map.of("message", "Profile updated successfully."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "An unexpected error occurred while updating the profile."));
        }
    }
}
