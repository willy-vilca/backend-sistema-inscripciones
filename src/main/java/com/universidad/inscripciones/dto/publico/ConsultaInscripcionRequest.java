package com.universidad.inscripciones.dto.publico;

import com.universidad.inscripciones.model.enums.TipoDocumento;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ConsultaInscripcionRequest(
        @NotNull Long procesoAdmisionId,
        @NotNull TipoDocumento tipoDocumento,
        @NotBlank String numeroDocumento) {
}
