package com.universidad.inscripciones.dto.admin;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;

public record ProcesoAdmisionAdminRequest(
        @NotBlank(message = "El codigo es obligatorio.")
        String codigo,

        @NotBlank(message = "El nombre es obligatorio.")
        String nombre,

        String descripcion,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        boolean activo) {
}
