package com.universidad.inscripciones.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.universidad.inscripciones.model.entity.ProcesoAdmision;

public interface ProcesoAdmisionRepository extends JpaRepository<ProcesoAdmision, Long> {

    boolean existsByCodigo(String codigo);

    Optional<ProcesoAdmision> findByCodigo(String codigo);

    List<ProcesoAdmision> findByActivoTrueOrderByNombreAsc();
}
