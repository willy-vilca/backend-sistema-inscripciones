package com.universidad.inscripciones.dto.publico;

import com.universidad.inscripciones.model.entity.EscuelaProfesional;

public record EscuelaProfesionalOption(
        Long id,
        String nombre) {

    public static EscuelaProfesionalOption fromEntity(EscuelaProfesional escuela) {
        return new EscuelaProfesionalOption(escuela.getId(), escuela.getNombre());
    }
}
