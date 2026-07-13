package com.universidad.inscripciones.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.universidad.inscripciones.dto.pago.PagoBancarioResponse;
import com.universidad.inscripciones.dto.pago.PagoImportacionResponse;
import com.universidad.inscripciones.dto.pago.PagoResumenResponse;
import com.universidad.inscripciones.service.PagoBancarioService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/pagos")
@RequiredArgsConstructor
public class PagoBancarioController {

    private final PagoBancarioService pagoBancarioService;

    @GetMapping
    public List<PagoBancarioResponse> listarPagos(
            @RequestParam(required = false) String busqueda,
            @RequestParam(required = false, defaultValue = "TODOS") String estado,
            @RequestParam(required = false, defaultValue = "0") int bloque) {
        return pagoBancarioService.listarPagosAdministracion(busqueda, estado, bloque);
    }

    @GetMapping("/resumen")
    public PagoResumenResponse obtenerResumen() {
        return pagoBancarioService.obtenerResumen();
    }

    @PostMapping("/importar")
    public ResponseEntity<PagoImportacionResponse> importarPagos(@RequestParam("archivo") MultipartFile archivo) {
        return ResponseEntity.ok(pagoBancarioService.importarExcel(archivo));
    }
}
