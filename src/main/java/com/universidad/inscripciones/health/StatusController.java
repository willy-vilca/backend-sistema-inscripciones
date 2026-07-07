package com.universidad.inscripciones.health;

import java.time.LocalDateTime;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/status")
public class StatusController {

    @GetMapping
    public StatusResponse getStatus() {
        return new StatusResponse(
                "online",
                "Sistema de inscripciones API",
                "Backend conectado correctamente",
                LocalDateTime.now());
    }

    public record StatusResponse(
            String status,
            String service,
            String message,
            LocalDateTime timestamp) {
    }
}
