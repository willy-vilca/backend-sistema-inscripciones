package com.universidad.inscripciones.dto.admin;

import com.universidad.inscripciones.model.entity.EscuelaProfesional;

public record CarreraProfesionalAdminResponse(
        Long id,
        String nombre,
        boolean activo,
        Long areaAcademicaId,
        String areaAcademicaCodigo,
        String areaAcademicaNombre,
        Long programaAcademicoId,
        long inscripciones) {

    public static CarreraProfesionalAdminResponse fromEntity(
            EscuelaProfesional carrera,
            Long programaAcademicoId,
            long inscripciones) {
        return new CarreraProfesionalAdminResponse(
                carrera.getId(),
                carrera.getNombre(),
                carrera.isActivo(),
                carrera.getAreaAcademica().getId(),
                carrera.getAreaAcademica().getCodigo(),
                carrera.getAreaAcademica().getNombre(),
                programaAcademicoId,
                inscripciones);
    }
}
