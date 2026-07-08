package com.universidad.inscripciones.dto.admin;

import java.time.LocalDateTime;

public record AdminLoginResponse(
        String token,
        LocalDateTime expiresAt,
        AdminUserResponse user) {
}
