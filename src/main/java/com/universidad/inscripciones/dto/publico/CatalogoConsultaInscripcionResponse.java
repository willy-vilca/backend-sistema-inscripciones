package com.universidad.inscripciones.dto.publico;

import java.util.List;

import com.universidad.inscripciones.dto.common.OptionResponse;

public record CatalogoConsultaInscripcionResponse(
        List<OptionResponse> tiposDocumento,
        List<ProcesoAdmisionOption> procesosAdmision) {
}
