package com.universidad.inscripciones.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.universidad.inscripciones.model.entity.EscuelaProfesional;

public interface EscuelaProfesionalRepository extends JpaRepository<EscuelaProfesional, Long> {

    List<EscuelaProfesional> findByAreaAcademicaIdAndActivoTrueOrderByNombreAsc(Long areaAcademicaId);

    Optional<EscuelaProfesional> findByAreaAcademicaCodigoAndNombre(String codigoArea, String nombre);

    boolean existsByAreaAcademicaIdAndNombre(Long areaAcademicaId, String nombre);

    boolean existsByAreaAcademicaIdAndNombreAndIdNot(Long areaAcademicaId, String nombre, Long id);

    long countByAreaAcademicaId(Long areaAcademicaId);

    List<EscuelaProfesional> findAllByOrderByAreaAcademicaCodigoAscNombreAsc();
}
