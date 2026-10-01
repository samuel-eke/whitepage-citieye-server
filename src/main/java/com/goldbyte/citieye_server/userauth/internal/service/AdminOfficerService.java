package com.goldbyte.citieye_server.userauth.internal.service;

import com.goldbyte.citieye_server.userauth.internal.dto.OfficerSummaryDto;
import com.goldbyte.citieye_server.userauth.internal.model.AdminUser;
import com.goldbyte.citieye_server.userauth.internal.model.enums.NigerianState;
import com.goldbyte.citieye_server.userauth.internal.model.enums.UserRole;
import com.goldbyte.citieye_server.userauth.internal.repo.AdminUserRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service facilitating administrative operations on registered field officers.
 * Guarantees that only field officer records (roles: field_officer, field_officer_health,
 * field_officer_education) are queried, shielding administrative and citizen credentials.
 */
@Service
public class AdminOfficerService {

    public static final Set<UserRole> FIELD_OFFICER_ROLES = Set.of(
            UserRole.field_officer,
            UserRole.field_officer_health,
            UserRole.field_officer_education
    );

    private final AdminUserRepository adminUserRepository;

    public AdminOfficerService(AdminUserRepository adminUserRepository) {
        this.adminUserRepository = adminUserRepository;
    }

    /**
     * Lists registered field officers matching the optional filtering parameters.
     */
    @Transactional(readOnly = true)
    public List<OfficerSummaryDto> getFieldOfficers(
            String roleStr,
            String specialty,
            String stateStr,
            String lga,
            String query
    ) {
        UserRole specificRole = null;
        if (roleStr != null && !roleStr.isBlank()) {
            try {
                UserRole parsed = UserRole.valueOf(roleStr.trim().toLowerCase());
                if (FIELD_OFFICER_ROLES.contains(parsed)) {
                    specificRole = parsed;
                } else {
                    return List.of(); // Non-officer role requested
                }
            } catch (IllegalArgumentException e) {
                return List.of();
            }
        }

        NigerianState parsedState = null;
        if (stateStr != null && !stateStr.isBlank()) {
            String formatted = stateStr.trim().replace(' ', '_');
            for (NigerianState s : NigerianState.values()) {
                if (s.name().equalsIgnoreCase(formatted)) {
                    parsedState = s;
                    break;
                }
            }
        }

        final UserRole filterRole = specificRole;
        final NigerianState filterState = parsedState;

        Specification<AdminUser> spec = (root, cq, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Always enforce field officer roles constraint
            if (filterRole != null) {
                predicates.add(cb.equal(root.get("role"), filterRole));
            } else {
                predicates.add(root.get("role").in(FIELD_OFFICER_ROLES));
            }

            // Specialty filter
            if (specialty != null && !specialty.isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("specialty")), specialty.trim().toLowerCase()));
            }

            // State of residence filter
            if (filterState != null) {
                predicates.add(cb.equal(root.get("stateOfResidence"), filterState));
            }

            // LGA filter (matches residenceLga or lga)
            if (lga != null && !lga.isBlank()) {
                String lgaPattern = "%" + lga.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("residenceLga")), lgaPattern),
                        cb.like(cb.lower(root.get("lga")), lgaPattern)
                ));
            }

            // Search query across name, citizenCode, email, phone number
            if (query != null && !query.isBlank()) {
                String q = "%" + query.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("firstName")), q),
                        cb.like(cb.lower(root.get("lastName")), q),
                        cb.like(cb.lower(root.get("citizenCode")), q),
                        cb.like(cb.lower(root.get("email")), q),
                        cb.like(root.get("phoneNumber"), "%" + query.trim() + "%")
                ));
            }

            if (cq != null) {
                cq.orderBy(cb.desc(root.get("createdAt")));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return adminUserRepository.findAll(spec)
                .stream()
                .map(OfficerSummaryDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a single registered field officer by their unique citizenCode.
     */
    @Transactional(readOnly = true)
    public OfficerSummaryDto getFieldOfficerByCode(String citizenCode) {
        if (citizenCode == null || citizenCode.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Citizen code is required.");
        }

        AdminUser officer = adminUserRepository.findByCitizenCodeAndRoleIn(citizenCode.trim(), FIELD_OFFICER_ROLES)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Field officer not found with citizen code: " + citizenCode));

        return OfficerSummaryDto.fromEntity(officer);
    }
}
