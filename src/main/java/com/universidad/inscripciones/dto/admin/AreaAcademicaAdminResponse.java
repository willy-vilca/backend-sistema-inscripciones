package com.universidad.inscripciones.dto.admin;

import com.universidad.inscripciones.model.entity.AreaAcademica;

public record AreaAcademicaAdminResponse(
        Long id,
        String codigo,
        String nombre,
        boolean activo,
        long carreras,
        long inscripciones) {

    public static AreaAcademicaAdminResponse fromEntity(AreaAcademica area, long carreras, long inscripciones) {
        return new AreaAcademicaAdminResponse(
                area.getId(),
                area.getCodigo(),
                area.getNombre(),
                area.isActivo(),
                carreras,
                inscripciones);
    }
}
