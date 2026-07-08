package com.universidad.inscripciones.dto.publico;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PagoValidacionRequest(
        @NotBlank String nroMovimiento,
        @NotNull @Positive BigDecimal monto,
        LocalDate fechaPago) {
}
