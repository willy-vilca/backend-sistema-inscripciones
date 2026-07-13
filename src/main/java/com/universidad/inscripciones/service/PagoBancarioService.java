package com.universidad.inscripciones.service;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.universidad.inscripciones.dto.pago.PagoBancarioResponse;
import com.universidad.inscripciones.dto.pago.PagoImportacionResponse;
import com.universidad.inscripciones.dto.pago.PagoResumenResponse;
import com.universidad.inscripciones.model.entity.PagoBancario;
import com.universidad.inscripciones.repository.PagoBancarioRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PagoBancarioService {

    private static final int TAMANIO_BLOQUE_ADMIN = 100;

    private static final List<String> COLUMNAS_REQUERIDAS = List.of(
            "NOMBRE CLIENTE",
            "CODIGO",
            "DESCRIPCION DEL PAGO",
            "IMPORTE A PAGAR",
            "IMPORTE PAGADO",
            "OFICINA",
            "NRO MOVIMIENTO",
            "FECHA PAGO",
            "FECHA PROCESO",
            "FORMA PAGO",
            "CANAL");

    private final PagoBancarioRepository pagoBancarioRepository;
    private final DataFormatter dataFormatter = new DataFormatter(Locale.US);

    @Transactional(readOnly = true)
    public List<PagoBancarioResponse> listarPagosAdministracion(String busqueda, String estado, int bloque) {
        String busquedaLimpia = busqueda == null || busqueda.isBlank() ? null : busqueda.trim();
        Boolean usado = obtenerFiltroUsado(estado);
        int bloqueSeguro = Math.max(bloque, 0);
        PageRequest pagina = PageRequest.of(bloqueSeguro, TAMANIO_BLOQUE_ADMIN);

        List<PagoBancario> pagos = obtenerPagosAdministracion(busquedaLimpia, usado, pagina);

        return pagos
                .stream()
                .map(PagoBancarioResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public PagoResumenResponse obtenerResumen() {
        long total = pagoBancarioRepository.count();
        long usados = pagoBancarioRepository.countByUsado(true);
        return new PagoResumenResponse(total, total - usados, usados);
    }

    @Transactional
    public PagoImportacionResponse importarExcel(MultipartFile archivo) {
        validarArchivo(archivo);

        int filasLeidas = 0;
        int duplicados = 0;
        int omitidos = 0;
        int actualizados = 0;
        List<PagoBancario> pagosCandidatos = new ArrayList<>();
        Set<String> movimientosArchivo = new HashSet<>();

        try (InputStream inputStream = archivo.getInputStream();
                Workbook workbook = WorkbookFactory.create(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);
            Row header = sheet.getRow(sheet.getFirstRowNum());
            Map<String, Integer> columnas = obtenerColumnas(header);
            validarColumnas(columnas);

            for (int rowIndex = sheet.getFirstRowNum() + 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null || filaVacia(row)) {
                    continue;
                }

                filasLeidas++;
                String nroMovimiento = texto(row, columnas, "NRO MOVIMIENTO");

                if (nroMovimiento == null || nroMovimiento.isBlank()) {
                    omitidos++;
                    continue;
                }

                if (!movimientosArchivo.add(nroMovimiento)) {
                    duplicados++;
                    continue;
                }

                PagoBancario pago = PagoBancario.builder()
                        .nombreCliente(texto(row, columnas, "NOMBRE CLIENTE"))
                        .codigo(texto(row, columnas, "CODIGO"))
                        .descripcionPago(texto(row, columnas, "DESCRIPCION DEL PAGO"))
                        .importeAPagar(decimal(row, columnas, "IMPORTE A PAGAR"))
                        .importePagado(decimal(row, columnas, "IMPORTE PAGADO"))
                        .oficina(texto(row, columnas, "OFICINA"))
                        .nroMovimiento(nroMovimiento)
                        .fechaPago(fechaHora(row, columnas, "FECHA PAGO"))
                        .fechaProceso(fecha(row, columnas, "FECHA PROCESO"))
                        .formaPago(texto(row, columnas, "FORMA PAGO"))
                        .canal(texto(row, columnas, "CANAL"))
                        .archivoOrigen(archivo.getOriginalFilename())
                        .usado(false)
                        .build();

                pagosCandidatos.add(pago);
            }

            Map<String, PagoBancario> pagosExistentes = pagoBancarioRepository.findByNroMovimientoIn(movimientosArchivo)
                    .stream()
                    .collect(java.util.stream.Collectors.toMap(
                            PagoBancario::getNroMovimiento,
                            pago -> pago));

            List<PagoBancario> pagosNuevos = pagosCandidatos.stream()
                    .filter(pago -> !pagosExistentes.containsKey(pago.getNroMovimiento()))
                    .toList();

            duplicados += pagosCandidatos.size() - pagosNuevos.size();

            List<PagoBancario> pagosParaActualizar = new ArrayList<>();
            for (PagoBancario candidato : pagosCandidatos) {
                PagoBancario existente = pagosExistentes.get(candidato.getNroMovimiento());
                if (existente != null && completarDatosFaltantes(existente, candidato)) {
                    pagosParaActualizar.add(existente);
                }
            }

            actualizados = pagosParaActualizar.size();
            pagoBancarioRepository.saveAll(pagosNuevos);
            pagoBancarioRepository.saveAll(pagosParaActualizar);

            return new PagoImportacionResponse(
                    archivo.getOriginalFilename(),
                    filasLeidas,
                    pagosNuevos.size(),
                    actualizados,
                    duplicados,
                    omitidos);
        } catch (IOException ex) {
            throw new IllegalArgumentException("No se pudo leer el archivo Excel.", ex);
        }
    }

    private void validarArchivo(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new IllegalArgumentException("Debe seleccionar un archivo Excel.");
        }

        String nombre = archivo.getOriginalFilename();
        if (nombre == null || !(nombre.toLowerCase().endsWith(".xlsx") || nombre.toLowerCase().endsWith(".xls"))) {
            throw new IllegalArgumentException("El archivo debe tener formato .xlsx o .xls.");
        }
    }

    private Boolean obtenerFiltroUsado(String estado) {
        if (estado == null || estado.isBlank() || "TODOS".equalsIgnoreCase(estado)) {
            return null;
        }

        if ("DISPONIBLE".equalsIgnoreCase(estado)) {
            return false;
        }

        if ("USADO".equalsIgnoreCase(estado)) {
            return true;
        }

        return null;
    }

    private List<PagoBancario> obtenerPagosAdministracion(String busqueda, Boolean usado, PageRequest pagina) {
        if (busqueda == null && usado == null) {
            return pagoBancarioRepository.listarParaAdministracion(pagina);
        }

        if (busqueda == null) {
            return pagoBancarioRepository.listarParaAdministracionPorEstado(usado, pagina);
        }

        return pagoBancarioRepository.buscarParaAdministracionConBusqueda(busqueda, usado, pagina);
    }

    private Map<String, Integer> obtenerColumnas(Row header) {
        if (header == null) {
            throw new IllegalArgumentException("El archivo no contiene una fila de encabezados.");
        }

        Map<String, Integer> columnas = new HashMap<>();
        for (Cell cell : header) {
            String nombre = normalizar(celdaTexto(cell));
            if (!nombre.isBlank()) {
                columnas.put(nombre, cell.getColumnIndex());
            }
        }
        return columnas;
    }

    private void validarColumnas(Map<String, Integer> columnas) {
        List<String> faltantes = COLUMNAS_REQUERIDAS.stream()
                .filter(columna -> !columnas.containsKey(columna))
                .toList();

        if (!faltantes.isEmpty()) {
            throw new IllegalArgumentException("Faltan columnas requeridas: " + String.join(", ", faltantes));
        }
    }

    private boolean filaVacia(Row row) {
        for (Cell cell : row) {
            if (!celdaTexto(cell).isBlank()) {
                return false;
            }
        }
        return true;
    }

    private String texto(Row row, Map<String, Integer> columnas, String columna) {
        Integer index = columnas.get(columna);
        if (index == null) {
            return null;
        }
        return limpiar(celdaTexto(row.getCell(index)));
    }

    private BigDecimal decimal(Row row, Map<String, Integer> columnas, String columna) {
        Cell cell = celda(row, columnas, columna);
        if (cell == null) {
            return null;
        }

        if (cell.getCellType() == CellType.NUMERIC) {
            return BigDecimal.valueOf(cell.getNumericCellValue()).setScale(2, RoundingMode.HALF_UP);
        }

        if (cell.getCellType() == CellType.FORMULA && cell.getCachedFormulaResultType() == CellType.NUMERIC) {
            return BigDecimal.valueOf(cell.getNumericCellValue()).setScale(2, RoundingMode.HALF_UP);
        }

        String valor = limpiar(celdaTexto(cell));
        if (valor == null || valor.isBlank()) {
            return null;
        }

        String normalizado = normalizarDecimal(valor);
        if (normalizado.isBlank() || "-".equals(normalizado)) {
            return null;
        }

        try {
            return new BigDecimal(normalizado).setScale(2, RoundingMode.HALF_UP);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private boolean completarDatosFaltantes(PagoBancario existente, PagoBancario candidato) {
        boolean actualizado = false;

        if (existente.getImporteAPagar() == null && candidato.getImporteAPagar() != null) {
            existente.setImporteAPagar(candidato.getImporteAPagar());
            actualizado = true;
        }

        if (existente.getImportePagado() == null && candidato.getImportePagado() != null) {
            existente.setImportePagado(candidato.getImportePagado());
            actualizado = true;
        }

        if (existente.getDescripcionPago() == null && candidato.getDescripcionPago() != null) {
            existente.setDescripcionPago(candidato.getDescripcionPago());
            actualizado = true;
        }

        if (existente.getNombreCliente() == null && candidato.getNombreCliente() != null) {
            existente.setNombreCliente(candidato.getNombreCliente());
            actualizado = true;
        }

        return actualizado;
    }

    private String normalizarDecimal(String valor) {
        String limpio = valor
                .replace("S/.", "")
                .replace("S/", "")
                .replace("PEN", "")
                .replaceAll("[^0-9,.-]", "")
                .trim();

        int ultimaComa = limpio.lastIndexOf(',');
        int ultimoPunto = limpio.lastIndexOf('.');

        if (ultimaComa >= 0 && ultimoPunto >= 0) {
            if (ultimaComa > ultimoPunto) {
                return limpio.replace(".", "").replace(",", ".");
            }
            return limpio.replace(",", "");
        }

        if (ultimaComa >= 0) {
            return limpio.replace(",", ".");
        }

        return limpio;
    }

    private LocalDateTime fechaHora(Row row, Map<String, Integer> columnas, String columna) {
        Cell cell = celda(row, columnas, columna);
        if (cell == null) {
            return null;
        }

        if (esCeldaFechaExcel(cell)) {
            return cell.getLocalDateTimeCellValue();
        }

        String valor = limpiar(celdaTexto(cell));
        if (valor == null || valor.isBlank()) {
            return null;
        }

        List<DateTimeFormatter> formatos = List.of(
                DateTimeFormatter.ofPattern("yyyyMMdd HH:mm:ss"),
                DateTimeFormatter.ofPattern("yyyyMMdd H:mm:ss"),
                DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"),
                DateTimeFormatter.ofPattern("dd/MM/yyyy H:mm:ss"));

        for (DateTimeFormatter formato : formatos) {
            try {
                return LocalDateTime.parse(valor, formato);
            } catch (DateTimeParseException ignored) {
                // Se prueba el siguiente formato conocido.
            }
        }

        LocalDate soloFecha = parseLocalDate(valor);
        return soloFecha != null ? LocalDateTime.of(soloFecha, LocalTime.MIDNIGHT) : null;
    }

    private LocalDate fecha(Row row, Map<String, Integer> columnas, String columna) {
        Cell cell = celda(row, columnas, columna);
        if (cell == null) {
            return null;
        }

        if (esCeldaFechaExcel(cell)) {
            return cell.getLocalDateTimeCellValue().toLocalDate();
        }

        return parseLocalDate(limpiar(celdaTexto(cell)));
    }

    private LocalDate parseLocalDate(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }

        List<DateTimeFormatter> formatos = List.of(
                DateTimeFormatter.ofPattern("yyyyMMdd"),
                DateTimeFormatter.ofPattern("dd/MM/yyyy"),
                DateTimeFormatter.ISO_LOCAL_DATE);

        for (DateTimeFormatter formato : formatos) {
            try {
                return LocalDate.parse(valor, formato);
            } catch (DateTimeParseException ignored) {
                // Se prueba el siguiente formato conocido.
            }
        }
        return null;
    }

    private Cell celda(Row row, Map<String, Integer> columnas, String columna) {
        Integer index = columnas.get(columna);
        return index == null ? null : row.getCell(index);
    }

    private boolean esCeldaFechaExcel(Cell cell) {
        if (cell == null) {
            return false;
        }

        CellType type = cell.getCellType();
        if (type == CellType.NUMERIC) {
            return DateUtil.isCellDateFormatted(cell);
        }

        if (type == CellType.FORMULA && cell.getCachedFormulaResultType() == CellType.NUMERIC) {
            return DateUtil.isCellDateFormatted(cell);
        }

        return false;
    }

    private String celdaTexto(Cell cell) {
        if (cell == null) {
            return "";
        }
        return dataFormatter.formatCellValue(cell);
    }

    private String limpiar(String valor) {
        if (valor == null) {
            return null;
        }
        String limpio = valor.trim();
        if (limpio.startsWith("'")) {
            limpio = limpio.substring(1);
        }
        return limpio.trim();
    }

    private String normalizar(String valor) {
        String sinAcentos = Normalizer.normalize(valor == null ? "" : valor, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return sinAcentos
                .replace(".", "")
                .trim()
                .replaceAll("\\s+", " ")
                .toUpperCase(Locale.ROOT);
    }
}
