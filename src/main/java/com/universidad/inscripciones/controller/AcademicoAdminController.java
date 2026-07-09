package com.universidad.inscripciones.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.universidad.inscripciones.dto.admin.AreaAcademicaAdminRequest;
import com.universidad.inscripciones.dto.admin.AreaAcademicaAdminResponse;
import com.universidad.inscripciones.dto.admin.CarreraProfesionalAdminRequest;
import com.universidad.inscripciones.dto.admin.CarreraProfesionalAdminResponse;
import com.universidad.inscripciones.dto.admin.EstadisticaInscripcionResponse;
import com.universidad.inscripciones.dto.admin.ProcesoAdmisionAdminRequest;
import com.universidad.inscripciones.dto.admin.ProcesoAdmisionAdminResponse;
import com.universidad.inscripciones.service.AcademicoAdminService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/academico")
@RequiredArgsConstructor
public class AcademicoAdminController {

    private final AcademicoAdminService academicoAdminService;

    @GetMapping("/procesos")
    public List<ProcesoAdmisionAdminResponse> listarProcesos() {
        return academicoAdminService.listarProcesos();
    }

    @PostMapping("/procesos")
    public ProcesoAdmisionAdminResponse crearProceso(@Valid @RequestBody ProcesoAdmisionAdminRequest request) {
        return academicoAdminService.crearProceso(request);
    }

    @PutMapping("/procesos/{id}")
    public ProcesoAdmisionAdminResponse actualizarProceso(
            @PathVariable Long id,
            @Valid @RequestBody ProcesoAdmisionAdminRequest request) {
        return academicoAdminService.actualizarProceso(id, request);
    }

    @GetMapping("/areas")
    public List<AreaAcademicaAdminResponse> listarAreas() {
        return academicoAdminService.listarAreas();
    }

    @PostMapping("/areas")
    public AreaAcademicaAdminResponse crearArea(@Valid @RequestBody AreaAcademicaAdminRequest request) {
        return academicoAdminService.crearArea(request);
    }

    @PutMapping("/areas/{id}")
    public AreaAcademicaAdminResponse actualizarArea(
            @PathVariable Long id,
            @Valid @RequestBody AreaAcademicaAdminRequest request) {
        return academicoAdminService.actualizarArea(id, request);
    }

    @GetMapping("/carreras")
    public List<CarreraProfesionalAdminResponse> listarCarreras() {
        return academicoAdminService.listarCarreras();
    }

    @PostMapping("/carreras")
    public CarreraProfesionalAdminResponse crearCarrera(@Valid @RequestBody CarreraProfesionalAdminRequest request) {
        return academicoAdminService.crearCarrera(request);
    }

    @PutMapping("/carreras/{id}")
    public CarreraProfesionalAdminResponse actualizarCarrera(
            @PathVariable Long id,
            @Valid @RequestBody CarreraProfesionalAdminRequest request) {
        return academicoAdminService.actualizarCarrera(id, request);
    }

    @GetMapping("/estadisticas")
    public EstadisticaInscripcionResponse obtenerEstadisticas(
            @RequestParam(required = false) Long procesoId,
            @RequestParam(required = false) Long modalidadId,
            @RequestParam(required = false) Long areaId,
            @RequestParam(required = false) Long escuelaId) {
        return academicoAdminService.obtenerEstadisticas(procesoId, modalidadId, areaId, escuelaId);
    }
}
