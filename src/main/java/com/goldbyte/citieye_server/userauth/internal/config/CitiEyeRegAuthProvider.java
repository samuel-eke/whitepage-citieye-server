package com.goldbyte.citieye_server.userauth.internal.config;

import com.goldbyte.citieye_server.userauth.FetchUserAuthoritiesEvent;
import com.goldbyte.citieye_server.userauth.internal.model.CitizenAuthEntity;
import com.goldbyte.citieye_server.userauth.internal.repo.CitizenRegRepo;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class CitiEyeRegAuthProvider implements AuthenticationProvider {

    @Autowired
    CitizenRegRepo citizenRegRepo;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Override
    public @Nullable Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String identifier = authentication.getName();
        String citizenPassword = authentication.getCredentials().toString();

        // Resolve user by identifier type (email, NIN, phone, or citizen code)
        Optional<CitizenAuthEntity> citizenOpt = resolveUser(identifier);

        if (citizenOpt.isPresent()) {
            CitizenAuthEntity citizen = citizenOpt.get();
            if (passwordEncoder.matches(citizenPassword, citizen.getPassword())) {
                List<GrantedAuthority> authorities = new ArrayList<>();
                // Add role directly as GrantedAuthority (relying on custom GrantedAuthorityDefaults to omit prefix)
                authorities.add(new SimpleGrantedAuthority(citizen.getRole().name()));
                
                // Publish event to query granular permissions from the RBAC module
                FetchUserAuthoritiesEvent event = new FetchUserAuthoritiesEvent(citizen.getRole().name(), citizen.getCitizenCode());
                eventPublisher.publishEvent(event);
                
                // Add permissions as authorities
                for (String permission : event.getAuthorities()) {
                    authorities.add(new SimpleGrantedAuthority(permission));
                }

                return new UsernamePasswordAuthenticationToken(identifier, citizenPassword, authorities);

            } else {
                throw new BadCredentialsException("Invalid password");
            }
        } else {
            throw new BadCredentialsException("Such user doesn't exist");
        }
    }


    /**
     * Resolves a user by detecting the identifier type and querying accordingly.
     * - Contains '@' → email
     * - Exactly 11 digits → NIN (fallback to phone)
     * - Starts with '0' or '+' → phone number
     * - Anything else → citizen code
     */
    private Optional<CitizenAuthEntity> resolveUser(String identifier) {
        if (identifier.contains("@")) {
            return citizenRegRepo.findFirstByEmail(identifier);
        }

        if (identifier.matches("\\d{11}")) {
            Optional<CitizenAuthEntity> byNin = citizenRegRepo.findFirstByNin(identifier);
            if (byNin.isPresent()) {
                return byNin;
            }
            return citizenRegRepo.findFirstByPhoneNumber(identifier);
        }

        if (identifier.startsWith("0") || identifier.startsWith("+")) {
            Optional<CitizenAuthEntity> byPhone = citizenRegRepo.findFirstByPhoneNumber(identifier);
            if (byPhone.isPresent()) {
                return byPhone;
            }
        }

        return citizenRegRepo.findFirstByCitizenCode(identifier);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return (UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication));
    }
}

