package com.universidad.inscripciones.dto.pago;

public record PagoImportacionResponse(
        String archivo,
        int filasLeidas,
        int pagosImportados,
        int pagosDuplicados,
        int filasOmitidas) {
}
