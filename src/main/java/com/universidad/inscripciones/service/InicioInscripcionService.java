package com.universidad.inscripciones.service;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.universidad.inscripciones.dto.common.OptionResponse;
import com.universidad.inscripciones.dto.publico.CatalogoConsultaInscripcionResponse;
import com.universidad.inscripciones.dto.publico.CatalogoInicioInscripcionResponse;
import com.universidad.inscripciones.dto.publico.ConsultaInscripcionRequest;
import com.universidad.inscripciones.dto.publico.ConsultaInscripcionResponse;
import com.universidad.inscripciones.dto.publico.DocumentoDisponibilidadRequest;
import com.universidad.inscripciones.dto.publico.DocumentoDisponibilidadResponse;
import com.universidad.inscripciones.dto.publico.ModalidadAdmisionOption;
import com.universidad.inscripciones.dto.publico.ProcesoAdmisionOption;
import com.universidad.inscripciones.model.enums.EstadoInscripcion;
import com.universidad.inscripciones.model.enums.TipoColegio;
import com.universidad.inscripciones.model.enums.TipoDocumento;
import com.universidad.inscripciones.repository.InscripcionRepository;
import com.universidad.inscripciones.repository.ModalidadAdmisionRepository;
import com.universidad.inscripciones.repository.ProcesoAdmisionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InicioInscripcionService {

    private final ProcesoAdmisionRepository procesoAdmisionRepository;
    private final ModalidadAdmisionRepository modalidadAdmisionRepository;
    private final InscripcionRepository inscripcionRepository;

    @Transactional(readOnly = true)
    public CatalogoInicioInscripcionResponse obtenerCatalogos() {
        List<OptionResponse> tiposDocumento = Arrays.stream(TipoDocumento.values())
                .map(tipo -> new OptionResponse(tipo.name(), etiqueta(tipo.name())))
                .toList();

        List<OptionResponse> tiposColegio = Arrays.stream(TipoColegio.values())
                .map(tipo -> new OptionResponse(tipo.name(), etiqueta(tipo.name())))
                .toList();

        return new CatalogoInicioInscripcionResponse(
                tiposDocumento,
                procesoAdmisionRepository.findVigentesOrderByNombreAsc()
                        .stream()
                        .map(ProcesoAdmisionOption::fromEntity)
                        .toList(),
                modalidadAdmisionRepository.findByActivoTrueOrderByNombreAsc()
                        .stream()
                        .map(ModalidadAdmisionOption::fromEntity)
                        .toList(),
                tiposColegio);
    }

    @Transactional(readOnly = true)
    public CatalogoConsultaInscripcionResponse obtenerCatalogosConsulta() {
        List<OptionResponse> tiposDocumento = Arrays.stream(TipoDocumento.values())
                .map(tipo -> new OptionResponse(tipo.name(), etiqueta(tipo.name())))
                .toList();

        return new CatalogoConsultaInscripcionResponse(
                tiposDocumento,
                procesoAdmisionRepository.findAllByOrderByNombreAsc()
                        .stream()
                        .map(ProcesoAdmisionOption::fromEntity)
                        .toList());
    }

    @Transactional(readOnly = true)
    public DocumentoDisponibilidadResponse verificarDocumento(DocumentoDisponibilidadRequest request) {
        String numeroDocumento = request.numeroDocumento().trim();
        boolean yaInscrito = inscripcionRepository
                .existsByPostulanteTipoDocumentoAndPostulanteNumeroDocumentoAndProcesoAdmisionIdAndEstadoNot(
                        request.tipoDocumento(),
                        numeroDocumento,
                        request.procesoAdmisionId(),
                        EstadoInscripcion.ANULADA);

        if (yaInscrito) {
            return new DocumentoDisponibilidadResponse(
                    false,
                    "El numero de documento ya se encuentra inscrito en este proceso de admision.");
        }

        return new DocumentoDisponibilidadResponse(
                true,
                "El documento esta disponible para iniciar la inscripcion.");
    }

    @Transactional(readOnly = true)
    public ConsultaInscripcionResponse consultarInscripcion(ConsultaInscripcionRequest request) {
        String numeroDocumento = request.numeroDocumento().trim();

        return inscripcionRepository
                .buscarConsultaPublica(
                        request.procesoAdmisionId(),
                        request.tipoDocumento(),
                        numeroDocumento)
                .map(ConsultaInscripcionResponse::fromEntity)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No se encontro ninguna inscripcion con ese numero de documento en el proceso seleccionado."));
    }

    private String etiqueta(String value) {
        return value.replace("_", " ");
    }
}
