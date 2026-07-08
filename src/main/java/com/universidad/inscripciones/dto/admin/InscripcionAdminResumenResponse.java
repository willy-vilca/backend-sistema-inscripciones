package com.universidad.inscripciones.dto.admin;

public record InscripcionAdminResumenResponse(
        long total,
        long registradas,
        long anuladas,
        long registradasHoy) {
}
