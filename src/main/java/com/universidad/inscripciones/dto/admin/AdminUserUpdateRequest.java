package com.universidad.inscripciones.dto.admin;

import com.universidad.inscripciones.model.enums.RolAdmin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AdminUserUpdateRequest(
        @NotBlank(message = "El nombre completo es obligatorio.")
        String nombreCompleto,

        @NotBlank(message = "El usuario es obligatorio.")
        String username,

        String email,

        String password,

        @NotNull(message = "El rol es obligatorio.")
        RolAdmin rol,

        boolean activo) {
}
