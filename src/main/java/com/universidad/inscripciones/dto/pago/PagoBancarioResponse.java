package com.universidad.inscripciones.dto.pago;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.universidad.inscripciones.model.entity.PagoBancario;

public record PagoBancarioResponse(
        Long id,
        String nombreCliente,
        String codigo,
        String descripcionPago,
        BigDecimal importeAPagar,
        BigDecimal importePagado,
        String oficina,
        String nroMovimiento,
        LocalDateTime fechaPago,
        LocalDate fechaProceso,
        String formaPago,
        String canal,
        String archivoOrigen,
        boolean usado,
        LocalDateTime usadoEn,
        LocalDateTime creadoEn) {

    public static PagoBancarioResponse fromEntity(PagoBancario pago) {
        return new PagoBancarioResponse(
                pago.getId(),
                pago.getNombreCliente(),
                pago.getCodigo(),
                pago.getDescripcionPago(),
                pago.getImporteAPagar(),
                pago.getImportePagado(),
                pago.getOficina(),
                pago.getNroMovimiento(),
                pago.getFechaPago(),
                pago.getFechaProceso(),
                pago.getFormaPago(),
                pago.getCanal(),
                pago.getArchivoOrigen(),
                pago.isUsado(),
                pago.getUsadoEn(),
                pago.getCreadoEn());
    }
}
