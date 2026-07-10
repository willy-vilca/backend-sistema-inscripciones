package com.universidad.inscripciones.model.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

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
@Table(name = "pagos_bancarios")
public class PagoBancario extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 180)
    private String nombreCliente;

    @Column(length = 40)
    private String codigo;

    @Column(length = 220)
    private String descripcionPago;

    @Column(precision = 10, scale = 2)
    private BigDecimal importeAPagar;

    @Column(precision = 10, scale = 2)
    private BigDecimal importePagado;

    @Column(length = 30)
    private String oficina;

    @Column(nullable = false, unique = true, length = 50)
    private String nroMovimiento;

    private LocalDateTime fechaPago;

    private LocalDate fechaProceso;

    @Column(length = 30)
    private String formaPago;

    @Column(length = 30)
    private String canal;

    @Column(length = 180)
    private String archivoOrigen;

    @Column(nullable = false)
    private boolean usado;

    private LocalDateTime usadoEn;

}
