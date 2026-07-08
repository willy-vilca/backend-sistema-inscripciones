package com.universidad.inscripciones.dto.publico;

import com.universidad.inscripciones.model.entity.AreaAcademica;

public record AreaAcademicaOption(
        Long id,
        String codigo,
        String nombre) {

    public static AreaAcademicaOption fromEntity(AreaAcademica area) {
        return new AreaAcademicaOption(area.getId(), area.getCodigo(), area.getNombre());
    }
}
