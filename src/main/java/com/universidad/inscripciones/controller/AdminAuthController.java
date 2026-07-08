package com.universidad.inscripciones.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.universidad.inscripciones.dto.admin.AdminLoginRequest;
import com.universidad.inscripciones.dto.admin.AdminLoginResponse;
import com.universidad.inscripciones.dto.admin.AdminUserResponse;
import com.universidad.inscripciones.service.AdminUserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/auth")
@RequiredArgsConstructor
public class AdminAuthController {

    private final AdminUserService adminUserService;

    @PostMapping("/login")
    public AdminLoginResponse login(@Valid @RequestBody AdminLoginRequest request) {
        return adminUserService.login(request);
    }

    @GetMapping("/me")
    public AdminUserResponse me(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        return adminUserService.me(authorization.substring(7));
    }
}
