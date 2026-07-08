package com.universidad.inscripciones.controller;

import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.universidad.inscripciones.dto.admin.ArchivoDownload;
import com.universidad.inscripciones.dto.admin.InscripcionAdminDetailResponse;
import com.universidad.inscripciones.dto.admin.InscripcionAdminListResponse;
import com.universidad.inscripciones.dto.admin.InscripcionAdminResumenResponse;
import com.universidad.inscripciones.service.InscripcionAdminService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/postulantes")
@RequiredArgsConstructor
public class InscripcionAdminController {

    private final InscripcionAdminService inscripcionAdminService;

    @GetMapping
    public List<InscripcionAdminListResponse> listarPostulantes(
            @RequestParam(value = "buscar", required = false) String buscar) {
        return inscripcionAdminService.listar(buscar);
    }

    @GetMapping("/resumen")
    public InscripcionAdminResumenResponse obtenerResumen() {
        return inscripcionAdminService.obtenerResumen();
    }

    @GetMapping("/{id}")
    public InscripcionAdminDetailResponse obtenerDetalle(@PathVariable Long id) {
        return inscripcionAdminService.obtenerDetalle(id);
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
