package com.universidad.inscripciones.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.universidad.inscripciones.model.entity.AreaAcademica;

public interface AreaAcademicaRepository extends JpaRepository<AreaAcademica, Long> {

    boolean existsByCodigo(String codigo);

    Optional<AreaAcademica> findByCodigo(String codigo);
}
