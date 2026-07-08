package com.universidad.inscripciones.dto.admin;

import com.universidad.inscripciones.model.enums.RolAdmin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AdminUserCreateRequest(
        @NotBlank(message = "El nombre completo es obligatorio.")
        String nombreCompleto,

        @NotBlank(message = "El usuario es obligatorio.")
        String username,

        String email,

        @NotBlank(message = "La clave es obligatoria.")
        @Size(min = 6, message = "La clave debe tener al menos 6 caracteres.")
        String password,

        @NotNull(message = "El rol es obligatorio.")
        RolAdmin rol,

        boolean activo) {
}
