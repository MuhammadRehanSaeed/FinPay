package com.rehancode.finpay.JWT;

import com.rehancode.finpay.Exception.ApiResponse;
import com.rehancode.finpay.SecurityConfig.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

@Slf4j
@Component
public class JwtFilter extends OncePerRequestFilter {
    private JwtService jwtService;
    private final ObjectMapper objectMapper;
    private UserDetailsServiceImpl userDetailsService;

    public JwtFilter(JwtService jwtService, ObjectMapper objectMapper, UserDetailsServiceImpl userDetailsService) {
        this.jwtService = jwtService;
        this.objectMapper = objectMapper;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();
        if (path != null && path.startsWith("/actuator")) {
            filterChain.doFilter(request, response);
            return;
        }
        String authorizationHeader = request.getHeader("Authorization");
        String username = null;
        String token = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            token = authorizationHeader.substring(7);
        }
        try {
            if (token != null && !token.isBlank()) {
                username = jwtService.extractUsername(token);
                log.debug("JWT token received for username: {}", username);
            }

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                if (jwtService.validateToken(token, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    log.info("Authenticated user: {} with roles: {}", username, userDetails.getAuthorities());
                } else {
                    log.warn("Token validation failed for username: {}", username);
                }
            }
        } catch (Exception e) {
            log.error("JWT authentication failed - {}: {}", e.getClass().getSimpleName(), e.getMessage());
            ApiResponse<Void> errorResponse = new ApiResponse<>(
                    401,
                    "Invalid Jwt - Unauthorized",
                    null
            );
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);


            response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
            return;
        }

        filterChain.doFilter(request, response);
    }

}
