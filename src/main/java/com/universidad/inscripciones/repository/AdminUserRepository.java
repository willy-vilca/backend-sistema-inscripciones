package com.universidad.inscripciones.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.universidad.inscripciones.model.entity.AdminUser;

public interface AdminUserRepository extends JpaRepository<AdminUser, Long> {

    boolean existsByUsername(String username);

    Optional<AdminUser> findByUsername(String username);
}
