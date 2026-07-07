package com.universidad.inscripciones.model.entity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.universidad.inscripciones.model.common.AuditableEntity;
import com.universidad.inscripciones.model.enums.EstadoCivil;
import com.universidad.inscripciones.model.enums.EstudiosConcluidos;
import com.universidad.inscripciones.model.enums.Sexo;
import com.universidad.inscripciones.model.enums.TipoApoderado;
import com.universidad.inscripciones.model.enums.TipoDocumento;
import com.universidad.inscripciones.model.enums.TipoEducacion;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
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
        name = "postulantes",
        uniqueConstraints = @UniqueConstraint(name = "uk_postulante_documento", columnNames = {"tipo_documento", "numero_documento"}))
public class Postulante extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_documento", nullable = false, length = 30)
    private TipoDocumento tipoDocumento;

    @Column(name = "numero_documento", nullable = false, length = 20)
    private String numeroDocumento;

    @Column(nullable = false, length = 100)
    private String nombres;

    @Column(nullable = false, length = 80)
    private String apellidoPaterno;

    @Column(length = 80)
    private String apellidoMaterno;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Sexo sexo;

    private LocalDate fechaNacimiento;

    private Integer edad;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private EstadoCivil estadoCivil;

    private Integer numeroHijos;

    @Column(length = 80)
    private String procedencia;

    @Column(length = 80)
    private String paisNacimiento;

    @Column(length = 80)
    private String departamentoNacimiento;

    @Column(length = 80)
    private String provinciaNacimiento;

    @Column(length = 80)
    private String distritoNacimiento;

    @Column(length = 80)
    private String departamentoDomicilio;

    @Column(length = 80)
    private String provinciaDomicilio;

    @Column(length = 80)
    private String distritoDomicilio;

    @Column(length = 220)
    private String direccion;

    @Column(length = 120)
    private String correoElectronico;

    @Column(length = 20)
    private String telefono1;

    @Column(length = 20)
    private String telefono2;

    @Column(nullable = false)
    private boolean trabaja;

    @Column(length = 120)
    private String ocupacion;

    @Column(length = 120)
    private String condicionLaboral;

    @Column(length = 180)
    private String institucionEmpresa;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private TipoApoderado tipoApoderado;

    @Column(length = 160)
    private String apoderadoNombreCompleto;

    @Column(length = 80)
    private String apoderadoRelacion;

    @Column(length = 120)
    private String apoderadoOcupacion;

    @Column(length = 180)
    private String apoderadoCentroLaboral;

    @Column(length = 20)
    private String apoderadoTelefono;

    @Column(length = 120)
    private String apoderadoCorreo;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private TipoEducacion tipoEducacionSecundaria;

    @Enumerated(EnumType.STRING)
    @Column(length = 40)
    private EstudiosConcluidos estudiosConcluidos;

    @Column(length = 80)
    private String colegioDepartamento;

    @Column(length = 80)
    private String colegioProvincia;

    @Column(length = 180)
    private String institucionEducativa;

    private Integer periodoEstudioInicio;

    private Integer periodoEstudioFin;

    @Column(nullable = false)
    private boolean presentaDiscapacidad;

    @Column(length = 180)
    private String discapacidadDetalle;

    @Column(length = 180)
    private String preparacionUniversitaria;

    @Column(length = 260)
    private String fotoPath;

    @OneToMany(mappedBy = "postulante", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Inscripcion> inscripciones = new ArrayList<>();
}
