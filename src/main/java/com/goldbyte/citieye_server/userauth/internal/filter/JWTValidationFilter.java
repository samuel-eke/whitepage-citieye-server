package com.goldbyte.citieye_server.userauth.internal.filter;

import com.goldbyte.citieye_server.userauth.internal.constants.SecurityConstants;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import com.goldbyte.citieye_server.userauth.internal.service.JwtTokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JWTValidationFilter extends OncePerRequestFilter {

    private final JwtTokenService jwtTokenService;

    public JWTValidationFilter(JwtTokenService jwtTokenService) {
        this.jwtTokenService = jwtTokenService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader(SecurityConstants.JWT_HEADER);

        if (authHeader != null && !authHeader.isBlank()) {
            String jwt = authHeader.trim();
            if (jwt.regionMatches(true, 0, "Bearer ", 0, 7)) {
                jwt = jwt.substring(7).trim();
            }
            if (jwt.startsWith("\"") && jwt.endsWith("\"") && jwt.length() >= 2) {
                jwt = jwt.substring(1, jwt.length() - 1).trim();
            }

            // Only attempt parsing if it looks like a compact JWT (must contain exactly 2 periods).
            // This safely ignores empty tokens, "undefined", "null", UUID refresh tokens, or non-JWT strings.
            if (jwt.chars().filter(ch -> ch == '.').count() == 2) {
                try {
                    Claims claims = jwtTokenService.getClaimsFromToken(jwt);

                    String username = String.valueOf(claims.get("username"));
                    String authorities = (String) claims.get("authorities");

                    Authentication authentication = new UsernamePasswordAuthenticationToken(username, null,
                            AuthorityUtils.commaSeparatedStringToAuthorityList(authorities));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } catch (Exception e) {
                    SecurityContextHolder.clearContext();
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\": \"Unauthorized\", \"message\": \"Invalid token: " + e.getMessage().replace("\"", "\\\"") + "\"}");
                    return;
                }
            }
        }
        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(jakarta.servlet.http.HttpServletRequest request)
            throws jakarta.servlet.ServletException {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String path = request.getServletPath();
        // Exclude all public auth endpoints — permitAll() in SecurityConfig does NOT
        // prevent custom OncePerRequestFilter subclasses from executing, so we must
        // explicitly skip them here.
        return path.startsWith("/api/auth/register")     // POST /api/auth/register/citizen (and any future register variants)
            || path.equals("/api/auth/citizen/login")    // POST /api/auth/citizen/login
            || path.equals("/api/auth/admin/login")      // POST /api/auth/admin/login
            || path.equals("/api/auth/states-lgas")      // GET  /api/auth/states-lgas (public lookup)
            || path.equals("/api/auth/refresh")          // POST /api/auth/refresh
            || path.equals("/api/auth/logout")           // POST /api/auth/logout
            || path.equals("/error")
            || path.equals("/");
    }

    @Override
    protected boolean shouldNotFilterErrorDispatch() {
        return true;
    }
}
