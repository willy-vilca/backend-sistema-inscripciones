package com.universidad.inscripciones.dto.publico;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.universidad.inscripciones.model.entity.PagoBancario;

public record PagoValidacionResponse(
        Long id,
        String tipo,
        String nroMovimiento,
        BigDecimal monto,
        LocalDateTime fechaPago,
        String nombreCliente,
        String descripcionPago,
        String mensaje) {

    public static PagoValidacionResponse fromEntity(PagoBancario pago) {
        return new PagoValidacionResponse(
                pago.getId(),
                "Banco",
                pago.getNroMovimiento(),
                pago.getImportePagado(),
                pago.getFechaPago(),
                pago.getNombreCliente(),
                pago.getDescripcionPago(),
                "Pago validado correctamente.");
    }
}
