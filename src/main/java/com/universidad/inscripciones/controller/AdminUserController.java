package com.universidad.inscripciones.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.universidad.inscripciones.dto.admin.AdminUserCreateRequest;
import com.universidad.inscripciones.dto.admin.AdminUserEstadoRequest;
import com.universidad.inscripciones.dto.admin.AdminUserResponse;
import com.universidad.inscripciones.dto.admin.AdminUserUpdateRequest;
import com.universidad.inscripciones.service.AdminUserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/usuarios")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    public List<AdminUserResponse> listar() {
        return adminUserService.listar();
    }

    @PostMapping
    public AdminUserResponse crear(@Valid @RequestBody AdminUserCreateRequest request) {
        return adminUserService.crear(request);
    }

    @PutMapping("/{id}")
    public AdminUserResponse actualizar(
            @PathVariable Long id,
            @Valid @RequestBody AdminUserUpdateRequest request) {
        return adminUserService.actualizar(id, request);
    }

    @PatchMapping("/{id}/estado")
    public AdminUserResponse cambiarEstado(
            @PathVariable Long id,
            @RequestBody AdminUserEstadoRequest request) {
        return adminUserService.cambiarEstado(id, request);
    }
}
