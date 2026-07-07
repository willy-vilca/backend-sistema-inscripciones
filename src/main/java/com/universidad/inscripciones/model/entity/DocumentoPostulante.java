package com.universidad.inscripciones.model.entity;

import com.universidad.inscripciones.model.common.AuditableEntity;
import com.universidad.inscripciones.model.enums.DocumentoEstado;

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
@Table(name = "documentos_postulante")
public class DocumentoPostulante extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "inscripcion_id", nullable = false)
    private Inscripcion inscripcion;

    @Column(nullable = false, length = 140)
    private String tipoDocumento;

    @Column(nullable = false, length = 180)
    private String nombreOriginal;

    @Column(nullable = false, length = 260)
    private String rutaArchivo;

    @Column(length = 120)
    private String contentType;

    private Long tamanioBytes;

    @Column(nullable = false)
    private boolean obligatorio;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private DocumentoEstado estado;
}
