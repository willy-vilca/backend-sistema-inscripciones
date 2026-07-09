package com.universidad.inscripciones.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.universidad.inscripciones.dto.admin.AreaAcademicaAdminRequest;
import com.universidad.inscripciones.dto.admin.AreaAcademicaAdminResponse;
import com.universidad.inscripciones.dto.admin.CarreraProfesionalAdminRequest;
import com.universidad.inscripciones.dto.admin.CarreraProfesionalAdminResponse;
import com.universidad.inscripciones.dto.admin.EstadisticaInscripcionResponse;
import com.universidad.inscripciones.dto.admin.EstadisticaInscripcionResponse.GrupoConteo;
import com.universidad.inscripciones.dto.admin.ProcesoAdmisionAdminRequest;
import com.universidad.inscripciones.dto.admin.ProcesoAdmisionAdminResponse;
import com.universidad.inscripciones.model.entity.AreaAcademica;
import com.universidad.inscripciones.model.entity.EscuelaProfesional;
import com.universidad.inscripciones.model.entity.ProcesoAdmision;
import com.universidad.inscripciones.model.entity.ProgramaAcademico;
import com.universidad.inscripciones.repository.AreaAcademicaRepository;
import com.universidad.inscripciones.repository.EscuelaProfesionalRepository;
import com.universidad.inscripciones.repository.InscripcionRepository;
import com.universidad.inscripciones.repository.ProcesoAdmisionRepository;
import com.universidad.inscripciones.repository.ProgramaAcademicoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AcademicoAdminService {

    private final ProcesoAdmisionRepository procesoAdmisionRepository;
    private final AreaAcademicaRepository areaAcademicaRepository;
    private final EscuelaProfesionalRepository escuelaProfesionalRepository;
    private final ProgramaAcademicoRepository programaAcademicoRepository;
    private final InscripcionRepository inscripcionRepository;

    @Transactional(readOnly = true)
    public List<ProcesoAdmisionAdminResponse> listarProcesos() {
        return procesoAdmisionRepository.findAll()
                .stream()
                .sorted((a, b) -> b.getCodigo().compareToIgnoreCase(a.getCodigo()))
                .map(proceso -> ProcesoAdmisionAdminResponse.fromEntity(
                        proceso,
                        inscripcionRepository.countByProcesoAdmisionId(proceso.getId())))
                .toList();
    }

    @Transactional
    public ProcesoAdmisionAdminResponse crearProceso(ProcesoAdmisionAdminRequest request) {
        validarFechas(request.fechaInicio(), request.fechaFin());
        String codigo = request.codigo().trim();
        if (procesoAdmisionRepository.existsByCodigo(codigo)) {
            throw new IllegalArgumentException("Ya existe un proceso con ese codigo.");
        }

        ProcesoAdmision proceso = ProcesoAdmision.builder()
                .codigo(codigo)
                .nombre(request.nombre().trim())
                .descripcion(blankToNull(request.descripcion()))
                .fechaInicio(request.fechaInicio())
                .fechaFin(request.fechaFin())
                .activo(request.activo())
                .build();

        proceso = procesoAdmisionRepository.save(proceso);
        return ProcesoAdmisionAdminResponse.fromEntity(proceso, 0);
    }

    @Transactional
    public ProcesoAdmisionAdminResponse actualizarProceso(Long id, ProcesoAdmisionAdminRequest request) {
        validarFechas(request.fechaInicio(), request.fechaFin());
        ProcesoAdmision proceso = procesoAdmisionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Proceso de admision no encontrado."));

        String codigo = request.codigo().trim();
        if (procesoAdmisionRepository.existsByCodigoAndIdNot(codigo, id)) {
            throw new IllegalArgumentException("Ya existe un proceso con ese codigo.");
        }

        proceso.setCodigo(codigo);
        proceso.setNombre(request.nombre().trim());
        proceso.setDescripcion(blankToNull(request.descripcion()));
        proceso.setFechaInicio(request.fechaInicio());
        proceso.setFechaFin(request.fechaFin());
        proceso.setActivo(request.activo());

        return ProcesoAdmisionAdminResponse.fromEntity(
                procesoAdmisionRepository.save(proceso),
                inscripcionRepository.countByProcesoAdmisionId(id));
    }

    @Transactional(readOnly = true)
    public List<AreaAcademicaAdminResponse> listarAreas() {
        return areaAcademicaRepository.findAllByOrderByCodigoAscNombreAsc()
                .stream()
                .map(area -> AreaAcademicaAdminResponse.fromEntity(
                        area,
                        escuelaProfesionalRepository.countByAreaAcademicaId(area.getId()),
                        inscripcionRepository.countByAreaAcademicaId(area.getId())))
                .toList();
    }

    @Transactional
    public AreaAcademicaAdminResponse crearArea(AreaAcademicaAdminRequest request) {
        String codigo = request.codigo().trim().toUpperCase();
        String nombre = request.nombre().trim();
        if (areaAcademicaRepository.existsByCodigo(codigo)) {
            throw new IllegalArgumentException("Ya existe un area con ese codigo.");
        }
        if (areaAcademicaRepository.existsByNombre(nombre)) {
            throw new IllegalArgumentException("Ya existe un area con ese nombre.");
        }

        AreaAcademica area = areaAcademicaRepository.save(AreaAcademica.builder()
                .codigo(codigo)
                .nombre(nombre)
                .activo(request.activo())
                .build());

        return AreaAcademicaAdminResponse.fromEntity(area, 0, 0);
    }

    @Transactional
    public AreaAcademicaAdminResponse actualizarArea(Long id, AreaAcademicaAdminRequest request) {
        AreaAcademica area = areaAcademicaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Area academica no encontrada."));

        String codigo = request.codigo().trim().toUpperCase();
        String nombre = request.nombre().trim();
        if (areaAcademicaRepository.existsByCodigoAndIdNot(codigo, id)) {
            throw new IllegalArgumentException("Ya existe un area con ese codigo.");
        }
        if (areaAcademicaRepository.existsByNombreAndIdNot(nombre, id)) {
            throw new IllegalArgumentException("Ya existe un area con ese nombre.");
        }

        area.setCodigo(codigo);
        area.setNombre(nombre);
        area.setActivo(request.activo());
        area = areaAcademicaRepository.save(area);

        return AreaAcademicaAdminResponse.fromEntity(
                area,
                escuelaProfesionalRepository.countByAreaAcademicaId(id),
                inscripcionRepository.countByAreaAcademicaId(id));
    }

    @Transactional(readOnly = true)
    public List<CarreraProfesionalAdminResponse> listarCarreras() {
        return escuelaProfesionalRepository.findAllByOrderByAreaAcademicaCodigoAscNombreAsc()
                .stream()
                .map(this::toCarreraResponse)
                .toList();
    }

    @Transactional
    public CarreraProfesionalAdminResponse crearCarrera(CarreraProfesionalAdminRequest request) {
        AreaAcademica area = areaAcademicaRepository.findById(request.areaAcademicaId())
                .orElseThrow(() -> new IllegalArgumentException("Area academica no encontrada."));
        String nombre = request.nombre().trim();

        if (escuelaProfesionalRepository.existsByAreaAcademicaIdAndNombre(area.getId(), nombre)) {
            throw new IllegalArgumentException("Ya existe una carrera con ese nombre en el area seleccionada.");
        }

        EscuelaProfesional carrera = escuelaProfesionalRepository.save(EscuelaProfesional.builder()
                .areaAcademica(area)
                .nombre(nombre)
                .activo(request.activo())
                .build());

        ProgramaAcademico programa = programaAcademicoRepository.save(ProgramaAcademico.builder()
                .escuelaProfesional(carrera)
                .nombre(nombre)
                .activo(request.activo())
                .build());

        return CarreraProfesionalAdminResponse.fromEntity(carrera, programa.getId(), 0);
    }

    @Transactional
    public CarreraProfesionalAdminResponse actualizarCarrera(Long id, CarreraProfesionalAdminRequest request) {
        EscuelaProfesional carrera = escuelaProfesionalRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Carrera profesional no encontrada."));
        AreaAcademica area = areaAcademicaRepository.findById(request.areaAcademicaId())
                .orElseThrow(() -> new IllegalArgumentException("Area academica no encontrada."));

        String nombre = request.nombre().trim();
        if (escuelaProfesionalRepository.existsByAreaAcademicaIdAndNombreAndIdNot(area.getId(), nombre, id)) {
            throw new IllegalArgumentException("Ya existe una carrera con ese nombre en el area seleccionada.");
        }

        carrera.setAreaAcademica(area);
        carrera.setNombre(nombre);
        carrera.setActivo(request.activo());
        carrera = escuelaProfesionalRepository.save(carrera);

        ProgramaAcademico programa = sincronizarPrograma(carrera, nombre, request.activo());
        return CarreraProfesionalAdminResponse.fromEntity(
                carrera,
                programa == null ? null : programa.getId(),
                inscripcionRepository.countByEscuelaProfesionalId(id));
    }

    @Transactional(readOnly = true)
    public EstadisticaInscripcionResponse obtenerEstadisticas(
            Long procesoId,
            Long modalidadId,
            Long areaId,
            Long escuelaId) {

        return new EstadisticaInscripcionResponse(
                inscripcionRepository.contarPorFiltros(procesoId, modalidadId, areaId, escuelaId),
                mapGrupos(inscripcionRepository.agruparPorProceso()),
                mapGrupos(inscripcionRepository.agruparPorModalidad()),
                mapGrupos(inscripcionRepository.agruparPorArea()),
                mapGrupos(inscripcionRepository.agruparPorCarrera()));
    }

    private CarreraProfesionalAdminResponse toCarreraResponse(EscuelaProfesional carrera) {
        Long programaId = programaAcademicoRepository.findByEscuelaProfesionalIdOrderByNombreAsc(carrera.getId())
                .stream()
                .findFirst()
                .map(ProgramaAcademico::getId)
                .orElse(null);

        return CarreraProfesionalAdminResponse.fromEntity(
                carrera,
                programaId,
                inscripcionRepository.countByEscuelaProfesionalId(carrera.getId()));
    }

    private ProgramaAcademico sincronizarPrograma(EscuelaProfesional carrera, String nombre, boolean activo) {
        List<ProgramaAcademico> programas = programaAcademicoRepository.findByEscuelaProfesionalIdOrderByNombreAsc(carrera.getId());
        if (programas.isEmpty()) {
            return programaAcademicoRepository.save(ProgramaAcademico.builder()
                    .escuelaProfesional(carrera)
                    .nombre(nombre)
                    .activo(activo)
                    .build());
        }

        ProgramaAcademico programa = programas.get(0);
        programa.setNombre(nombre);
        programa.setActivo(activo);
        return programaAcademicoRepository.save(programa);
    }

    private List<GrupoConteo> mapGrupos(List<Object[]> rows) {
        return rows.stream()
                .map(row -> new GrupoConteo(
                        (Long) row[0],
                        (String) row[1],
                        (Long) row[2]))
                .toList();
    }

    private void validarFechas(LocalDate inicio, LocalDate fin) {
        if (inicio != null && fin != null && fin.isBefore(inicio)) {
            throw new IllegalArgumentException("La fecha fin no puede ser anterior a la fecha inicio.");
        }
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
