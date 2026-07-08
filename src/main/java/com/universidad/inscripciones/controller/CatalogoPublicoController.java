package com.universidad.inscripciones.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.universidad.inscripciones.dto.publico.AreaAcademicaOption;
import com.universidad.inscripciones.dto.publico.EscuelaProfesionalOption;
import com.universidad.inscripciones.dto.publico.ProgramaAcademicoOption;
import com.universidad.inscripciones.repository.AreaAcademicaRepository;
import com.universidad.inscripciones.repository.EscuelaProfesionalRepository;
import com.universidad.inscripciones.repository.ProgramaAcademicoRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/public/catalogos")
@RequiredArgsConstructor
public class CatalogoPublicoController {

    private final AreaAcademicaRepository areaAcademicaRepository;
    private final EscuelaProfesionalRepository escuelaProfesionalRepository;
    private final ProgramaAcademicoRepository programaAcademicoRepository;

    @GetMapping("/areas")
    public List<AreaAcademicaOption> listarAreas() {
        return areaAcademicaRepository.findAll()
                .stream()
                .filter(area -> area.isActivo())
                .map(AreaAcademicaOption::fromEntity)
                .toList();
    }

    @GetMapping("/escuelas")
    public List<EscuelaProfesionalOption> listarEscuelas(@RequestParam Long areaId) {
        return escuelaProfesionalRepository.findByAreaAcademicaIdAndActivoTrueOrderByNombreAsc(areaId)
                .stream()
                .map(EscuelaProfesionalOption::fromEntity)
                .toList();
    }

    @GetMapping("/programas")
    public List<ProgramaAcademicoOption> listarProgramas(@RequestParam Long escuelaId) {
        return programaAcademicoRepository.findByEscuelaProfesionalIdAndActivoTrueOrderByNombreAsc(escuelaId)
                .stream()
                .map(ProgramaAcademicoOption::fromEntity)
                .toList();
    }
}
