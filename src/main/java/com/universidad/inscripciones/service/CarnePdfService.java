package com.universidad.inscripciones.service;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import javax.imageio.ImageIO;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.Barcode128;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfWriter;
import com.universidad.inscripciones.dto.inscripcion.CarnePdfDownload;
import com.universidad.inscripciones.model.entity.Inscripcion;
import com.universidad.inscripciones.model.entity.Postulante;
import com.universidad.inscripciones.repository.InscripcionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CarnePdfService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final Color RED = new Color(185, 28, 28);
    private static final Color GOLD = new Color(245, 158, 11);
    private static final Color BLUE = new Color(30, 64, 175);
    private static final Color GRAY = new Color(75, 85, 99);

    private final InscripcionRepository inscripcionRepository;

    @Value("${app.upload-dir}")
    private String uploadDir;

    public String generarCarne(Inscripcion inscripcion) {
        Path uploadRoot = uploadRoot();
        String folder = "inscripciones/" + inscripcion.getCodigoPostulante();
        String filename = "carne-" + inscripcion.getCodigoPostulante() + ".pdf";
        Path targetFolder = uploadRoot.resolve(folder).normalize();
        Path target = targetFolder.resolve(filename).normalize();

        if (!target.startsWith(uploadRoot)) {
            throw new IllegalArgumentException("Ruta de carne no valida.");
        }

        try {
            Files.createDirectories(targetFolder);
            try (OutputStream outputStream = Files.newOutputStream(target)) {
                Document document = new Document(PageSize.A4.rotate(), 26, 26, 28, 28);
                PdfWriter writer = PdfWriter.getInstance(document, outputStream);
                document.open();

                drawCarnePage(document, writer, inscripcion);
                document.setPageSize(PageSize.A4);
                document.newPage();
                drawDeclaracionPage(document, writer, inscripcion);

                document.close();
            }

            return uploadRoot.relativize(target).toString().replace("\\", "/");
        } catch (DocumentException | IOException ex) {
            throw new IllegalArgumentException("No se pudo generar el carne del postulante.", ex);
        }
    }

    @Transactional
    public CarnePdfDownload obtenerCarne(Long inscripcionId) {
        Inscripcion inscripcion = inscripcionRepository.buscarDetalleAdmin(inscripcionId)
                .orElseThrow(() -> new IllegalArgumentException("Inscripcion no encontrada."));

        String carnePath = generarCarne(inscripcion);
        inscripcion.setCarnePdfPath(carnePath);

        try {
            Path file = uploadRoot().resolve(carnePath).normalize();
            if (!file.startsWith(uploadRoot()) || !Files.exists(file)) {
                throw new IllegalArgumentException("No se encontro el archivo del carne.");
            }

            Resource resource = new UrlResource(file.toUri());
            return new CarnePdfDownload(
                    "carne-" + inscripcion.getCodigoPostulante() + ".pdf",
                    resource);
        } catch (IOException ex) {
            throw new IllegalArgumentException("No se pudo abrir el archivo del carne.", ex);
        }
    }

    private void drawCarnePage(Document document, PdfWriter writer, Inscripcion inscripcion) throws DocumentException {
        PdfContentByte canvas = writer.getDirectContent();
        Rectangle page = document.getPageSize();
        float margin = 32f;
        float separatorX = page.getWidth() / 2f;
        float copyWidth = (page.getWidth() - (margin * 2f) - 22f) / 2f;
        float top = page.getHeight() - 42f;
        float leftOne = margin;
        float leftTwo = separatorX + 14f;

        canvas.setLineWidth(1.2f);
        canvas.moveTo(separatorX, 32f);
        canvas.lineTo(separatorX, page.getHeight() - 32f);
        canvas.stroke();

        drawCarneCopy(canvas, writer, inscripcion, leftOne, top, copyWidth, true);
        drawCarneCopy(canvas, writer, inscripcion, leftTwo, top, copyWidth, false);
    }

    private void drawCarneCopy(
            PdfContentByte canvas,
            PdfWriter writer,
            Inscripcion inscripcion,
            float left,
            float top,
            float width,
            boolean universityCopy) throws DocumentException {

        Postulante postulante = inscripcion.getPostulante();
        Font headerFont = font(11, Font.BOLD, Color.BLACK);
        Font redFont = font(13, Font.BOLD, RED);
        Font smallBold = font(7.5f, Font.BOLD, Color.BLACK);
        Font small = font(7.2f, Font.NORMAL, Color.BLACK);
        Font tiny = font(6.2f, Font.NORMAL, Color.BLACK);

        drawLogo(canvas, left, top - 24f);
        writeText(canvas, "Universidad Nacional", left + 34f, top - 8f, font(8, Font.BOLD, GOLD), Element.ALIGN_LEFT);
        writeText(canvas, "SISTEMA DE INSCRIPCIONES", left + 34f, top - 22f, redFont, Element.ALIGN_LEFT);
        writeText(canvas, "DIRECCION DE ADMISION", left + width / 2f, top - 8f, headerFont, Element.ALIGN_CENTER);
        writeText(canvas, "CARNE DE POSTULANTE - DECLARACION JURADA DE VERACIDAD DE INFORMACION",
                left + width / 2f, top - 36f, smallBold, Element.ALIGN_CENTER);
        drawOcaBadge(canvas, left + width - 52f, top - 24f);

        float dataTop = top - 80f;
        float labelX = left + 4f;
        float valueX = left + 122f;
        float line = 15f;

        drawField(canvas, "CODIGO DEL POSTULANTE:", inscripcion.getCodigoPostulante(), labelX, valueX, dataTop, small, smallBold);
        drawField(canvas, "APELLIDO PATERNO:", text(postulante.getApellidoPaterno()), labelX, valueX, dataTop - line, small, smallBold);
        drawField(canvas, "APELLIDO MATERNO:", text(postulante.getApellidoMaterno()), labelX, valueX, dataTop - (line * 2), small, smallBold);
        drawField(canvas, "NOMBRES:", text(postulante.getNombres()), labelX, valueX, dataTop - (line * 3), small, smallBold);
        drawField(canvas, "MODALIDAD:", text(inscripcion.getModalidadAdmision().getNombre()), labelX, valueX, dataTop - (line * 4), small, smallBold);
        drawField(canvas, "CARRERA PROFESIONAL:", text(carrera(inscripcion)), labelX, valueX, dataTop - (line * 5), small, smallBold);
        drawField(canvas, "FECHA DE EXAMEN:", "Por confirmar", labelX, valueX, dataTop - (line * 6.4f), small, smallBold);

        drawPhoto(canvas, postulante.getFotoPath(), left + width - 82f, dataTop - 70f, 66f, 82f);
        drawVerticalBox(canvas, left + width - 12f, dataTop - 106f, universityCopy ? "PARA LA UNIVERSIDAD" : "PARA EL POSTULANTE");

        float declarationY = dataTop - 120f;
        writeText(canvas, "DECLARACION JURADA", labelX, declarationY, smallBold, Element.ALIGN_LEFT);
        String[] declaration = {
                "La informacion consignada al momento de inscribirme es verdadera y de mi entera responsabilidad.",
                "Conozco y acepto todas las disposiciones del Reglamento de Admision, al cual me someto.",
                "En caso de alcanzar una vacante, me comprometo a cumplir con lo dispuesto en el Reglamento de Admision."
        };
        float bulletY = declarationY - 16f;
        for (String item : declaration) {
            writeText(canvas, "- " + item, labelX, bulletY, tiny, Element.ALIGN_LEFT);
            bulletY -= 10f;
        }

        float dayY = bulletY - 18f;
        writeText(canvas, "DIA DEL EXAMEN", labelX, dayY, smallBold, Element.ALIGN_LEFT);
        String[] examNotes = {
                "Presentarse con este carne en el local que le corresponda para rendir su Examen de Admision.",
                "Portar el DNI original. Los extranjeros presentaran su pasaporte o carne de extranjeria.",
                "La firma e impresion dactilar se realizara en el aula asignada.",
                "Debera traer lapiz, borrador y tajador."
        };
        float noteY = dayY - 16f;
        for (String item : examNotes) {
            writeText(canvas, "- " + item, labelX, noteY, tiny, Element.ALIGN_LEFT);
            noteY -= 10f;
        }

        drawQrPlaceholder(canvas, labelX + 28f, 88f, 46f);
        drawBarcode(canvas, inscripcion.getCodigoPostulante(), labelX + 18f, 42f, 90f, 24f);
        writeText(canvas, "No tocar esta area", labelX + 58f, 34f, tiny, Element.ALIGN_CENTER);

        drawSignatureLine(canvas, left + width - 230f, 108f, "FIRMA DEL POSTULANTE");
        drawSignatureLine(canvas, left + width - 230f, 58f, "FIRMA DEL DOCENTE");
        drawFingerprintBoxes(canvas, left + width - 68f, 70f);
        drawSmallGrid(canvas, left + width - 240f, 20f, "FACULTAD");
        drawSmallGrid(canvas, left + width - 160f, 20f, "AULA");
    }

    private void drawDeclaracionPage(Document document, PdfWriter writer, Inscripcion inscripcion) throws DocumentException {
        PdfContentByte canvas = writer.getDirectContent();
        Rectangle page = document.getPageSize();
        Postulante postulante = inscripcion.getPostulante();

        float left = 76f;
        float top = page.getHeight() - 60f;
        Font title = font(12, Font.BOLD, Color.BLACK);
        Font normal = font(9, Font.NORMAL, Color.BLACK);
        Font bold = font(9, Font.BOLD, Color.BLACK);

        drawLogo(canvas, left, top - 20f);
        writeText(canvas, "Universidad Nacional", left + 36f, top - 6f, font(8, Font.BOLD, GOLD), Element.ALIGN_LEFT);
        writeText(canvas, "SISTEMA DE INSCRIPCIONES", left + 36f, top - 20f, font(12, Font.BOLD, RED), Element.ALIGN_LEFT);
        writeText(canvas, "OFICINA CENTRAL DE ADMISION", page.getWidth() / 2f, top - 6f, title, Element.ALIGN_CENTER);
        writeText(canvas, text(inscripcion.getProcesoAdmision().getNombre()).toUpperCase(), page.getWidth() / 2f, top - 30f, bold, Element.ALIGN_CENTER);
        writeText(canvas, "DECLARACION JURADA DE NO TENER", page.getWidth() / 2f, top - 52f, title, Element.ALIGN_CENTER);
        writeText(canvas, "ANTECEDENTES PENALES", page.getWidth() / 2f, top - 74f, title, Element.ALIGN_CENTER);

        float dataY = top - 140f;
        writeText(canvas, "Por la presente, el suscrito:", left + 6f, dataY, normal, Element.ALIGN_LEFT);
        drawField(canvas, "Apellidos", apellidos(postulante), left + 6f, left + 120f, dataY - 18f, normal, normal);
        drawField(canvas, "Nombres(s)", text(postulante.getNombres()), left + 6f, left + 120f, dataY - 36f, normal, normal);
        drawField(canvas, "DNI", text(postulante.getNumeroDocumento()), left + 6f, left + 120f, dataY - 54f, normal, normal);
        drawField(canvas, "Fecha de Nacimiento", formatDate(postulante.getFechaNacimiento()), left + 6f, left + 120f, dataY - 72f, normal, normal);
        drawField(canvas, "Modalidad", text(inscripcion.getModalidadAdmision().getNombre()), left + 6f, left + 120f, dataY - 90f, normal, normal);

        String paragraph = "DECLARO BAJO JURAMENTO QUE NO REGISTRO ANTECEDENTES PENALES NI JUDICIALES. "
                + "Esta declaracion se formula en aplicacion del principio de veracidad establecido en el Texto Unico "
                + "Ordenado de la Ley del Procedimiento Administrativo General; y asumo, de corresponder, la responsabilidad "
                + "administrativa, civil y/o penal cuando por cualquier accion de verificacion se compruebe la falsedad "
                + "o inexactitud de la presente declaracion jurada.";
        drawParagraph(canvas, paragraph, left, dataY - 160f, page.getWidth() - 150f, 80f, normal);

        writeText(canvas, "En senal de conformidad, firmo a continuacion.", left + 6f, dataY - 250f, normal, Element.ALIGN_LEFT);
        writeText(canvas, "Ica, " + formatDate(LocalDate.now()), page.getWidth() - 120f, dataY - 310f, normal, Element.ALIGN_CENTER);
        drawRectangle(canvas, page.getWidth() - 222f, dataY - 422f, 78f, 104f);
        drawSignatureLine(canvas, page.getWidth() / 2f - 94f, dataY - 470f, "Firma del Postulante");
        writeText(canvas, postulante.getTipoDocumento().name() + " Nro " + text(postulante.getNumeroDocumento()),
                page.getWidth() / 2f, dataY - 496f, normal, Element.ALIGN_CENTER);
    }

    private void drawParagraph(PdfContentByte canvas, String text, float x, float y, float width, float height, Font font) throws DocumentException {
        ColumnText column = new ColumnText(canvas);
        column.setSimpleColumn(new Phrase(text, font), x, y - height, x + width, y, 12f, Element.ALIGN_JUSTIFIED);
        column.go();
    }

    private void drawLogo(PdfContentByte canvas, float x, float y) {
        canvas.saveState();
        canvas.setColorStroke(RED);
        canvas.setColorFill(new Color(254, 243, 199));
        canvas.circle(x + 14f, y + 14f, 14f);
        canvas.fillStroke();
        canvas.setColorFill(RED);
        canvas.rectangle(x + 7f, y + 5f, 14f, 18f);
        canvas.fill();
        canvas.setColorFill(Color.WHITE);
        canvas.circle(x + 14f, y + 14f, 4f);
        canvas.fill();
        canvas.restoreState();
    }

    private void drawOcaBadge(PdfContentByte canvas, float x, float y) {
        canvas.saveState();
        canvas.setColorStroke(BLUE);
        canvas.setLineWidth(1f);
        canvas.rectangle(x, y, 42f, 22f);
        canvas.stroke();
        writeText(canvas, "OCA", x + 21f, y + 7f, font(12, Font.BOLD, BLUE), Element.ALIGN_CENTER);
        canvas.restoreState();
    }

    private void drawPhoto(PdfContentByte canvas, String relativePath, float x, float y, float width, float height) {
        drawRectangle(canvas, x, y, width, height);
        if (relativePath == null || relativePath.isBlank()) {
            return;
        }

        try {
            Path photo = uploadRoot().resolve(relativePath).normalize();
            if (!photo.startsWith(uploadRoot()) || !Files.exists(photo)) {
                return;
            }
            Image image = loadPhotoImage(photo);
            image.scaleToFit(width - 2f, height - 2f);
            image.setAbsolutePosition(x + ((width - image.getScaledWidth()) / 2f), y + ((height - image.getScaledHeight()) / 2f));
            canvas.addImage(image);
        } catch (Exception ignored) {
            writeText(canvas, "FOTO", x + width / 2f, y + height / 2f, font(8, Font.BOLD, GRAY), Element.ALIGN_CENTER);
        }
    }

    private Image loadPhotoImage(Path photo) throws IOException, DocumentException {
        try {
            return Image.getInstance(photo.toAbsolutePath().toString());
        } catch (Exception ex) {
            BufferedImage source = ImageIO.read(photo.toFile());
            if (source == null) {
                throw new IOException("Formato de fotografia no compatible.");
            }

            BufferedImage rgbImage = new BufferedImage(
                    source.getWidth(),
                    source.getHeight(),
                    BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = rgbImage.createGraphics();
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, rgbImage.getWidth(), rgbImage.getHeight());
            graphics.drawImage(source, 0, 0, null);
            graphics.dispose();

            try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
                ImageIO.write(rgbImage, "jpg", output);
                return Image.getInstance(output.toByteArray());
            }
        }
    }

    private void drawVerticalBox(PdfContentByte canvas, float x, float y, String label) {
        drawRectangle(canvas, x, y, 16f, 112f);
        canvas.saveState();
        canvas.beginText();
        canvas.setFontAndSize(baseFont(), 6f);
        canvas.setTextMatrix(0, 1, -1, 0, x + 11f, y + 12f);
        canvas.showText(label);
        canvas.endText();
        canvas.restoreState();
    }

    private void drawQrPlaceholder(PdfContentByte canvas, float x, float y, float size) {
        drawRectangle(canvas, x, y, size, size);
        float cell = size / 7f;
        canvas.saveState();
        canvas.setColorFill(Color.BLACK);
        for (int row = 0; row < 7; row++) {
            for (int col = 0; col < 7; col++) {
                boolean mark = row == 0 || col == 0 || row == 6 || col == 6 || (row + col) % 3 == 0;
                if (mark) {
                    canvas.rectangle(x + (col * cell) + 1f, y + (row * cell) + 1f, cell - 2f, cell - 2f);
                }
            }
        }
        canvas.fill();
        canvas.restoreState();
    }

    private void drawBarcode(PdfContentByte canvas, String code, float x, float y, float width, float height) {
        Barcode128 barcode = new Barcode128();
        barcode.setCode(code);
        barcode.setCodeType(Barcode128.CODE128);
        barcode.setBarHeight(height);
        barcode.setX(0.8f);
        Image image = barcode.createImageWithBarcode(canvas, Color.BLACK, Color.BLACK);
        image.scaleToFit(width, height);
        image.setAbsolutePosition(x, y);
        try {
            canvas.addImage(image);
        } catch (DocumentException ignored) {
            writeText(canvas, code, x + width / 2f, y + 8f, font(6, Font.NORMAL, Color.BLACK), Element.ALIGN_CENTER);
        }
    }

    private void drawSignatureLine(PdfContentByte canvas, float x, float y, String label) {
        canvas.saveState();
        canvas.setLineWidth(0.8f);
        canvas.moveTo(x, y);
        canvas.lineTo(x + 120f, y);
        canvas.stroke();
        canvas.restoreState();
        writeText(canvas, label, x + 60f, y - 9f, font(6.2f, Font.NORMAL, Color.BLACK), Element.ALIGN_CENTER);
    }

    private void drawFingerprintBoxes(PdfContentByte canvas, float x, float y) {
        drawRectangle(canvas, x, y + 44f, 56f, 40f);
        drawRectangle(canvas, x, y, 56f, 40f);
        drawRectangle(canvas, x, y - 44f, 56f, 40f);
        writeRotated(canvas, "Huella Al Recoger", x + 52f, y + 48f);
        writeRotated(canvas, "Huella Dia del Examen", x + 52f, y + 4f);
    }

    private void drawSmallGrid(PdfContentByte canvas, float x, float y, String label) {
        drawRectangle(canvas, x, y, 60f, 18f);
        for (int i = 1; i < 4; i++) {
            canvas.moveTo(x + (i * 15f), y);
            canvas.lineTo(x + (i * 15f), y + 18f);
        }
        canvas.stroke();
        writeText(canvas, label, x + 30f, y - 8f, font(6, Font.NORMAL, Color.BLACK), Element.ALIGN_CENTER);
    }

    private void drawField(PdfContentByte canvas, String label, String value, float labelX, float valueX, float y, Font labelFont, Font valueFont) {
        writeText(canvas, label, labelX, y, labelFont, Element.ALIGN_LEFT);
        writeText(canvas, value, valueX, y, valueFont, Element.ALIGN_LEFT);
    }

    private void drawRectangle(PdfContentByte canvas, float x, float y, float width, float height) {
        canvas.saveState();
        canvas.setLineWidth(0.8f);
        canvas.rectangle(x, y, width, height);
        canvas.stroke();
        canvas.restoreState();
    }

    private void writeText(PdfContentByte canvas, String text, float x, float y, Font font, int align) {
        Phrase phrase = new Phrase(text == null ? "" : text, font);
        com.lowagie.text.pdf.ColumnText.showTextAligned(canvas, align, phrase, x, y, 0f);
    }

    private void writeRotated(PdfContentByte canvas, String text, float x, float y) {
        canvas.saveState();
        canvas.beginText();
        canvas.setFontAndSize(baseFont(), 5.5f);
        canvas.setTextMatrix(0, 1, -1, 0, x, y);
        canvas.showText(text);
        canvas.endText();
        canvas.restoreState();
    }

    private Font font(float size, int style, Color color) {
        return new Font(Font.HELVETICA, size, style, color);
    }

    private BaseFont baseFont() {
        try {
            return BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
        } catch (DocumentException | IOException ex) {
            throw new IllegalArgumentException("No se pudo cargar la fuente del PDF.", ex);
        }
    }

    private String carrera(Inscripcion inscripcion) {
        if (inscripcion.getProgramaAcademico() != null) {
            return inscripcion.getProgramaAcademico().getNombre();
        }
        if (inscripcion.getEscuelaProfesional() != null) {
            return inscripcion.getEscuelaProfesional().getNombre();
        }
        return "Por definir";
    }

    private String apellidos(Postulante postulante) {
        return (text(postulante.getApellidoPaterno()) + " " + text(postulante.getApellidoMaterno())).trim();
    }

    private String formatDate(LocalDate date) {
        return date == null ? "" : date.format(DATE_FORMAT);
    }

    private String text(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private Path uploadRoot() {
        return Paths.get(uploadDir).toAbsolutePath().normalize();
    }
}
