package com.universidad.inscripciones.dto.pago;

public record PagoResumenResponse(
        long totalPagos,
        long pagosDisponibles,
        long pagosUsados) {
}
