package com.universidad.inscripciones.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.universidad.inscripciones.model.entity.PagoBancario;

public interface PagoBancarioRepository extends JpaRepository<PagoBancario, Long> {

    boolean existsByNroMovimiento(String nroMovimiento);

    Optional<PagoBancario> findByNroMovimiento(String nroMovimiento);

    List<PagoBancario> findByNroMovimientoIn(Collection<String> numerosMovimiento);

    @Query("""
            select p from PagoBancario p
            order by p.creadoEn desc, p.id desc
            """)
    List<PagoBancario> listarParaAdministracion(Pageable pageable);

    @Query("""
            select p from PagoBancario p
            where p.usado = :usado
            order by p.creadoEn desc, p.id desc
            """)
    List<PagoBancario> listarParaAdministracionPorEstado(
            @Param("usado") boolean usado,
            Pageable pageable);

    @Query("""
            select p from PagoBancario p
            where (lower(p.nroMovimiento) like concat('%', lower(:busqueda), '%')
                or lower(p.nombreCliente) like concat('%', lower(:busqueda), '%'))
              and (:usado is null or p.usado = :usado)
            order by p.creadoEn desc, p.id desc
            """)
    List<PagoBancario> buscarParaAdministracionConBusqueda(
            @Param("busqueda") String busqueda,
            @Param("usado") Boolean usado,
            Pageable pageable);

    long countByUsado(boolean usado);
}
