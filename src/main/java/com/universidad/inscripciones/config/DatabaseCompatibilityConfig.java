package com.universidad.inscripciones.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DatabaseCompatibilityConfig implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        eliminarUniquePagoBancarioSiExiste();
        actualizarCheckEstadoInscripcion();
    }

    private void eliminarUniquePagoBancarioSiExiste() {
        String sql = """
                do $$
                declare
                    constraint_record record;
                begin
                    for constraint_record in
                        select distinct n.nspname, c.conname
                        from pg_constraint c
                        join pg_class t on t.oid = c.conrelid
                        join pg_namespace n on n.oid = t.relnamespace
                        join pg_attribute a on a.attrelid = t.oid and a.attnum = any(c.conkey)
                        where n.nspname = current_schema()
                            and t.relname = 'inscripciones'
                            and c.contype = 'u'
                            and a.attname = 'pago_bancario_id'
                    loop
                        execute format(
                            'alter table %I.inscripciones drop constraint %I',
                            constraint_record.nspname,
                            constraint_record.conname
                        );
                    end loop;
                end $$;
                """;

        jdbcTemplate.execute(sql);
    }

    private void actualizarCheckEstadoInscripcion() {
        String sql = """
                do $$
                declare
                    constraint_record record;
                begin
                    for constraint_record in
                        select n.nspname, c.conname
                        from pg_constraint c
                        join pg_class t on t.oid = c.conrelid
                        join pg_namespace n on n.oid = t.relnamespace
                        left join pg_attribute a on a.attrelid = t.oid and a.attnum = any(c.conkey)
                        where n.nspname = current_schema()
                            and t.relname = 'inscripciones'
                            and c.contype = 'c'
                            and (
                                a.attname = 'estado'
                                or pg_get_constraintdef(c.oid) ilike '%estado%'
                            )
                    loop
                        execute format(
                            'alter table %I.inscripciones drop constraint %I',
                            constraint_record.nspname,
                            constraint_record.conname
                        );
                    end loop;

                    alter table inscripciones
                        add constraint inscripciones_estado_check
                        check (estado in ('BORRADOR', 'REGISTRADA', 'APROBADA', 'ANULADA'));
                end $$;
                """;

        jdbcTemplate.execute(sql);
    }
}
