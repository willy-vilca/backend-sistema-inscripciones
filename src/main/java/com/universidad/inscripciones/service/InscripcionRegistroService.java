package com.universidad.inscripciones.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.universidad.inscripciones.dto.inscripcion.DocumentoRegistroRequest;
import com.universidad.inscripciones.dto.inscripcion.InscripcionRegistroRequest;
import com.universidad.inscripciones.dto.inscripcion.InscripcionRegistroResponse;
import com.universidad.inscripciones.model.entity.AreaAcademica;
import com.universidad.inscripciones.model.entity.DocumentoPostulante;
import com.universidad.inscripciones.model.entity.EscuelaProfesional;
import com.universidad.inscripciones.model.entity.Inscripcion;
import com.universidad.inscripciones.model.entity.ModalidadAdmision;
import com.universidad.inscripciones.model.entity.PagoBancario;
import com.universidad.inscripciones.model.entity.Postulante;
import com.universidad.inscripciones.model.entity.ProcesoAdmision;
import com.universidad.inscripciones.model.entity.ProgramaAcademico;
import com.universidad.inscripciones.model.enums.DocumentoEstado;
import com.universidad.inscripciones.model.enums.EstadoInscripcion;
import com.universidad.inscripciones.repository.AreaAcademicaRepository;
import com.universidad.inscripciones.repository.EscuelaProfesionalRepository;
import com.universidad.inscripciones.repository.InscripcionRepository;
import com.universidad.inscripciones.repository.ModalidadAdmisionRepository;
import com.universidad.inscripciones.repository.PagoBancarioRepository;
import com.universidad.inscripciones.repository.PostulanteRepository;
import com.universidad.inscripciones.repository.ProcesoAdmisionRepository;
import com.universidad.inscripciones.repository.ProgramaAcademicoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InscripcionRegistroService {

    private final PostulanteRepository postulanteRepository;
    private final InscripcionRepository inscripcionRepository;
    private final ProcesoAdmisionRepository procesoAdmisionRepository;
    private final ModalidadAdmisionRepository modalidadAdmisionRepository;
    private final PagoBancarioRepository pagoBancarioRepository;
    private final AreaAcademicaRepository areaAcademicaRepository;
    private final EscuelaProfesionalRepository escuelaProfesionalRepository;
    private final ProgramaAcademicoRepository programaAcademicoRepository;
    private final FileStorageService fileStorageService;

    @Transactional
    public InscripcionRegistroResponse registrar(
            InscripcionRegistroRequest request,
            MultipartFile foto,
            Map<String, MultipartFile> documentosArchivos) {

        validarSolicitud(request, foto);

        ProcesoAdmision proceso = procesoAdmisionRepository.findById(request.procesoAdmisionId())
                .orElseThrow(() -> new IllegalArgumentException("Proceso de admision no encontrado."));
        ModalidadAdmision modalidad = modalidadAdmisionRepository.findById(request.modalidadAdmisionId())
                .orElseThrow(() -> new IllegalArgumentException("Modalidad de admision no encontrada."));
        PagoBancario pago = pagoBancarioRepository.findById(request.pagoBancarioId())
                .orElseThrow(() -> new IllegalArgumentException("Pago bancario no encontrado."));

        if (pago.isUsado()) {
            throw new IllegalArgumentException("El pago seleccionado ya fue usado en otra inscripcion.");
        }

        boolean yaInscrito = inscripcionRepository
                .existsByPostulanteTipoDocumentoAndPostulanteNumeroDocumentoAndProcesoAdmisionIdAndEstadoNot(
                        request.tipoDocumento(),
                        request.numeroDocumento().trim(),
                        request.procesoAdmisionId(),
                        EstadoInscripcion.ANULADA);

        if (yaInscrito) {
            throw new IllegalArgumentException("El numero de documento ya esta inscrito en este proceso de admision.");
        }

        Postulante postulante = postulanteRepository
                .findByTipoDocumentoAndNumeroDocumento(request.tipoDocumento(), request.numeroDocumento().trim())
                .orElseGet(Postulante::new);

        actualizarPostulante(postulante, request);
        String codigoPostulante = generarCodigoPostulante(request.numeroDocumento());
        String folder = "inscripciones/" + codigoPostulante;
        postulante.setFotoPath(fileStorageService.store(foto, folder, "foto"));
        postulante = postulanteRepository.save(postulante);

        AreaAcademica area = buscarArea(request.areaAcademicaId());
        EscuelaProfesional escuela = buscarEscuela(request.escuelaProfesionalId());
        ProgramaAcademico programa = buscarPrograma(request.programaAcademicoId());

        Inscripcion inscripcion = Inscripcion.builder()
                .codigoPostulante(codigoPostulante)
                .postulante(postulante)
                .procesoAdmision(proceso)
                .modalidadAdmision(modalidad)
                .tipoColegio(request.tipoColegio())
                .pagoBancario(pago)
                .areaAcademica(area)
                .escuelaProfesional(escuela)
                .programaAcademico(programa)
                .medioDifusion(request.medioDifusion())
                .medioDifusionOtro(request.medioDifusionOtro())
                .aceptaTerminos(request.aceptaTerminos())
                .estado(EstadoInscripcion.REGISTRADA)
                .fechaRegistro(LocalDateTime.now())
                .build();

        agregarDocumentos(inscripcion, request.documentos(), documentosArchivos, folder);
        inscripcion = inscripcionRepository.save(inscripcion);

        pago.setUsado(true);
        pago.setUsadoEn(LocalDateTime.now());
        pagoBancarioRepository.save(pago);

        return new InscripcionRegistroResponse(
                inscripcion.getId(),
                codigoPostulante,
                "Inscripcion registrada correctamente.");
    }

    private void validarSolicitud(InscripcionRegistroRequest request, MultipartFile foto) {
        if (!request.aceptaTerminos()) {
            throw new IllegalArgumentException("Debes aceptar los terminos y condiciones.");
        }

        if (foto == null || foto.isEmpty()) {
            throw new IllegalArgumentException("Debes adjuntar la fotografia del postulante.");
        }
    }

    private void actualizarPostulante(Postulante postulante, InscripcionRegistroRequest request) {
        postulante.setTipoDocumento(request.tipoDocumento());
        postulante.setNumeroDocumento(request.numeroDocumento().trim());
        postulante.setNombres(request.nombres());
        postulante.setApellidoPaterno(request.apellidoPaterno());
        postulante.setApellidoMaterno(request.apellidoMaterno());
        postulante.setSexo(request.sexo());
        postulante.setFechaNacimiento(request.fechaNacimiento());
        postulante.setEdad(request.edad());
        postulante.setEstadoCivil(request.estadoCivil());
        postulante.setNumeroHijos(request.numeroHijos());
        postulante.setProcedencia(request.procedencia());
        postulante.setPaisNacimiento(request.paisNacimiento());
        postulante.setDepartamentoNacimiento(request.departamentoNacimiento());
        postulante.setProvinciaNacimiento(request.provinciaNacimiento());
        postulante.setDistritoNacimiento(request.distritoNacimiento());
        postulante.setDepartamentoDomicilio(request.departamentoDomicilio());
        postulante.setProvinciaDomicilio(request.provinciaDomicilio());
        postulante.setDistritoDomicilio(request.distritoDomicilio());
        postulante.setDireccion(request.direccion());
        postulante.setCorreoElectronico(request.correoElectronico());
        postulante.setTelefono1(request.telefono1());
        postulante.setTelefono2(request.telefono2());
        postulante.setTrabaja(request.trabaja());
        postulante.setOcupacion(request.ocupacion());
        postulante.setCondicionLaboral(request.condicionLaboral());
        postulante.setInstitucionEmpresa(request.institucionEmpresa());
        postulante.setTipoApoderado(request.tipoApoderado());
        postulante.setApoderadoNombreCompleto(request.apoderadoNombreCompleto());
        postulante.setApoderadoRelacion(request.apoderadoRelacion());
        postulante.setApoderadoOcupacion(request.apoderadoOcupacion());
        postulante.setApoderadoCentroLaboral(request.apoderadoCentroLaboral());
        postulante.setApoderadoTelefono(request.apoderadoTelefono());
        postulante.setApoderadoCorreo(request.apoderadoCorreo());
        postulante.setTipoEducacionSecundaria(request.tipoEducacionSecundaria());
        postulante.setEstudiosConcluidos(request.estudiosConcluidos());
        postulante.setColegioDepartamento(request.colegioDepartamento());
        postulante.setColegioProvincia(request.colegioProvincia());
        postulante.setInstitucionEducativa(request.institucionEducativa());
        postulante.setPeriodoEstudioInicio(request.periodoEstudioInicio());
        postulante.setPeriodoEstudioFin(request.periodoEstudioFin());
        postulante.setPresentaDiscapacidad(request.presentaDiscapacidad());
        postulante.setDiscapacidadDetalle(request.discapacidadDetalle());
        postulante.setPreparacionUniversitaria(request.preparacionUniversitaria());
    }

    private void agregarDocumentos(
            Inscripcion inscripcion,
            List<DocumentoRegistroRequest> documentos,
            Map<String, MultipartFile> documentosArchivos,
            String folder) {

        if (documentos == null) {
            return;
        }

        for (DocumentoRegistroRequest documento : documentos) {
            MultipartFile file = documentosArchivos.get(documento.clave());
            if (documento.obligatorio() && (file == null || file.isEmpty())) {
                throw new IllegalArgumentException("Falta adjuntar el documento: " + documento.tipoDocumento());
            }

            if (file == null || file.isEmpty()) {
                continue;
            }

            String path = fileStorageService.store(file, folder, documento.clave());
            DocumentoPostulante adjunto = DocumentoPostulante.builder()
                    .inscripcion(inscripcion)
                    .tipoDocumento(documento.tipoDocumento())
                    .nombreOriginal(file.getOriginalFilename())
                    .rutaArchivo(path)
                    .contentType(file.getContentType())
                    .tamanioBytes(file.getSize())
                    .obligatorio(documento.obligatorio())
                    .estado(DocumentoEstado.CARGADO)
                    .build();
            inscripcion.getDocumentos().add(adjunto);
        }
    }

    private AreaAcademica buscarArea(Long id) {
        return id == null ? null : areaAcademicaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Area academica no encontrada."));
    }

    private EscuelaProfesional buscarEscuela(Long id) {
        return id == null ? null : escuelaProfesionalRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Escuela profesional no encontrada."));
    }

    private ProgramaAcademico buscarPrograma(Long id) {
        return id == null ? null : programaAcademicoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Programa academico no encontrado."));
    }

    private String generarCodigoPostulante(String numeroDocumento) {
        String cleanDocument = numeroDocumento.replaceAll("\\D", "");
        String suffix = String.valueOf(System.currentTimeMillis()).substring(7);
        return (cleanDocument.isBlank() ? "POST" : cleanDocument) + suffix;
    }
}
