package com.universidad.inscripciones.config;

import java.io.IOException;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.universidad.inscripciones.dto.admin.AdminUserResponse;
import com.universidad.inscripciones.model.enums.RolAdmin;
import com.universidad.inscripciones.service.AdminUserService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AdminAuthFilter extends OncePerRequestFilter {

    private final AdminUserService adminUserService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();
        if (!path.startsWith("/api/admin")
                || path.equals("/api/admin/auth/login")
                || HttpMethod.OPTIONS.matches(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String token = extractToken(request.getHeader(HttpHeaders.AUTHORIZATION));
            AdminUserResponse user = adminUserService.me(token);
            if (path.startsWith("/api/admin/usuarios") && user.rol() != RolAdmin.ADMIN) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json");
                response.getWriter().write("{\"message\":\"No tienes permiso para gestionar usuarios.\"}");
                return;
            }
            filterChain.doFilter(request, response);
        } catch (IllegalArgumentException ex) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"message\":\"" + ex.getMessage() + "\"}");
        }
    }

    private String extractToken(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Debes iniciar sesion como administrador.");
        }
        return authorization.substring(7);
    }
}
