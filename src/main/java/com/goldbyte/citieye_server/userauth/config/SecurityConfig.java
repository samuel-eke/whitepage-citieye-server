package com.goldbyte.citieye_server.userauth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity  // this informs spring security to use this configuration class for its security clearance
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
        return httpSecurity.csrf(Customizer.withDefaults()) // enable csrf
                .authorizeHttpRequests(request -> request.requestMatchers("/dashboard/**").authenticated())  // defines urls that need to be authenticated
                .authorizeHttpRequests(requests -> requests.requestMatchers("/login", "/").permitAll())
                .httpBasic(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)).build();
    }

    /**
     * This Bean is responsible for informing spring that we want to use a custom login credential setup from users.
     * */
    @Bean
    public UserDetailsService userDetailsService(){
        return new InMemoryUserDetailsManager();
    }

}
