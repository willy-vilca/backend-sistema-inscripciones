package com.universidad.inscripciones.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.universidad.inscripciones.model.entity.ProgramaAcademico;

public interface ProgramaAcademicoRepository extends JpaRepository<ProgramaAcademico, Long> {

    List<ProgramaAcademico> findByEscuelaProfesionalIdAndActivoTrueOrderByNombreAsc(Long escuelaProfesionalId);

    Optional<ProgramaAcademico> findByEscuelaProfesionalIdAndNombre(Long escuelaProfesionalId, String nombre);
}
