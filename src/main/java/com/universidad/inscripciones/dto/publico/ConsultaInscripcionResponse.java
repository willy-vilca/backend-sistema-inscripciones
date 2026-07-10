package com.universidad.inscripciones.dto.publico;

import java.time.LocalDateTime;

import com.universidad.inscripciones.model.entity.Inscripcion;

public record ConsultaInscripcionResponse(
        Long inscripcionId,
        String codigoPostulante,
        String tipoDocumento,
        String numeroDocumento,
        String nombresCompletos,
        String procesoAdmision,
        String modalidadAdmision,
        String tipoColegio,
        String areaAcademica,
        String escuelaProfesional,
        String programaAcademico,
        LocalDateTime fechaRegistro,
        String estado,
        String observaciones,
        String nroMovimiento,
        String carneDownloadUrl) {

    public static ConsultaInscripcionResponse fromEntity(Inscripcion inscripcion) {
        return new ConsultaInscripcionResponse(
                inscripcion.getId(),
                inscripcion.getCodigoPostulante(),
                inscripcion.getPostulante().getTipoDocumento().name(),
                inscripcion.getPostulante().getNumeroDocumento(),
                nombresCompletos(inscripcion),
                inscripcion.getProcesoAdmision().getNombre(),
                inscripcion.getModalidadAdmision().getNombre(),
                inscripcion.getTipoColegio().name().replace("_", " "),
                inscripcion.getAreaAcademica() == null ? null : inscripcion.getAreaAcademica().getNombre(),
                inscripcion.getEscuelaProfesional() == null ? null : inscripcion.getEscuelaProfesional().getNombre(),
                inscripcion.getProgramaAcademico() == null ? null : inscripcion.getProgramaAcademico().getNombre(),
                inscripcion.getFechaRegistro(),
                inscripcion.getEstado().name(),
                inscripcion.getObservaciones(),
                inscripcion.getPagoBancario().getNroMovimiento(),
                "/public/inscripcion/" + inscripcion.getId() + "/carne");
    }

    private static String nombresCompletos(Inscripcion inscripcion) {
        return String.join(" ",
                inscripcion.getPostulante().getNombres(),
                inscripcion.getPostulante().getApellidoPaterno(),
                inscripcion.getPostulante().getApellidoMaterno() == null
                        ? ""
                        : inscripcion.getPostulante().getApellidoMaterno()).trim();
    }
}
