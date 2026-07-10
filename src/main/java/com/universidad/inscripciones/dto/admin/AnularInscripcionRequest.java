package com.universidad.inscripciones.dto.admin;

import jakarta.validation.constraints.NotBlank;

public record AnularInscripcionRequest(
        @NotBlank(message = "El motivo de anulacion es obligatorio.")
        String motivo) {
}
