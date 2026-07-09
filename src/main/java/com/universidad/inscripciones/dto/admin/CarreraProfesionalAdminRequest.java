package com.universidad.inscripciones.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CarreraProfesionalAdminRequest(
        @NotNull(message = "El area academica es obligatoria.")
        Long areaAcademicaId,

        @NotBlank(message = "El nombre de la carrera es obligatorio.")
        String nombre,

        boolean activo) {
}
