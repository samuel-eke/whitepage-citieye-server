package com.goldbyte.citieye_server.userauth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity  // this informs spring security to use this configuration class for its security clearance
@EnableMethodSecurity
public class SecurityConfig {

    /**
     * this bean is responsible for informing spring that we are using our own security architecture
     * CONFIGURATIONS DONE
     * 1. enabled csrf token
     * 2. ensured that all requests are authenticated first
     * 3. allowed requests to be authenticated using basic authentication
     * 4. made the session stateless for JWT
     * */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception{
        return httpSecurity.csrf(csrf -> csrf.disable()) // disable csrf
                .authorizeHttpRequests(requests -> requests
                        .requestMatchers("/login", "/register", "/", "/content", "/api/user/register").permitAll()
                        .requestMatchers("/api/admin/content/**").hasAnyRole("agency_admin", "super_admin")
                        .anyRequest().authenticated()
                )
                .httpBasic(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)).build();
    }

    @Bean
    public PasswordEncoder myPasswordEncoder(){
        return new BCryptPasswordEncoder();
    }

}

