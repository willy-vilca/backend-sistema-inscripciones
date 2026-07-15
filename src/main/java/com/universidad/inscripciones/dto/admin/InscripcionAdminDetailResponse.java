package com.universidad.inscripciones.dto.admin;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.universidad.inscripciones.model.entity.DocumentoPostulante;
import com.universidad.inscripciones.model.entity.Inscripcion;
import com.universidad.inscripciones.model.entity.PagoBancario;
import com.universidad.inscripciones.model.entity.Postulante;

public record InscripcionAdminDetailResponse(
        Long id,
        String codigoPostulante,
        String estado,
        String observaciones,
        LocalDateTime fechaRegistro,
        String fotoUrl,
        String carneDownloadUrl,
        DatosPersonales datosPersonales,
        LugarNacimiento lugarNacimiento,
        DireccionDomiciliaria direccionDomiciliaria,
        Contacto contacto,
        Ocupacion ocupacion,
        Apoderado apoderado,
        EducacionSecundaria educacionSecundaria,
        InformacionAdicional informacionAdicional,
        DatosPostulacion datosPostulacion,
        PagoDetalle pago,
        DocumentoDetalle huellaDigital,
        List<DocumentoDetalle> documentos) {

    private static final String HUELLA_DIGITAL_TIPO = "Huella digital del postulante";

    public static InscripcionAdminDetailResponse fromEntity(Inscripcion inscripcion) {
        Postulante postulante = inscripcion.getPostulante();
        DocumentoDetalle huellaDigital = inscripcion.getDocumentos().stream()
                .filter(documento -> HUELLA_DIGITAL_TIPO.equals(documento.getTipoDocumento()))
                .findFirst()
                .map(documento -> documento(documento, inscripcion.getId()))
                .orElse(null);

        return new InscripcionAdminDetailResponse(
                inscripcion.getId(),
                inscripcion.getCodigoPostulante(),
                inscripcion.getEstado().name(),
                inscripcion.getObservaciones(),
                inscripcion.getFechaRegistro(),
                "/admin/postulantes/" + inscripcion.getId() + "/foto",
                "/public/inscripcion/" + inscripcion.getId() + "/carne",
                datosPersonales(postulante),
                lugarNacimiento(postulante),
                direccionDomiciliaria(postulante),
                contacto(postulante),
                ocupacion(postulante),
                apoderado(postulante),
                educacionSecundaria(postulante),
                informacionAdicional(postulante),
                datosPostulacion(inscripcion),
                pago(inscripcion.getPagoBancario()),
                huellaDigital,
                inscripcion.getDocumentos().stream()
                        .filter(documento -> !HUELLA_DIGITAL_TIPO.equals(documento.getTipoDocumento()))
                        .map(documento -> documento(documento, inscripcion.getId()))
                        .toList());
    }

    private static DatosPersonales datosPersonales(Postulante postulante) {
        return new DatosPersonales(
                postulante.getNombres(),
                postulante.getApellidoPaterno(),
                postulante.getApellidoMaterno(),
                postulante.getTipoDocumento().name(),
                postulante.getNumeroDocumento(),
                postulante.getSexo().name(),
                postulante.getFechaNacimiento(),
                postulante.getEdad(),
                postulante.getEstadoCivil() == null ? null : postulante.getEstadoCivil().name(),
                postulante.getNumeroHijos(),
                postulante.getProcedencia());
    }

    private static LugarNacimiento lugarNacimiento(Postulante postulante) {
        return new LugarNacimiento(
                postulante.getPaisNacimiento(),
                postulante.getDepartamentoNacimiento(),
                postulante.getProvinciaNacimiento(),
                postulante.getDistritoNacimiento());
    }

    private static DireccionDomiciliaria direccionDomiciliaria(Postulante postulante) {
        return new DireccionDomiciliaria(
                postulante.getDepartamentoDomicilio(),
                postulante.getProvinciaDomicilio(),
                postulante.getDistritoDomicilio(),
                postulante.getDireccion());
    }

    private static Contacto contacto(Postulante postulante) {
        return new Contacto(
                postulante.getCorreoElectronico(),
                postulante.getTelefono1(),
                postulante.getTelefono2());
    }

    private static Ocupacion ocupacion(Postulante postulante) {
        return new Ocupacion(
                postulante.isTrabaja(),
                postulante.getOcupacion(),
                postulante.getCondicionLaboral(),
                postulante.getInstitucionEmpresa());
    }

    private static Apoderado apoderado(Postulante postulante) {
        return new Apoderado(
                postulante.getTipoApoderado() == null ? null : postulante.getTipoApoderado().name(),
                postulante.getApoderadoNombreCompleto(),
                postulante.getApoderadoRelacion(),
                postulante.getApoderadoOcupacion(),
                postulante.getApoderadoCentroLaboral(),
                postulante.getApoderadoTelefono(),
                postulante.getApoderadoCorreo());
    }

    private static EducacionSecundaria educacionSecundaria(Postulante postulante) {
        return new EducacionSecundaria(
                postulante.getTipoEducacionSecundaria() == null ? null : postulante.getTipoEducacionSecundaria().name(),
                postulante.getEstudiosConcluidos() == null ? null : postulante.getEstudiosConcluidos().name(),
                postulante.getColegioDepartamento(),
                postulante.getColegioProvincia(),
                postulante.getInstitucionEducativa(),
                postulante.getPeriodoEstudioInicio(),
                postulante.getPeriodoEstudioFin());
    }

    private static InformacionAdicional informacionAdicional(Postulante postulante) {
        return new InformacionAdicional(
                postulante.isPresentaDiscapacidad(),
                postulante.getDiscapacidadDetalle(),
                postulante.getPreparacionUniversitaria());
    }

    private static DatosPostulacion datosPostulacion(Inscripcion inscripcion) {
        return new DatosPostulacion(
                inscripcion.getProcesoAdmision().getNombre(),
                inscripcion.getModalidadAdmision().getNombre(),
                inscripcion.getTipoColegio().name(),
                inscripcion.getAreaAcademica() == null ? null : inscripcion.getAreaAcademica().getNombre(),
                inscripcion.getEscuelaProfesional() == null ? null : inscripcion.getEscuelaProfesional().getNombre(),
                inscripcion.getProgramaAcademico() == null ? null : inscripcion.getProgramaAcademico().getNombre(),
                inscripcion.getMedioDifusion() == null ? null : inscripcion.getMedioDifusion().name(),
                inscripcion.getMedioDifusionOtro(),
                inscripcion.isAceptaTerminos());
    }

    private static PagoDetalle pago(PagoBancario pago) {
        return new PagoDetalle(
                pago.getId(),
                pago.getNroMovimiento(),
                pago.getNombreCliente(),
                pago.getCodigo(),
                pago.getDescripcionPago(),
                pago.getImporteAPagar(),
                pago.getImportePagado(),
                pago.getOficina(),
                pago.getFechaPago(),
                pago.getFechaProceso(),
                pago.getFormaPago(),
                pago.getCanal(),
                pago.getArchivoOrigen(),
                pago.isUsado(),
                pago.getUsadoEn());
    }

    private static DocumentoDetalle documento(DocumentoPostulante documento, Long inscripcionId) {
        return new DocumentoDetalle(
                documento.getId(),
                documento.getTipoDocumento(),
                documento.getNombreOriginal(),
                documento.getContentType(),
                documento.getTamanioBytes(),
                documento.isObligatorio(),
                documento.getEstado().name(),
                "/admin/postulantes/" + inscripcionId + "/documentos/" + documento.getId());
    }

    public record DatosPersonales(
            String nombres,
            String apellidoPaterno,
            String apellidoMaterno,
            String tipoDocumento,
            String numeroDocumento,
            String sexo,
            LocalDate fechaNacimiento,
            Integer edad,
            String estadoCivil,
            Integer numeroHijos,
            String procedencia) {
    }

    public record LugarNacimiento(
            String pais,
            String departamento,
            String provincia,
            String distrito) {
    }

    public record DireccionDomiciliaria(
            String departamento,
            String provincia,
            String distrito,
            String direccion) {
    }

    public record Contacto(
            String correoElectronico,
            String telefono1,
            String telefono2) {
    }

    public record Ocupacion(
            boolean trabaja,
            String ocupacion,
            String condicionLaboral,
            String institucionEmpresa) {
    }

    public record Apoderado(
            String tipoApoderado,
            String nombreCompleto,
            String relacion,
            String ocupacion,
            String centroLaboral,
            String telefono,
            String correoElectronico) {
    }

    public record EducacionSecundaria(
            String tipoEducacion,
            String estudiosConcluidos,
            String departamento,
            String provincia,
            String institucionEducativa,
            Integer periodoInicio,
            Integer periodoFin) {
    }

    public record InformacionAdicional(
            boolean presentaDiscapacidad,
            String discapacidadDetalle,
            String preparacionUniversitaria) {
    }

    public record DatosPostulacion(
            String procesoAdmision,
            String modalidadAdmision,
            String tipoColegio,
            String areaAcademica,
            String escuelaProfesional,
            String programaAcademico,
            String medioDifusion,
            String medioDifusionOtro,
            boolean aceptaTerminos) {
    }

    public record PagoDetalle(
            Long id,
            String nroMovimiento,
            String nombreCliente,
            String codigo,
            String descripcionPago,
            BigDecimal importeAPagar,
            BigDecimal importePagado,
            String oficina,
            LocalDateTime fechaPago,
            LocalDate fechaProceso,
            String formaPago,
            String canal,
            String archivoOrigen,
            boolean usado,
            LocalDateTime usadoEn) {
    }

    public record DocumentoDetalle(
            Long id,
            String tipoDocumento,
            String nombreOriginal,
            String contentType,
            Long tamanioBytes,
            boolean obligatorio,
            String estado,
            String downloadUrl) {
    }
}
