package com.universidad.inscripciones.model.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.universidad.inscripciones.model.common.AuditableEntity;
import com.universidad.inscripciones.model.enums.EstadoInscripcion;
import com.universidad.inscripciones.model.enums.MedioDifusion;
import com.universidad.inscripciones.model.enums.TipoColegio;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "inscripciones")
public class Inscripcion extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String codigoPostulante;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "postulante_id", nullable = false)
    private Postulante postulante;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "proceso_admision_id", nullable = false)
    private ProcesoAdmision procesoAdmision;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "modalidad_admision_id", nullable = false)
    private ModalidadAdmision modalidadAdmision;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoColegio tipoColegio;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pago_bancario_id", nullable = false, unique = true)
    private PagoBancario pagoBancario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "area_academica_id")
    private AreaAcademica areaAcademica;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "escuela_profesional_id")
    private EscuelaProfesional escuelaProfesional;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "programa_academico_id")
    private ProgramaAcademico programaAcademico;

    @Enumerated(EnumType.STRING)
    @Column(length = 40)
    private MedioDifusion medioDifusion;

    @Column(length = 120)
    private String medioDifusionOtro;

    @Column(nullable = false)
    private boolean aceptaTerminos;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EstadoInscripcion estado;

    private LocalDateTime fechaRegistro;

    @Column(length = 260)
    private String carnePdfPath;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @OneToMany(mappedBy = "inscripcion", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DocumentoPostulante> documentos = new ArrayList<>();
}
