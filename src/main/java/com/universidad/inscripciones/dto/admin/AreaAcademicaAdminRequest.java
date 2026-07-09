package com.universidad.inscripciones.dto.admin;

import jakarta.validation.constraints.NotBlank;

public record AreaAcademicaAdminRequest(
        @NotBlank(message = "El codigo es obligatorio.")
        String codigo,

        @NotBlank(message = "El nombre es obligatorio.")
        String nombre,

        boolean activo) {
}
