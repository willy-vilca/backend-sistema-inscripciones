package com.universidad.inscripciones.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.universidad.inscripciones.model.entity.AreaAcademica;

public interface AreaAcademicaRepository extends JpaRepository<AreaAcademica, Long> {

    boolean existsByCodigo(String codigo);

    boolean existsByCodigoAndIdNot(String codigo, Long id);

    boolean existsByNombre(String nombre);

    boolean existsByNombreAndIdNot(String nombre, Long id);

    Optional<AreaAcademica> findByCodigo(String codigo);

    List<AreaAcademica> findAllByOrderByCodigoAscNombreAsc();
}
