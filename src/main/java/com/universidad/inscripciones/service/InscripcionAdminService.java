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

import com.universidad.inscripciones.dto.admin.ArchivoDownload;
import com.universidad.inscripciones.dto.admin.AnularInscripcionRequest;
import com.universidad.inscripciones.dto.admin.InscripcionAdminDetailResponse;
import com.universidad.inscripciones.dto.admin.InscripcionAdminListResponse;
import com.universidad.inscripciones.dto.admin.InscripcionAdminResumenResponse;
import com.universidad.inscripciones.model.entity.DocumentoPostulante;
import com.universidad.inscripciones.model.entity.Inscripcion;
import com.universidad.inscripciones.model.entity.PagoBancario;
import com.universidad.inscripciones.model.enums.EstadoInscripcion;
import com.universidad.inscripciones.repository.DocumentoPostulanteRepository;
import com.universidad.inscripciones.repository.InscripcionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InscripcionAdminService {

    private final InscripcionRepository inscripcionRepository;
    private final DocumentoPostulanteRepository documentoPostulanteRepository;

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
