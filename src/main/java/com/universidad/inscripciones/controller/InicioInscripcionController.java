package com.universidad.inscripciones.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.universidad.inscripciones.dto.publico.CatalogoInicioInscripcionResponse;
import com.universidad.inscripciones.dto.publico.DocumentoDisponibilidadRequest;
import com.universidad.inscripciones.dto.publico.DocumentoDisponibilidadResponse;
import com.universidad.inscripciones.dto.publico.PagoValidacionRequest;
import com.universidad.inscripciones.dto.publico.PagoValidacionResponse;
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

    @GetMapping("/catalogos")
    public CatalogoInicioInscripcionResponse obtenerCatalogos() {
        return inicioInscripcionService.obtenerCatalogos();
    }

    @PostMapping("/verificar-documento")
    public DocumentoDisponibilidadResponse verificarDocumento(
            @Valid @RequestBody DocumentoDisponibilidadRequest request) {
        return inicioInscripcionService.verificarDocumento(request);
    }

    @PostMapping("/validar-pago")
    public PagoValidacionResponse validarPago(@Valid @RequestBody PagoValidacionRequest request) {
        return pagoValidacionService.validarPago(request);
    }
}
