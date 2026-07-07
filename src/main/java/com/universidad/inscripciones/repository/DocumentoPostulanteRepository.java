package com.universidad.inscripciones.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.universidad.inscripciones.model.entity.DocumentoPostulante;

public interface DocumentoPostulanteRepository extends JpaRepository<DocumentoPostulante, Long> {

    List<DocumentoPostulante> findByInscripcionId(Long inscripcionId);
}
