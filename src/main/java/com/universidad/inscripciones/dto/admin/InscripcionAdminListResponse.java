package com.universidad.inscripciones.dto.admin;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.universidad.inscripciones.model.entity.Inscripcion;
import com.universidad.inscripciones.model.entity.Postulante;
import com.universidad.inscripciones.model.enums.EstadoInscripcion;

public record InscripcionAdminListResponse(
        Long id,
        String codigoPostulante,
        String tipoDocumento,
        String numeroDocumento,
        String nombresCompletos,
        String procesoAdmision,
        String modalidadAdmision,
        String carrera,
        String nroMovimiento,
        BigDecimal importePagado,
        LocalDateTime fechaRegistro,
        EstadoInscripcion estado,
        String observaciones,
        String carneDownloadUrl) {

    public static InscripcionAdminListResponse fromEntity(Inscripcion inscripcion) {
        Postulante postulante = inscripcion.getPostulante();
        return new InscripcionAdminListResponse(
                inscripcion.getId(),
                inscripcion.getCodigoPostulante(),
                postulante.getTipoDocumento().name(),
                postulante.getNumeroDocumento(),
                nombreCompleto(postulante),
                inscripcion.getProcesoAdmision().getNombre(),
                inscripcion.getModalidadAdmision().getNombre(),
                carrera(inscripcion),
                inscripcion.getPagoBancario().getNroMovimiento(),
                inscripcion.getPagoBancario().getImportePagado(),
                inscripcion.getFechaRegistro(),
                inscripcion.getEstado(),
                inscripcion.getObservaciones(),
                "/public/inscripcion/" + inscripcion.getId() + "/carne");
    }

    private static String nombreCompleto(Postulante postulante) {
        return String.join(" ",
                safe(postulante.getNombres()),
                safe(postulante.getApellidoPaterno()),
                safe(postulante.getApellidoMaterno())).trim();
    }

    private static String carrera(Inscripcion inscripcion) {
        if (inscripcion.getProgramaAcademico() != null) {
            return inscripcion.getProgramaAcademico().getNombre();
        }
        if (inscripcion.getEscuelaProfesional() != null) {
            return inscripcion.getEscuelaProfesional().getNombre();
        }
        return "Por definir";
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
