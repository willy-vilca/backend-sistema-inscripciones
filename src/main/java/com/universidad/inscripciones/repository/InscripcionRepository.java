package com.universidad.inscripciones.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.universidad.inscripciones.model.entity.Inscripcion;
import com.universidad.inscripciones.model.enums.EstadoInscripcion;
import com.universidad.inscripciones.model.enums.TipoDocumento;

public interface InscripcionRepository extends JpaRepository<Inscripcion, Long> {

    Optional<Inscripcion> findByCodigoPostulante(String codigoPostulante);

    boolean existsByCodigoPostulante(String codigoPostulante);

    boolean existsByPostulanteTipoDocumentoAndPostulanteNumeroDocumentoAndProcesoAdmisionIdAndEstadoNot(
            TipoDocumento tipoDocumento,
            String numeroDocumento,
            Long procesoAdmisionId,
            EstadoInscripcion estado);
}
