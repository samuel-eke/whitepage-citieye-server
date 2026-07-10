package com.goldbyte.citieye_server.userauth.internal.filter;

import com.goldbyte.citieye_server.userauth.internal.constants.SecurityConstants;
import com.goldbyte.citieye_server.userauth.internal.service.JwtTokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

// this class is to grenerate the jwt token that will be issued to all users upon login
public class JWTTokenGenertorFilter extends OncePerRequestFilter {

    private final JwtTokenService jwtTokenService;

    public JWTTokenGenertorFilter(JwtTokenService jwtTokenService) {
        this.jwtTokenService = jwtTokenService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (null != authentication){
            Set<String> authoritiesSet = new HashSet<>();
            for (GrantedAuthority grantedAuthority : authentication.getAuthorities()){
                authoritiesSet.add(grantedAuthority.getAuthority());
            }

            String jwt = jwtTokenService.generateToken(authentication.getName(), authoritiesSet);
            response.setHeader(SecurityConstants.JWT_HEADER, jwt);
            System.out.println("the response " + response);

        }
        filterChain.doFilter(request, response);
    }
}

