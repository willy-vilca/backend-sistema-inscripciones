package com.universidad.inscripciones.dto.publico;

import java.math.BigDecimal;

import com.universidad.inscripciones.model.entity.ModalidadAdmision;

public record ModalidadAdmisionOption(
        Long id,
        String nombre,
        BigDecimal montoBase) {

    public static ModalidadAdmisionOption fromEntity(ModalidadAdmision modalidad) {
        return new ModalidadAdmisionOption(
                modalidad.getId(),
                modalidad.getNombre(),
                modalidad.getMontoBase());
    }
}
