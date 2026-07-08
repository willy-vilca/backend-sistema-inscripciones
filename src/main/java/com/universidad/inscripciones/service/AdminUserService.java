package com.universidad.inscripciones.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.universidad.inscripciones.dto.admin.AdminLoginRequest;
import com.universidad.inscripciones.dto.admin.AdminLoginResponse;
import com.universidad.inscripciones.dto.admin.AdminUserCreateRequest;
import com.universidad.inscripciones.dto.admin.AdminUserEstadoRequest;
import com.universidad.inscripciones.dto.admin.AdminUserResponse;
import com.universidad.inscripciones.dto.admin.AdminUserUpdateRequest;
import com.universidad.inscripciones.model.entity.AdminUser;
import com.universidad.inscripciones.repository.AdminUserRepository;
import com.universidad.inscripciones.service.AdminTokenService.TokenData;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final AdminTokenService adminTokenService;

    @Transactional
    public AdminLoginResponse login(AdminLoginRequest request) {
        AdminUser user = adminUserRepository.findByUsername(request.username().trim())
                .orElseThrow(() -> new IllegalArgumentException("Usuario o clave incorrectos."));

        if (!user.isActivo() || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Usuario o clave incorrectos.");
        }

        user.setUltimoAccesoEn(LocalDateTime.now());
        adminUserRepository.save(user);

        TokenData token = adminTokenService.generate(user.getUsername());
        return new AdminLoginResponse(token.token(), token.expiresAt(), AdminUserResponse.fromEntity(user));
    }

    @Transactional(readOnly = true)
    public AdminUserResponse me(String token) {
        String username = adminTokenService.validateAndGetUsername(token);
        AdminUser user = adminUserRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuario administrador no encontrado."));

        if (!user.isActivo()) {
            throw new IllegalArgumentException("El usuario administrador esta inactivo.");
        }

        return AdminUserResponse.fromEntity(user);
    }

    @Transactional(readOnly = true)
    public List<AdminUserResponse> listar() {
        return adminUserRepository.findAll()
                .stream()
                .map(AdminUserResponse::fromEntity)
                .toList();
    }

    @Transactional
    public AdminUserResponse crear(AdminUserCreateRequest request) {
        validarDuplicadosCrear(request);

        AdminUser user = AdminUser.builder()
                .nombreCompleto(request.nombreCompleto().trim())
                .username(request.username().trim())
                .email(normalizarEmail(request.email()))
                .passwordHash(passwordEncoder.encode(request.password()))
                .rol(request.rol())
                .activo(request.activo())
                .build();

        return AdminUserResponse.fromEntity(adminUserRepository.save(user));
    }

    @Transactional
    public AdminUserResponse actualizar(Long id, AdminUserUpdateRequest request) {
        AdminUser user = adminUserRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario administrador no encontrado."));

        validarDuplicadosActualizar(id, request);

        user.setNombreCompleto(request.nombreCompleto().trim());
        user.setUsername(request.username().trim());
        user.setEmail(normalizarEmail(request.email()));
        user.setRol(request.rol());
        user.setActivo(request.activo());

        if (request.password() != null && !request.password().isBlank()) {
            if (request.password().length() < 6) {
                throw new IllegalArgumentException("La clave debe tener al menos 6 caracteres.");
            }
            user.setPasswordHash(passwordEncoder.encode(request.password()));
        }

        return AdminUserResponse.fromEntity(adminUserRepository.save(user));
    }

    @Transactional
    public AdminUserResponse cambiarEstado(Long id, AdminUserEstadoRequest request) {
        AdminUser user = adminUserRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario administrador no encontrado."));
        user.setActivo(request.activo());
        return AdminUserResponse.fromEntity(adminUserRepository.save(user));
    }

    private void validarDuplicadosCrear(AdminUserCreateRequest request) {
        if (adminUserRepository.existsByUsername(request.username().trim())) {
            throw new IllegalArgumentException("Ya existe un administrador con ese usuario.");
        }

        String email = normalizarEmail(request.email());
        if (email != null && adminUserRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Ya existe un administrador con ese correo.");
        }
    }

    private void validarDuplicadosActualizar(Long id, AdminUserUpdateRequest request) {
        if (adminUserRepository.existsByUsernameAndIdNot(request.username().trim(), id)) {
            throw new IllegalArgumentException("Ya existe un administrador con ese usuario.");
        }

        String email = normalizarEmail(request.email());
        if (email != null && adminUserRepository.existsByEmailAndIdNot(email, id)) {
            throw new IllegalArgumentException("Ya existe un administrador con ese correo.");
        }
    }

    private String normalizarEmail(String email) {
        return email == null || email.isBlank() ? null : email.trim().toLowerCase();
    }
}
