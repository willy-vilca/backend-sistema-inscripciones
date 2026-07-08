package com.universidad.inscripciones.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.universidad.inscripciones.model.entity.AdminUser;

public interface AdminUserRepository extends JpaRepository<AdminUser, Long> {

    boolean existsByUsername(String username);

    boolean existsByUsernameAndIdNot(String username, Long id);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);

    Optional<AdminUser> findByUsername(String username);

    @Modifying
    @Query(value = "update usuarios_admin set rol = 'ADMIN' where rol = 'SUPER_ADMIN'", nativeQuery = true)
    void migrarRolSuperAdminAAdmin();

    @Modifying
    @Query(value = "alter table usuarios_admin drop constraint if exists usuarios_admin_rol_check", nativeQuery = true)
    void eliminarRestriccionRolAnterior();

    @Modifying
    @Query(value = "alter table usuarios_admin add constraint usuarios_admin_rol_check check (rol in ('ADMIN', 'COORDINADOR'))", nativeQuery = true)
    void crearRestriccionRolActual();
}
