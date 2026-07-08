package com.universidad.inscripciones.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.universidad.inscripciones.model.entity.ModalidadAdmision;

public interface ModalidadAdmisionRepository extends JpaRepository<ModalidadAdmision, Long> {

    boolean existsByNombre(String nombre);

    Optional<ModalidadAdmision> findByNombre(String nombre);

    List<ModalidadAdmision> findByActivoTrueOrderByNombreAsc();
}
