package com.universidad.inscripciones.dto.pago;

public record PagoImportacionResponse(
        String archivo,
        int filasLeidas,
        int pagosImportados,
        int pagosActualizados,
        int pagosDuplicados,
        int filasOmitidas) {
}
