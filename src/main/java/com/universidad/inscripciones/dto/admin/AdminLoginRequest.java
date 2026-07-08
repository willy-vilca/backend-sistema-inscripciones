package com.universidad.inscripciones.dto.admin;

import jakarta.validation.constraints.NotBlank;

public record AdminLoginRequest(
        @NotBlank(message = "El usuario es obligatorio.")
        String username,

        @NotBlank(message = "La clave es obligatoria.")
        String password) {
}
