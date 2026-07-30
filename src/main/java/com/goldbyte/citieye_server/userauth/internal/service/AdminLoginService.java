package com.goldbyte.citieye_server.userauth.internal.service;

import com.goldbyte.citieye_server.userauth.FetchUserAuthoritiesEvent;
import com.goldbyte.citieye_server.userauth.internal.dto.AdminUserLoginReq;
import com.goldbyte.citieye_server.userauth.internal.dto.AdminUserLoginResp;
import com.goldbyte.citieye_server.userauth.internal.model.AdminUser;
import com.goldbyte.citieye_server.userauth.internal.repo.AdminUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service handling admin login logic.
 * Supports authentication via citizen code + password.
 * Retrieves RBAC permissions through the Spring Modulith event system,
 * then issues a signed JWT token for stateless authentication.
 */
@Service
public class AdminLoginService {

    @Autowired
    private AdminUserRepository adminUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private JwtTokenService jwtTokenService;

    /**
     * Authenticates an admin user using their citizen code and password.
     *
     * @param request the login request containing the admin citizen code and password
     * @return a fully populated {@link AdminUserLoginResp} including the JWT token
     * @throws BadCredentialsException if the citizen code is not found or the password does not match
     */
    @Transactional(readOnly = true)
    public AdminUserLoginResp login(AdminUserLoginReq request) {
        String code = request.getCode().trim();

        Optional<AdminUser> adminOpt = adminUserRepository.findFirstByCitizenCode(code);
        if (adminOpt.isEmpty()) {
            throw new BadCredentialsException("Invalid citizen code or password");
        }

        AdminUser admin = adminOpt.get();

        if (!passwordEncoder.matches(request.getPassword(), admin.getPassword())) {
            throw new BadCredentialsException("Invalid citizen code or password");
        }


        // Publish event to RBAC module to retrieve granular permissions
        FetchUserAuthoritiesEvent event = new FetchUserAuthoritiesEvent(
                admin.getRole().name(),
                admin.getCitizenCode()
        );
        eventPublisher.publishEvent(event);
        List<String> permissions = event.getAuthorities();

        // Build authority list for JWT (role + permissions)
        List<String> tokenAuthorities = new ArrayList<>();
        tokenAuthorities.add(admin.getRole().name());
        if (permissions != null) {
            tokenAuthorities.addAll(permissions);
        }

        String token = jwtTokenService.generateToken(admin.getCitizenCode(), tokenAuthorities);

        return AdminUserLoginResp.builder()
                .firstName(admin.getFirstName())
                .middleName(admin.getMiddleName())
                .lastName(admin.getLastName())
                .email(admin.getEmail())
                .citizenCode(admin.getCitizenCode())
                .role(admin.getRole().name())
                .stateOfResidence(admin.getStateOfResidence() != null ? admin.getStateOfResidence().name() : null)
                .lga(admin.getLga())
                .residenceLga(admin.getResidenceLga())
                .permissions(permissions)
                .token(token)
                .message("Login successful")
                .build();
    }
}


