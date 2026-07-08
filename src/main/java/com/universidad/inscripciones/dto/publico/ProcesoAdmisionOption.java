package com.universidad.inscripciones.dto.publico;

import com.universidad.inscripciones.model.entity.ProcesoAdmision;

public record ProcesoAdmisionOption(
        Long id,
        String codigo,
        String nombre) {

    public static ProcesoAdmisionOption fromEntity(ProcesoAdmision proceso) {
        return new ProcesoAdmisionOption(
                proceso.getId(),
                proceso.getCodigo(),
                proceso.getNombre());
    }
}
