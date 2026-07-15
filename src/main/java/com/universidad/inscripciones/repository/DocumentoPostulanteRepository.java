package com.universidad.inscripciones.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.universidad.inscripciones.model.entity.DocumentoPostulante;

public interface DocumentoPostulanteRepository extends JpaRepository<DocumentoPostulante, Long> {

    List<DocumentoPostulante> findByInscripcionId(Long inscripcionId);

    Optional<DocumentoPostulante> findByIdAndInscripcionId(Long id, Long inscripcionId);

    Optional<DocumentoPostulante> findFirstByInscripcionIdAndTipoDocumento(Long inscripcionId, String tipoDocumento);

    boolean existsByInscripcionIdAndTipoDocumento(Long inscripcionId, String tipoDocumento);
}
