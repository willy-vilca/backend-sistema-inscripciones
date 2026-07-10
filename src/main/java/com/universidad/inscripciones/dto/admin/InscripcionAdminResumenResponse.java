package com.universidad.inscripciones.dto.admin;

public record InscripcionAdminResumenResponse(
        long total,
        long registradas,
        long aprobadas,
        long anuladas,
        long registradasHoy) {
}
