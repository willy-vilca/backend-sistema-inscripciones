package com.universidad.inscripciones.dto.inscripcion;

import org.springframework.core.io.Resource;

public record CarnePdfDownload(
        String filename,
        Resource resource) {
}
