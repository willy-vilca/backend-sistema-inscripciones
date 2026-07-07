package com.universidad.inscripciones.config;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.universidad.inscripciones.model.entity.AdminUser;
import com.universidad.inscripciones.model.entity.AreaAcademica;
import com.universidad.inscripciones.model.entity.EscuelaProfesional;
import com.universidad.inscripciones.model.entity.ModalidadAdmision;
import com.universidad.inscripciones.model.entity.ProcesoAdmision;
import com.universidad.inscripciones.model.entity.ProgramaAcademico;
import com.universidad.inscripciones.model.enums.RolAdmin;
import com.universidad.inscripciones.repository.AdminUserRepository;
import com.universidad.inscripciones.repository.AreaAcademicaRepository;
import com.universidad.inscripciones.repository.EscuelaProfesionalRepository;
import com.universidad.inscripciones.repository.ModalidadAdmisionRepository;
import com.universidad.inscripciones.repository.ProcesoAdmisionRepository;
import com.universidad.inscripciones.repository.ProgramaAcademicoRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final AdminUserRepository adminUserRepository;
    private final AreaAcademicaRepository areaAcademicaRepository;
    private final EscuelaProfesionalRepository escuelaProfesionalRepository;
    private final ModalidadAdmisionRepository modalidadAdmisionRepository;
    private final ProcesoAdmisionRepository procesoAdmisionRepository;
    private final ProgramaAcademicoRepository programaAcademicoRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        seedAdmin();
        seedProcesoYModalidad();
        seedCatalogoAcademico();
    }

    private void seedAdmin() {
        if (adminUserRepository.existsByUsername("admin")) {
            return;
        }

        AdminUser admin = AdminUser.builder()
                .nombreCompleto("Administrador Principal")
                .username("admin")
                .email("admin@sistema.local")
                .passwordHash(passwordEncoder.encode("admin123"))
                .rol(RolAdmin.SUPER_ADMIN)
                .activo(true)
                .build();

        adminUserRepository.save(admin);
    }

    private void seedProcesoYModalidad() {
        if (!procesoAdmisionRepository.existsByCodigo("2026-1")) {
            procesoAdmisionRepository.save(ProcesoAdmision.builder()
                    .codigo("2026-1")
                    .nombre("PROCESO DE ADMISION 2026-1 ORDINARIO")
                    .descripcion("Proceso ordinario de admision 2026-1")
                    .activo(true)
                    .build());
        }

        if (!modalidadAdmisionRepository.existsByNombre("ADMISION ORDINARIA")) {
            modalidadAdmisionRepository.save(ModalidadAdmision.builder()
                    .nombre("ADMISION ORDINARIA")
                    .descripcion("Modalidad ordinaria para postulantes de pregrado")
                    .montoBase(new BigDecimal("400.00"))
                    .activo(true)
                    .build());
        }
    }

    private void seedCatalogoAcademico() {
        seedArea("A", "AREA A - CIENCIAS DE LA SALUD", List.of(
                "BIOLOGIA",
                "ENFERMERIA",
                "FARMACIA Y BIOQUIMICA",
                "MEDICINA HUMANA",
                "MEDICINA VETERINARIA Y ZOOTECNIA",
                "OBSTETRICIA",
                "ODONTOLOGIA",
                "PSICOLOGIA"));

        seedArea("B", "AREA B - CIENCIAS SOCIALES Y HUMANIDADES", List.of(
                "ADMINISTRACION",
                "ARQUEOLOGIA",
                "CIENCIAS DE LA COMUNICACION",
                "TURISMO",
                "CIENCIAS DE LA EDUCACION EN CIENCIAS BIOLOGICAS Y QUIMICA",
                "CIENCIAS DE LA EDUCACION EN EDUCACION ARTISTICA",
                "CIENCIAS DE LA EDUCACION EN EDUCACION FISICA",
                "CIENCIAS DE LA EDUCACION EN EDUCACION INICIAL",
                "CIENCIAS DE LA EDUCACION EN EDUCACION PRIMARIA",
                "CIENCIAS DE LA EDUCACION EN FILOSOFIA, PSICOLOGIA Y CIENCIAS SOCIALES",
                "CIENCIAS DE LA EDUCACION EN HISTORIA Y GEOGRAFIA",
                "CIENCIAS DE LA EDUCACION EN LENGUA Y LITERATURA",
                "CIENCIAS DE LA EDUCACION EN MATEMATICA E INFORMATICA",
                "ECONOMIA",
                "NEGOCIOS INTERNACIONALES",
                "CONTABILIDAD",
                "DERECHO"));

        seedArea("C", "AREA C - CIENCIAS E INGENIERIA", List.of(
                "AGRONOMIA",
                "ARQUITECTURA",
                "ESTADISTICA",
                "FISICA",
                "MATEMATICA E INFORMATICA",
                "INGENIERIA AMBIENTAL Y SANITARIA",
                "INGENIERIA CIVIL",
                "INGENIERIA DE MINAS",
                "INGENIERIA METALURGICA",
                "INGENIERIA DE SISTEMAS",
                "INGENIERIA ELECTRONICA",
                "INGENIERIA MECANICA ELECTRICA",
                "INGENIERIA DE ALIMENTOS",
                "INGENIERIA PESQUERA",
                "INGENIERIA QUIMICA"));
    }

    private void seedArea(String codigo, String nombre, List<String> escuelas) {
        AreaAcademica area = areaAcademicaRepository.findByCodigo(codigo)
                .orElseGet(() -> areaAcademicaRepository.save(AreaAcademica.builder()
                        .codigo(codigo)
                        .nombre(nombre)
                        .activo(true)
                        .build()));

        for (String nombreEscuela : escuelas) {
            EscuelaProfesional escuela = escuelaProfesionalRepository
                    .findByAreaAcademicaCodigoAndNombre(codigo, nombreEscuela)
                    .orElseGet(() -> escuelaProfesionalRepository.save(EscuelaProfesional.builder()
                            .areaAcademica(area)
                            .nombre(nombreEscuela)
                            .activo(true)
                            .build()));

            programaAcademicoRepository.findByEscuelaProfesionalIdAndNombre(escuela.getId(), nombreEscuela)
                    .orElseGet(() -> programaAcademicoRepository.save(ProgramaAcademico.builder()
                            .escuelaProfesional(escuela)
                            .nombre(nombreEscuela)
                            .activo(true)
                            .build()));
        }
    }
}
