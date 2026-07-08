package com.universidad.inscripciones.dto.admin;

import java.time.LocalDateTime;

import com.universidad.inscripciones.model.entity.AdminUser;
import com.universidad.inscripciones.model.enums.RolAdmin;

public record AdminUserResponse(
        Long id,
        String nombreCompleto,
        String username,
        String email,
        RolAdmin rol,
        boolean activo,
        LocalDateTime ultimoAccesoEn,
        LocalDateTime creadoEn) {

    public static AdminUserResponse fromEntity(AdminUser user) {
        return new AdminUserResponse(
                user.getId(),
                user.getNombreCompleto(),
                user.getUsername(),
                user.getEmail(),
                user.getRol(),
                user.isActivo(),
                user.getUltimoAccesoEn(),
                user.getCreadoEn());
    }
}
