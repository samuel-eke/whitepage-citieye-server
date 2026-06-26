package com.goldbyte.citieye_server.userauth.config;

import com.goldbyte.citieye_server.userauth.internal.model.CitizenAuthEntity;
import com.goldbyte.citieye_server.userauth.internal.repo.CitizenRegRepo;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
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

@Component
public class CitiEyeRegAuthProvider implements AuthenticationProvider {

    @Autowired
    CitizenRegRepo citizenRegRepo;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Override
    public @Nullable Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String citizenusername = authentication.getName();
        String citizenPassword = authentication.getCredentials().toString();

        List<CitizenAuthEntity> citizen = citizenRegRepo.findByEmail(citizenusername);

        if (citizen.size() > 0) {
            if (passwordEncoder.matches(citizenPassword, citizen.getFirst().getPassword())) {
                List<GrantedAuthority> authorities = new ArrayList<>();
                // Prepend ROLE_ to match Spring Security's hasRole expectation
                authorities.add(new SimpleGrantedAuthority("ROLE_" + citizen.getFirst().getRole().name()));
                return new UsernamePasswordAuthenticationToken(citizenusername, citizenPassword, authorities);

            } else {
                throw new BadCredentialsException("Invalid password");
            }
        } else {
            throw new BadCredentialsException("Such user doesn't exist");
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return (UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication));
    }
}
