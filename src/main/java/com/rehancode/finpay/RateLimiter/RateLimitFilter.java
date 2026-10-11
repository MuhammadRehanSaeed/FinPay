
package com.rehancode.finpay.RateLimiter;

import com.rehancode.finpay.Exception.ApiResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

@Component
public class RateLimitFilter extends OncePerRequestFilter {
    private final RateLimitConfig config;
    private final RateLimitService rateLimitService;
    private final ObjectMapper objectMapper;

    public RateLimitFilter(RateLimitConfig config, RateLimitService rateLimitService, ObjectMapper objectMapper) {
        this.config = config;
        this.rateLimitService = rateLimitService;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();
        if (path != null && path.startsWith("/actuator")) {
            filterChain.doFilter(request, response);
            return;
        }
        String ip = request.getRemoteAddr();

        RateLimitRule rule = config.getRule(path);

        if (rule == null) {
            filterChain.doFilter(request, response);
            return;
        }

        String key = buildKey(rule, ip, path);
        if (key == null) {
            filterChain.doFilter(request, response);
            return;
        }

        boolean allowed = rateLimitService.isAllowed(key, rule);
        if (!allowed) {
            ApiResponse response1 = new ApiResponse(HttpStatus.TOO_MANY_REQUESTS.value(), "Too many requests", null);
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write(objectMapper.writeValueAsString(response1));
            return;
        }
        filterChain.doFilter(request, response);


    }

    private String buildKey(RateLimitRule rule, String ip, String path) {
        String key;
        if (rule.getIdentity() == RateLimitIdentity.IP) {
            key = ip + " " + path;
            return key;
        }
        if (rule.getIdentity() == RateLimitIdentity.USERID) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null
                    || !authentication.isAuthenticated()
                    || authentication.getPrincipal() == null
                    || !(authentication.getPrincipal() instanceof UserDetails userDetails)
            ) {
                return null;
            }
            String user = authentication.getName();
            key = user + " " + path;
            return key;
        }
        return null;
    }
}
