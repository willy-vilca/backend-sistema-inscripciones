package com.universidad.inscripciones.dto.admin;

import java.util.List;

public record EstadisticaInscripcionResponse(
        long totalFiltrado,
        List<GrupoConteo> porProceso,
        List<GrupoConteo> porModalidad,
        List<GrupoConteo> porArea,
        List<GrupoConteo> porCarrera) {

    public record GrupoConteo(
            Long id,
            String nombre,
            long total) {
    }
}
