package com.universidad.inscripciones.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.universidad.inscripciones.dto.inscripcion.CarnePdfDownload;
import com.universidad.inscripciones.dto.inscripcion.InscripcionRegistroRequest;
import com.universidad.inscripciones.dto.inscripcion.InscripcionRegistroResponse;
import com.universidad.inscripciones.dto.publico.CatalogoConsultaInscripcionResponse;
import com.universidad.inscripciones.dto.publico.CatalogoInicioInscripcionResponse;
import com.universidad.inscripciones.dto.publico.ConsultaInscripcionRequest;
import com.universidad.inscripciones.dto.publico.ConsultaInscripcionResponse;
import com.universidad.inscripciones.dto.publico.DocumentoDisponibilidadRequest;
import com.universidad.inscripciones.dto.publico.DocumentoDisponibilidadResponse;
import com.universidad.inscripciones.dto.publico.PagoValidacionRequest;
import com.universidad.inscripciones.dto.publico.PagoValidacionResponse;
import com.universidad.inscripciones.service.CarnePdfService;
import com.universidad.inscripciones.service.InscripcionRegistroService;
import com.universidad.inscripciones.service.InicioInscripcionService;
import com.universidad.inscripciones.service.PagoValidacionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/public/inscripcion")
@RequiredArgsConstructor
public class InicioInscripcionController {

    private final InicioInscripcionService inicioInscripcionService;
    private final PagoValidacionService pagoValidacionService;
    private final InscripcionRegistroService inscripcionRegistroService;
    private final CarnePdfService carnePdfService;
    private final ObjectMapper objectMapper;

    @GetMapping("/catalogos")
    public CatalogoInicioInscripcionResponse obtenerCatalogos() {
        return inicioInscripcionService.obtenerCatalogos();
    }

    @GetMapping("/catalogos-consulta")
    public CatalogoConsultaInscripcionResponse obtenerCatalogosConsulta() {
        return inicioInscripcionService.obtenerCatalogosConsulta();
    }

    @PostMapping("/verificar-documento")
    public DocumentoDisponibilidadResponse verificarDocumento(
            @Valid @RequestBody DocumentoDisponibilidadRequest request) {
        return inicioInscripcionService.verificarDocumento(request);
    }

    @PostMapping("/consultar")
    public ConsultaInscripcionResponse consultarInscripcion(
            @Valid @RequestBody ConsultaInscripcionRequest request) {
        return inicioInscripcionService.consultarInscripcion(request);
    }

    @PostMapping("/validar-pago")
    public PagoValidacionResponse validarPago(@Valid @RequestBody PagoValidacionRequest request) {
        return pagoValidacionService.validarPago(request);
    }

    @PostMapping(value = "/registrar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public InscripcionRegistroResponse registrarInscripcion(
            @RequestPart("datos") String datosJson,
            @RequestPart("foto") MultipartFile foto,
            @RequestPart(value = "documentos", required = false) List<MultipartFile> documentos,
            @RequestParam(value = "documentoKeys", required = false) List<String> documentoKeys) throws JsonProcessingException {

        InscripcionRegistroRequest request = objectMapper.readValue(datosJson, InscripcionRegistroRequest.class);
        Map<String, MultipartFile> documentosPorClave = new HashMap<>();

        if (documentos != null && documentoKeys != null) {
            for (int i = 0; i < documentos.size() && i < documentoKeys.size(); i++) {
                documentosPorClave.put(documentoKeys.get(i), documentos.get(i));
            }
        }

        return inscripcionRegistroService.registrar(request, foto, documentosPorClave);
    }

    @GetMapping("/{inscripcionId}/carne")
    public ResponseEntity<Resource> descargarCarne(@PathVariable Long inscripcionId) {
        CarnePdfDownload download = carnePdfService.obtenerCarne(inscripcionId);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(download.filename())
                        .build()
                        .toString())
                .body(download.resource());
    }
}
