package com.universidad.inscripciones.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.universidad.inscripciones.model.entity.ProcesoAdmision;

public interface ProcesoAdmisionRepository extends JpaRepository<ProcesoAdmision, Long> {

    boolean existsByCodigo(String codigo);

    Optional<ProcesoAdmision> findByCodigo(String codigo);

    List<ProcesoAdmision> findByActivoTrueOrderByNombreAsc();

    boolean existsByCodigoAndIdNot(String codigo, Long id);

    @Query("""
            select p
            from ProcesoAdmision p
            where p.activo = true
                and (p.fechaInicio is null or p.fechaInicio <= current_date)
                and (p.fechaFin is null or p.fechaFin >= current_date)
            order by p.nombre asc
            """)
    List<ProcesoAdmision> findVigentesOrderByNombreAsc();
}
