package com.universidad.inscripciones.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.universidad.inscripciones.dto.admin.ArchivoDownload;
import com.universidad.inscripciones.dto.admin.AnularInscripcionRequest;
import com.universidad.inscripciones.dto.admin.InscripcionAdminDetailResponse;
import com.universidad.inscripciones.dto.admin.InscripcionAdminListResponse;
import com.universidad.inscripciones.dto.admin.InscripcionAdminResumenResponse;
import com.universidad.inscripciones.model.entity.DocumentoPostulante;
import com.universidad.inscripciones.model.entity.Inscripcion;
import com.universidad.inscripciones.model.entity.PagoBancario;
import com.universidad.inscripciones.model.enums.DocumentoEstado;
import com.universidad.inscripciones.model.enums.EstadoInscripcion;
import com.universidad.inscripciones.repository.DocumentoPostulanteRepository;
import com.universidad.inscripciones.repository.InscripcionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InscripcionAdminService {

    private static final String HUELLA_DIGITAL_TIPO = "Huella digital del postulante";
    private static final String HUELLA_DIGITAL_PREFIX = "huella_digital";

    private final InscripcionRepository inscripcionRepository;
    private final DocumentoPostulanteRepository documentoPostulanteRepository;
    private final FileStorageService fileStorageService;

    @Value("${app.upload-dir}")
    private String uploadDir;

    @Transactional(readOnly = true)
    public List<InscripcionAdminListResponse> listar(String buscar) {
        return listar(buscar, "TODOS", 0);
    }

    @Transactional(readOnly = true)
    public List<InscripcionAdminListResponse> listar(String buscar, String estado, int bloque) {
        String filtro = buscar == null || buscar.isBlank() ? null : buscar.trim();
        EstadoInscripcion estadoFiltro = obtenerEstadoFiltro(estado);
        PageRequest pagina = PageRequest.of(Math.max(bloque, 0), 100);
        List<Inscripcion> inscripciones = obtenerInscripcionesAdmin(filtro, estadoFiltro, pagina);

        return inscripciones
                .stream()
                .map(InscripcionAdminListResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public InscripcionAdminResumenResponse obtenerResumen() {
        LocalDate hoy = LocalDate.now();
        LocalDateTime inicio = hoy.atStartOfDay();
        LocalDateTime fin = hoy.atTime(LocalTime.MAX);

        return new InscripcionAdminResumenResponse(
                inscripcionRepository.count(),
                inscripcionRepository.countByEstado(EstadoInscripcion.REGISTRADA),
                inscripcionRepository.countByEstado(EstadoInscripcion.APROBADA),
                inscripcionRepository.countByEstado(EstadoInscripcion.ANULADA),
                inscripcionRepository.countByFechaRegistroBetween(inicio, fin));
    }

    @Transactional(readOnly = true)
    public InscripcionAdminDetailResponse obtenerDetalle(Long id) {
        Inscripcion inscripcion = inscripcionRepository.buscarDetalleAdmin(id)
                .orElseThrow(() -> new IllegalArgumentException("Inscripcion no encontrada."));
        return InscripcionAdminDetailResponse.fromEntity(inscripcion);
    }

    @Transactional
    public InscripcionAdminDetailResponse aprobar(Long id) {
        Inscripcion inscripcion = inscripcionRepository.buscarDetalleAdmin(id)
                .orElseThrow(() -> new IllegalArgumentException("Inscripcion no encontrada."));

        if (inscripcion.getEstado() == EstadoInscripcion.ANULADA) {
            throw new IllegalArgumentException("No se puede aprobar una inscripcion anulada.");
        }

        if (!documentoPostulanteRepository.existsByInscripcionIdAndTipoDocumento(id, HUELLA_DIGITAL_TIPO)) {
            throw new IllegalArgumentException("Primero debes registrar la huella digital del postulante para poder aprobar la inscripcion.");
        }

        inscripcion.setEstado(EstadoInscripcion.APROBADA);
        inscripcion.setObservaciones(null);
        return InscripcionAdminDetailResponse.fromEntity(inscripcion);
    }

    @Transactional
    public InscripcionAdminDetailResponse anular(Long id, AnularInscripcionRequest request) {
        String motivo = request.motivo() == null ? "" : request.motivo().trim();
        if (motivo.isBlank()) {
            throw new IllegalArgumentException("El motivo de anulacion es obligatorio.");
        }

        Inscripcion inscripcion = inscripcionRepository.buscarDetalleAdmin(id)
                .orElseThrow(() -> new IllegalArgumentException("Inscripcion no encontrada."));

        if (inscripcion.getEstado() == EstadoInscripcion.ANULADA) {
            throw new IllegalArgumentException("La inscripcion ya se encuentra anulada.");
        }

        inscripcion.setEstado(EstadoInscripcion.ANULADA);
        inscripcion.setObservaciones(motivo);

        PagoBancario pago = inscripcion.getPagoBancario();
        pago.setUsado(false);
        pago.setUsadoEn(null);

        return InscripcionAdminDetailResponse.fromEntity(inscripcion);
    }

    @Transactional(readOnly = true)
    public ArchivoDownload obtenerFoto(Long inscripcionId) {
        Inscripcion inscripcion = inscripcionRepository.buscarDetalleAdmin(inscripcionId)
                .orElseThrow(() -> new IllegalArgumentException("Inscripcion no encontrada."));
        String fotoPath = inscripcion.getPostulante().getFotoPath();

        if (fotoPath == null || fotoPath.isBlank()) {
            throw new IllegalArgumentException("El postulante no tiene fotografia registrada.");
        }

        String extension = extension(fotoPath);
        return cargarArchivo(
                fotoPath,
                "foto-" + inscripcion.getCodigoPostulante() + extension,
                detectarContentType(fotoPath, "image/jpeg"));
    }

    @Transactional
    public InscripcionAdminDetailResponse registrarHuellaDigital(Long inscripcionId, MultipartFile archivo) {
        Inscripcion inscripcion = inscripcionRepository.buscarDetalleAdmin(inscripcionId)
                .orElseThrow(() -> new IllegalArgumentException("Inscripcion no encontrada."));

        if (inscripcion.getEstado() == EstadoInscripcion.ANULADA) {
            throw new IllegalArgumentException("No se puede registrar huella digital en una inscripcion anulada.");
        }

        if (documentoPostulanteRepository.existsByInscripcionIdAndTipoDocumento(inscripcionId, HUELLA_DIGITAL_TIPO)) {
            throw new IllegalArgumentException("La huella digital de esta inscripcion ya fue registrada.");
        }

        validarArchivoHuella(archivo);

        String folder = "inscripciones/" + inscripcion.getCodigoPostulante();
        String path = fileStorageService.store(archivo, folder, HUELLA_DIGITAL_PREFIX);
        DocumentoPostulante huella = DocumentoPostulante.builder()
                .inscripcion(inscripcion)
                .tipoDocumento(HUELLA_DIGITAL_TIPO)
                .nombreOriginal(archivo.getOriginalFilename())
                .rutaArchivo(path)
                .contentType(archivo.getContentType())
                .tamanioBytes(archivo.getSize())
                .obligatorio(false)
                .estado(DocumentoEstado.CARGADO)
                .build();

        documentoPostulanteRepository.save(huella);
        inscripcion.getDocumentos().add(huella);
        return InscripcionAdminDetailResponse.fromEntity(inscripcion);
    }

    private EstadoInscripcion obtenerEstadoFiltro(String estado) {
        if (estado == null || estado.isBlank() || "TODOS".equalsIgnoreCase(estado)) {
            return null;
        }

        try {
            return EstadoInscripcion.valueOf(estado.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Estado de inscripcion no valido.");
        }
    }

    private List<Inscripcion> obtenerInscripcionesAdmin(
            String filtro,
            EstadoInscripcion estado,
            PageRequest pagina) {
        if (filtro == null && estado == null) {
            return inscripcionRepository.listarRecientesParaAdmin(pagina);
        }

        if (filtro == null) {
            return inscripcionRepository.listarRecientesParaAdminPorEstado(estado, pagina);
        }

        if (estado == null) {
            return inscripcionRepository.buscarParaAdmin(filtro, pagina);
        }

        return inscripcionRepository.buscarParaAdminPorEstado(filtro, estado, pagina);
    }

    @Transactional(readOnly = true)
    public ArchivoDownload obtenerDocumento(Long inscripcionId, Long documentoId) {
        DocumentoPostulante documento = documentoPostulanteRepository
                .findByIdAndInscripcionId(documentoId, inscripcionId)
                .orElseThrow(() -> new IllegalArgumentException("Documento no encontrado."));

        return cargarArchivo(
                documento.getRutaArchivo(),
                documento.getNombreOriginal(),
                documento.getContentType());
    }

    private void validarArchivoHuella(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new IllegalArgumentException("Debes seleccionar una imagen de la huella digital.");
        }

        String contentType = archivo.getContentType();
        if (contentType != null && contentType.toLowerCase().startsWith("image/")) {
            return;
        }

        String nombre = archivo.getOriginalFilename() == null ? "" : archivo.getOriginalFilename().toLowerCase();
        if (nombre.endsWith(".jpg") || nombre.endsWith(".jpeg") || nombre.endsWith(".png") || nombre.endsWith(".webp")) {
            return;
        }

        throw new IllegalArgumentException("La huella digital debe ser un archivo de imagen.");
    }

    private ArchivoDownload cargarArchivo(String relativePath, String filename, String contentType) {
        try {
            Path file = resolveUploadPath(relativePath);
            if (!Files.exists(file) || !file.startsWith(uploadRoot())) {
                throw new IllegalArgumentException("No se encontro el archivo solicitado.");
            }

            Resource resource = new UrlResource(file.toUri());
            return new ArchivoDownload(
                    filename == null || filename.isBlank() ? file.getFileName().toString() : filename,
                    contentType == null || contentType.isBlank() ? "application/octet-stream" : contentType,
                    resource);
        } catch (IOException ex) {
            throw new IllegalArgumentException("No se pudo abrir el archivo solicitado.", ex);
        }
    }

    private Path resolveUploadPath(String relativePath) {
        return uploadRoot().resolve(relativePath).normalize();
    }

    private Path uploadRoot() {
        return Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    private String extension(String path) {
        int dotIndex = path.lastIndexOf('.');
        return dotIndex >= 0 ? path.substring(dotIndex) : ".jpg";
    }

    private String detectarContentType(String relativePath, String fallback) {
        try {
            String contentType = Files.probeContentType(resolveUploadPath(relativePath));
            return contentType == null || contentType.isBlank() ? fallback : contentType;
        } catch (IOException ex) {
            return fallback;
        }
    }
}
