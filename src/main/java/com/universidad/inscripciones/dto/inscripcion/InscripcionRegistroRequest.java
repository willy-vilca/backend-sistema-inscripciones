package com.universidad.inscripciones.dto.inscripcion;

import java.time.LocalDate;
import java.util.List;

import com.universidad.inscripciones.model.enums.EstadoCivil;
import com.universidad.inscripciones.model.enums.EstudiosConcluidos;
import com.universidad.inscripciones.model.enums.MedioDifusion;
import com.universidad.inscripciones.model.enums.Sexo;
import com.universidad.inscripciones.model.enums.TipoApoderado;
import com.universidad.inscripciones.model.enums.TipoColegio;
import com.universidad.inscripciones.model.enums.TipoDocumento;
import com.universidad.inscripciones.model.enums.TipoEducacion;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record InscripcionRegistroRequest(
        @NotNull TipoDocumento tipoDocumento,
        @NotBlank String numeroDocumento,
        @NotNull Long procesoAdmisionId,
        @NotNull Long modalidadAdmisionId,
        @NotNull TipoColegio tipoColegio,
        @NotNull Long pagoBancarioId,

        @NotBlank String nombres,
        @NotBlank String apellidoPaterno,
        String apellidoMaterno,
        @NotNull Sexo sexo,
        LocalDate fechaNacimiento,
        Integer edad,
        EstadoCivil estadoCivil,
        Integer numeroHijos,
        String procedencia,

        String paisNacimiento,
        String departamentoNacimiento,
        String provinciaNacimiento,
        String distritoNacimiento,

        String departamentoDomicilio,
        String provinciaDomicilio,
        String distritoDomicilio,
        String direccion,

        String correoElectronico,
        String telefono1,
        String telefono2,

        boolean trabaja,
        String ocupacion,
        String condicionLaboral,
        String institucionEmpresa,

        TipoApoderado tipoApoderado,
        String apoderadoNombreCompleto,
        String apoderadoRelacion,
        String apoderadoOcupacion,
        String apoderadoCentroLaboral,
        String apoderadoTelefono,
        String apoderadoCorreo,

        TipoEducacion tipoEducacionSecundaria,
        EstudiosConcluidos estudiosConcluidos,
        String colegioDepartamento,
        String colegioProvincia,
        String institucionEducativa,
        Integer periodoEstudioInicio,
        Integer periodoEstudioFin,

        boolean presentaDiscapacidad,
        String discapacidadDetalle,
        String preparacionUniversitaria,

        MedioDifusion medioDifusion,
        String medioDifusionOtro,
        Long areaAcademicaId,
        Long escuelaProfesionalId,
        Long programaAcademicoId,

        boolean aceptaTerminos,
        List<DocumentoRegistroRequest> documentos) {
}
