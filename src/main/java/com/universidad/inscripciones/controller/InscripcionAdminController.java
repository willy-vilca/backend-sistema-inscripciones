package com.universidad.inscripciones.controller;

import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.universidad.inscripciones.dto.admin.ArchivoDownload;
import com.universidad.inscripciones.dto.admin.AnularInscripcionRequest;
import com.universidad.inscripciones.dto.admin.InscripcionAdminDetailResponse;
import com.universidad.inscripciones.dto.admin.InscripcionAdminListResponse;
import com.universidad.inscripciones.dto.admin.InscripcionAdminResumenResponse;
import com.universidad.inscripciones.service.InscripcionAdminService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/postulantes")
@RequiredArgsConstructor
public class InscripcionAdminController {

    private final InscripcionAdminService inscripcionAdminService;

    @GetMapping
    public List<InscripcionAdminListResponse> listarPostulantes(
            @RequestParam(value = "buscar", required = false) String buscar,
            @RequestParam(value = "estado", required = false, defaultValue = "TODOS") String estado,
            @RequestParam(value = "bloque", required = false, defaultValue = "0") int bloque) {
        return inscripcionAdminService.listar(buscar, estado, bloque);
    }

    @GetMapping("/resumen")
    public InscripcionAdminResumenResponse obtenerResumen() {
        return inscripcionAdminService.obtenerResumen();
    }

    @GetMapping("/{id}")
    public InscripcionAdminDetailResponse obtenerDetalle(@PathVariable Long id) {
        return inscripcionAdminService.obtenerDetalle(id);
    }

    @PatchMapping("/{id}/aprobar")
    public InscripcionAdminDetailResponse aprobar(@PathVariable Long id) {
        return inscripcionAdminService.aprobar(id);
    }

    @PatchMapping("/{id}/anular")
    public InscripcionAdminDetailResponse anular(
            @PathVariable Long id,
            @Valid @RequestBody AnularInscripcionRequest request) {
        return inscripcionAdminService.anular(id, request);
    }

    @GetMapping("/{id}/foto")
    public ResponseEntity<Resource> descargarFoto(@PathVariable Long id) {
        return descargar(inscripcionAdminService.obtenerFoto(id), false);
    }

    @GetMapping("/{id}/documentos/{documentoId}")
    public ResponseEntity<Resource> descargarDocumento(
            @PathVariable Long id,
            @PathVariable Long documentoId) {
        return descargar(inscripcionAdminService.obtenerDocumento(id, documentoId), true);
    }

    private ResponseEntity<Resource> descargar(ArchivoDownload archivo, boolean attachment) {
        ContentDisposition.Builder disposition = attachment
                ? ContentDisposition.attachment()
                : ContentDisposition.inline();

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(archivo.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition
                        .filename(archivo.filename())
                        .build()
                        .toString())
                .body(archivo.resource());
    }
}
