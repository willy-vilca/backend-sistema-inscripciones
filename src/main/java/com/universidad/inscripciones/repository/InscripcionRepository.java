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
            where i.procesoAdmision.id = :procesoAdmisionId
                and p.tipoDocumento = :tipoDocumento
                and p.numeroDocumento = :numeroDocumento
            """)
    Optional<Inscripcion> buscarConsultaPublica(
            @Param("procesoAdmisionId") Long procesoAdmisionId,
            @Param("tipoDocumento") TipoDocumento tipoDocumento,
            @Param("numeroDocumento") String numeroDocumento);

    long countByEstado(EstadoInscripcion estado);

    long countByFechaRegistroBetween(LocalDateTime inicio, LocalDateTime fin);

    boolean existsByPostulanteTipoDocumentoAndPostulanteNumeroDocumentoAndProcesoAdmisionIdAndEstadoNot(
            TipoDocumento tipoDocumento,
            String numeroDocumento,
            Long procesoAdmisionId,
            EstadoInscripcion estado);

    long countByProcesoAdmisionId(Long procesoAdmisionId);

    long countByAreaAcademicaId(Long areaAcademicaId);

    long countByEscuelaProfesionalId(Long escuelaProfesionalId);

    @Query("""
            select count(i)
            from Inscripcion i
            where (:procesoId is null or i.procesoAdmision.id = :procesoId)
                and (:modalidadId is null or i.modalidadAdmision.id = :modalidadId)
                and (:areaId is null or i.areaAcademica.id = :areaId)
                and (:escuelaId is null or i.escuelaProfesional.id = :escuelaId)
                and i.estado <> com.universidad.inscripciones.model.enums.EstadoInscripcion.ANULADA
            """)
    long contarPorFiltros(
            @Param("procesoId") Long procesoId,
            @Param("modalidadId") Long modalidadId,
            @Param("areaId") Long areaId,
            @Param("escuelaId") Long escuelaId);

    @Query("""
            select i.procesoAdmision.id, i.procesoAdmision.nombre, count(i)
            from Inscripcion i
            where i.estado <> com.universidad.inscripciones.model.enums.EstadoInscripcion.ANULADA
            group by i.procesoAdmision.id, i.procesoAdmision.nombre
            order by count(i) desc, i.procesoAdmision.nombre asc
            """)
    List<Object[]> agruparPorProceso();

    @Query("""
            select i.modalidadAdmision.id, i.modalidadAdmision.nombre, count(i)
            from Inscripcion i
            where i.estado <> com.universidad.inscripciones.model.enums.EstadoInscripcion.ANULADA
            group by i.modalidadAdmision.id, i.modalidadAdmision.nombre
            order by count(i) desc, i.modalidadAdmision.nombre asc
            """)
    List<Object[]> agruparPorModalidad();

    @Query("""
            select i.areaAcademica.id, i.areaAcademica.nombre, count(i)
            from Inscripcion i
            where i.areaAcademica is not null
                and i.estado <> com.universidad.inscripciones.model.enums.EstadoInscripcion.ANULADA
            group by i.areaAcademica.id, i.areaAcademica.nombre
            order by count(i) desc, i.areaAcademica.nombre asc
            """)
    List<Object[]> agruparPorArea();

    @Query("""
            select i.escuelaProfesional.id, i.escuelaProfesional.nombre, count(i)
            from Inscripcion i
            where i.escuelaProfesional is not null
                and i.estado <> com.universidad.inscripciones.model.enums.EstadoInscripcion.ANULADA
            group by i.escuelaProfesional.id, i.escuelaProfesional.nombre
            order by count(i) desc, i.escuelaProfesional.nombre asc
            """)
    List<Object[]> agruparPorCarrera();

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
