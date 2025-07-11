package com.itech.itech_backend.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;

@Component
public class JwtTokenUtil {

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Extract user ID from JWT token in the Authorization header
     */
    public Long extractUserIdFromRequest(HttpServletRequest request) {
        String token = extractTokenFromRequest(request);
        if (token != null && jwtUtil.validateToken(token)) {
            return jwtUtil.extractUserId(token);
        }
        return null;
    }

    /**
     * Extract username from JWT token in the Authorization header
     */
    public String extractUsernameFromRequest(HttpServletRequest request) {
        String token = extractTokenFromRequest(request);
        if (token != null && jwtUtil.validateToken(token)) {
            return jwtUtil.extractUsername(token);
        }
        return null;
    }

    /**
     * Extract role from JWT token in the Authorization header
     */
    public String extractRoleFromRequest(HttpServletRequest request) {
        String token = extractTokenFromRequest(request);
        if (token != null && jwtUtil.validateToken(token)) {
            return jwtUtil.extractRole(token);
        }
        return null;
    }

    /**
     * Extract JWT token from Authorization header
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    /**
     * Validate if user ID from token matches the requested user ID
     */
    public boolean validateUserAccess(HttpServletRequest request, Long requestedUserId) {
        Long tokenUserId = extractUserIdFromRequest(request);
        String role = extractRoleFromRequest(request);
        
        // Admin can access any user's data
        if ("ADMIN".equals(role)) {
            return true;
        }
        
        // User can only access their own data
        return tokenUserId != null && tokenUserId.equals(requestedUserId);
    }
}
