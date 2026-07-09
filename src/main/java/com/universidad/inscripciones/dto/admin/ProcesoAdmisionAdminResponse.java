package com.universidad.inscripciones.dto.admin;

import java.time.LocalDate;

import com.universidad.inscripciones.model.entity.ProcesoAdmision;

public record ProcesoAdmisionAdminResponse(
        Long id,
        String codigo,
        String nombre,
        String descripcion,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        boolean activo,
        boolean vigente,
        boolean estadoEfectivo,
        long inscripciones) {

    public static ProcesoAdmisionAdminResponse fromEntity(ProcesoAdmision proceso, long inscripciones) {
        boolean vigente = vigente(proceso);
        return new ProcesoAdmisionAdminResponse(
                proceso.getId(),
                proceso.getCodigo(),
                proceso.getNombre(),
                proceso.getDescripcion(),
                proceso.getFechaInicio(),
                proceso.getFechaFin(),
                proceso.isActivo(),
                vigente,
                proceso.isActivo() && vigente,
                inscripciones);
    }

    private static boolean vigente(ProcesoAdmision proceso) {
        LocalDate today = LocalDate.now();
        boolean inicioOk = proceso.getFechaInicio() == null || !today.isBefore(proceso.getFechaInicio());
        boolean finOk = proceso.getFechaFin() == null || !today.isAfter(proceso.getFechaFin());
        return inicioOk && finOk;
    }
}
