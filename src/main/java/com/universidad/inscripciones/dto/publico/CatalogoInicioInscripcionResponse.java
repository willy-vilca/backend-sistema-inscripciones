package com.universidad.inscripciones.dto.publico;

import java.util.List;

import com.universidad.inscripciones.dto.common.OptionResponse;

public record CatalogoInicioInscripcionResponse(
        List<OptionResponse> tiposDocumento,
        List<ProcesoAdmisionOption> procesosAdmision,
        List<ModalidadAdmisionOption> modalidadesAdmision,
        List<OptionResponse> tiposColegio) {
}
