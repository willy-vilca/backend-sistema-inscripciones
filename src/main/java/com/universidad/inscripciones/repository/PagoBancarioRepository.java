package com.universidad.inscripciones.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.universidad.inscripciones.model.entity.PagoBancario;

public interface PagoBancarioRepository extends JpaRepository<PagoBancario, Long> {

    boolean existsByNroMovimiento(String nroMovimiento);

    Optional<PagoBancario> findByNroMovimiento(String nroMovimiento);
}
