package com.universidad.inscripciones.dto.admin;

import org.springframework.core.io.Resource;

public record ArchivoDownload(
        String filename,
        String contentType,
        Resource resource) {
}
