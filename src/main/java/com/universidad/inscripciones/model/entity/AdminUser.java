package com.universidad.inscripciones.model.entity;

import java.time.LocalDateTime;

import com.universidad.inscripciones.model.common.AuditableEntity;
import com.universidad.inscripciones.model.enums.RolAdmin;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "usuarios_admin")
public class AdminUser extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nombreCompleto;

    @Column(nullable = false, unique = true, length = 60)
    private String username;

    @Column(unique = true, length = 120)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private RolAdmin rol;

    @Column(nullable = false)
    private boolean activo;

    private LocalDateTime ultimoAccesoEn;
}
