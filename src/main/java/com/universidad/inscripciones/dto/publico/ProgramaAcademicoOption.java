package com.universidad.inscripciones.dto.publico;

import com.universidad.inscripciones.model.entity.ProgramaAcademico;

public record ProgramaAcademicoOption(
        Long id,
        String nombre) {

    public static ProgramaAcademicoOption fromEntity(ProgramaAcademico programa) {
        return new ProgramaAcademicoOption(programa.getId(), programa.getNombre());
    }
}
