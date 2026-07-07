package com.universidad.inscripciones.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.universidad.inscripciones.model.entity.Postulante;
import com.universidad.inscripciones.model.enums.TipoDocumento;

public interface PostulanteRepository extends JpaRepository<Postulante, Long> {

    Optional<Postulante> findByTipoDocumentoAndNumeroDocumento(TipoDocumento tipoDocumento, String numeroDocumento);

    boolean existsByTipoDocumentoAndNumeroDocumento(TipoDocumento tipoDocumento, String numeroDocumento);
}
