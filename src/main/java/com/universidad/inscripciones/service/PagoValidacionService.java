package com.universidad.inscripciones.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.universidad.inscripciones.dto.publico.PagoValidacionRequest;
import com.universidad.inscripciones.dto.publico.PagoValidacionResponse;
import com.universidad.inscripciones.model.entity.PagoBancario;
import com.universidad.inscripciones.repository.PagoBancarioRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PagoValidacionService {

    private final PagoBancarioRepository pagoBancarioRepository;

    @Transactional(readOnly = true)
    public PagoValidacionResponse validarPago(PagoValidacionRequest request) {
        String nroMovimiento = request.nroMovimiento().trim();
        PagoBancario pago = pagoBancarioRepository.findByNroMovimiento(nroMovimiento)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No se encontro un pago registrado con el numero de movimiento ingresado."));

        if (pago.isUsado()) {
            throw new IllegalArgumentException(
                    "Este numero de movimiento ya fue usado en una inscripcion.");
        }

        if (pago.getImportePagado() != null && request.monto() != null
                && pago.getImportePagado().compareTo(request.monto()) != 0) {
            throw new IllegalArgumentException(
                    "El monto ingresado no coincide con el importe pagado registrado por el banco.");
        }

        return PagoValidacionResponse.fromEntity(pago);
    }
}
