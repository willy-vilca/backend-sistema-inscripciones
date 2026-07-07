package com.universidad.inscripciones.model.entity;

import java.time.LocalDate;

import com.universidad.inscripciones.model.common.AuditableEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@Table(name = "procesos_admision")
public class ProcesoAdmision extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String codigo;

    @Column(nullable = false, length = 160)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    private LocalDate fechaInicio;

    private LocalDate fechaFin;

    @Column(nullable = false)
    private boolean activo;
}
