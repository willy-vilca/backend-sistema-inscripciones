package com.universidad.inscripciones.dto.inscripcion;

public record InscripcionRegistroResponse(
        Long inscripcionId,
        String codigoPostulante,
        String mensaje,
        String carneDownloadUrl) {
}
