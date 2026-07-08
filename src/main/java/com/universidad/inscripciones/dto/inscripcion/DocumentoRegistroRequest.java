package com.universidad.inscripciones.dto.inscripcion;

public record DocumentoRegistroRequest(
        String clave,
        String tipoDocumento,
        boolean obligatorio) {
}
