package com.goldbyte.citieye_server.userauth.internal.config;

import com.goldbyte.citieye_server.userauth.internal.filter.CsrfCookieFilter;
import com.goldbyte.citieye_server.userauth.internal.filter.JWTTokenGenertorFilter;
import com.goldbyte.citieye_server.userauth.internal.filter.JWTValidationFilter;
import jakarta.servlet.http.HttpServletRequest;
import org.jspecify.annotations.Nullable;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.config.core.GrantedAuthorityDefaults;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.goldbyte.citieye_server.userauth.internal.service.JwtTokenService;
import org.springframework.beans.factory.annotation.Autowired;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtTokenService jwtTokenService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        CsrfTokenRequestAttributeHandler csrfHandler = new CsrfTokenRequestAttributeHandler();
        csrfHandler.setCsrfRequestAttributeName("_csrf"); // this is to look for csrf attribute with user requests

        httpSecurity
                .cors(cors -> cors.configurationSource(new CorsConfigurationSource() {
                    @Override
                    public @Nullable CorsConfiguration getCorsConfiguration(HttpServletRequest request) {
                        CorsConfiguration configuration = new CorsConfiguration();
                        configuration.setAllowedOriginPatterns(Arrays.asList(
                                "http://localhost:*",
                                "http://127.0.0.1:*",
                                "http://10.0.2.2:*",
                                "https://*.vercel.app",
                                "https://*.citieye.gov.ng",
                                "*"
                        ));
                        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
                        configuration.setAllowCredentials(true);
                        configuration.setAllowedHeaders(Collections.singletonList("*"));
                        configuration.setExposedHeaders(Arrays.asList("Authorization", "Set-Cookie", "Access-Control-Allow-Origin", "Access-Control-Allow-Credentials"));
                        configuration.setMaxAge(86400L);
                        return configuration;
                    }
                }))
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers(
                                "/api/auth/register/**", // Public URLs that don't require CSRF tokens
                                "/api/auth/citizen/login", // Citizen login endpoint doesn't require CSRF tokens
                                "/api/auth/admin/login", // Admin login endpoint doesn't require CSRF tokens
                                "/api/auth/refresh",
                                "/api/auth/logout",
                                "/api/admin/content/**",
                                "/api/field-officers/health/**",
                                "/api/field-officers/education/**",
                                "/api/field-officers/opportunities/**",
                                "/api/citizen/**",
                                "/api/profile/**",
                                "/api/analytics/**")
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .csrfTokenRequestHandler(csrfHandler)) // Enable CSRF protection with cookie-based token
                                                               // repository
                .addFilterAfter(new CsrfCookieFilter(), BasicAuthenticationFilter.class) // the csrf filter will run
                                                                                         // after authentication is
                                                                                         // successful
                .addFilterAfter(new JWTTokenGenertorFilter(jwtTokenService), BasicAuthenticationFilter.class)
                .addFilterBefore(new JWTValidationFilter(jwtTokenService), BasicAuthenticationFilter.class)
                .authorizeHttpRequests(requests -> requests
                        .requestMatchers(
                                "/",
                                "/error",
                                "/api/auth/citizen/login",
                                "/api/auth/admin/login",
                                "/api/auth/register/**",
                                "/api/auth/refresh",
                                "/api/auth/logout")
                        .permitAll()
                        .requestMatchers("/api/admin/content", "/api/admin/content/**")
                        .hasAnyRole("agency_admin", "super_admin")
                        .requestMatchers("/api/field-officers/health/**")
                        .hasAnyRole("field_officer_health", "agency_admin", "super_admin")
                        .requestMatchers("/api/field-officers/education/**")
                        .hasAnyRole("field_officer_education", "agency_admin", "super_admin")
                        // Analytics dashboard — agency_admin and super_admin only
                        .requestMatchers("/api/analytics/summary", "/api/analytics/citizens",
                                "/api/analytics/citizens/**")
                        .hasAnyRole("agency_admin", "super_admin")
                        // Manual view refresh — super_admin only
                        .requestMatchers("/api/analytics/refresh")
                        .hasRole("super_admin")
                        .requestMatchers("/read")
                        .hasAnyAuthority("agency_admin", "super_admin")
                        .anyRequest().authenticated())
                .httpBasic(httpBasic -> httpBasic.disable()) // Disable HTTP Basic auth
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        return httpSecurity.build();
    }

    @Bean
    public RoleHierarchy roleHierarchy() {
        return RoleHierarchyImpl.fromHierarchy("super_admin > agency_admin\n" +
                "agency_admin > field_officer\n" +
                "agency_admin > field_officer_health\n" +
                "agency_admin > field_officer_education\n" +
                "field_officer > citizen\n" +
                "field_officer_health > citizen\n" +
                "field_officer_education > citizen");
    }

    @Bean
    public GrantedAuthorityDefaults grantedAuthorityDefaults() {
        return new GrantedAuthorityDefaults(""); // Remove the ROLE_ prefix
    }

    @Bean
    public PasswordEncoder myPasswordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
