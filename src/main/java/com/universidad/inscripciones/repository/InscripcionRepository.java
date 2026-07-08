package com.universidad.inscripciones.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.universidad.inscripciones.model.entity.Inscripcion;
import com.universidad.inscripciones.model.enums.EstadoInscripcion;
import com.universidad.inscripciones.model.enums.TipoDocumento;

public interface InscripcionRepository extends JpaRepository<Inscripcion, Long> {

    Optional<Inscripcion> findByCodigoPostulante(String codigoPostulante);

    boolean existsByCodigoPostulante(String codigoPostulante);

    long countByEstado(EstadoInscripcion estado);

    long countByFechaRegistroBetween(LocalDateTime inicio, LocalDateTime fin);

    boolean existsByPostulanteTipoDocumentoAndPostulanteNumeroDocumentoAndProcesoAdmisionIdAndEstadoNot(
            TipoDocumento tipoDocumento,
            String numeroDocumento,
            Long procesoAdmisionId,
            EstadoInscripcion estado);

    @Query("""
            select i
            from Inscripcion i
            join fetch i.postulante p
            join fetch i.procesoAdmision pr
            join fetch i.modalidadAdmision m
            join fetch i.pagoBancario pago
            left join fetch i.areaAcademica area
            left join fetch i.escuelaProfesional escuela
            left join fetch i.programaAcademico programa
            order by i.fechaRegistro desc
            """)
    List<Inscripcion> listarRecientesParaAdmin(Pageable pageable);

    @Query("""
            select i
            from Inscripcion i
            join fetch i.postulante p
            join fetch i.procesoAdmision pr
            join fetch i.modalidadAdmision m
            join fetch i.pagoBancario pago
            left join fetch i.areaAcademica area
            left join fetch i.escuelaProfesional escuela
            left join fetch i.programaAcademico programa
            where lower(i.codigoPostulante) like concat('%', lower(:buscar), '%')
                or lower(p.numeroDocumento) like concat('%', lower(:buscar), '%')
                or lower(p.nombres) like concat('%', lower(:buscar), '%')
                or lower(p.apellidoPaterno) like concat('%', lower(:buscar), '%')
                or lower(coalesce(p.apellidoMaterno, '')) like concat('%', lower(:buscar), '%')
            order by i.fechaRegistro desc
            """)
    List<Inscripcion> buscarParaAdmin(@Param("buscar") String buscar, Pageable pageable);

    @Query("""
            select distinct i
            from Inscripcion i
            join fetch i.postulante p
            join fetch i.procesoAdmision pr
            join fetch i.modalidadAdmision m
            join fetch i.pagoBancario pago
            left join fetch i.areaAcademica area
            left join fetch i.escuelaProfesional escuela
            left join fetch i.programaAcademico programa
            left join fetch i.documentos documentos
            where i.id = :id
            """)
    Optional<Inscripcion> buscarDetalleAdmin(@Param("id") Long id);
}
