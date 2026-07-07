package com.universidad.inscripciones.model.entity;

import com.universidad.inscripciones.model.common.AuditableEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
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
@Table(
        name = "programas_academicos",
        uniqueConstraints = @UniqueConstraint(name = "uk_programa_escuela_nombre", columnNames = {"escuela_profesional_id", "nombre"}))
public class ProgramaAcademico extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "escuela_profesional_id", nullable = false)
    private EscuelaProfesional escuelaProfesional;

    @Column(nullable = false, length = 180)
    private String nombre;

    @Column(nullable = false)
    private boolean activo;
}
