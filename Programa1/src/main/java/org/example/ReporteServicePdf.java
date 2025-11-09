package org.example;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.PDPageContentStream;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementación de ReporteService que genera PDFs con Apache PDFBox.
 * - Lista de estudiantes (tabla).
 * - Estadística de matrícula (cursos/grupos).
 *
 * Carpeta de salida: ~/reportes_sistema/
 */
public class ReporteServicePdf implements ReporteService {

    private final CursoService cursoService;
    private final UsuarioService usuarioService;

    // Fuentes como constantes
    private static final PDType1Font HELVETICA = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
    private static final PDType1Font HELVETICA_BOLD = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

    public ReporteServicePdf(CursoService cursoService, UsuarioService usuarioService) {
        this.cursoService = cursoService;
        this.usuarioService = usuarioService;
    }

    // ──────────────────────────── API ────────────────────────────

    @Override
    public Path generarListaEstudiantes(LocalDate fechaVigencia, Scope scope) throws Exception {
        // Recopilar datos
        List<ItemAlumno> alumnos = recolectarAlumnos(fechaVigencia, scope);
        alumnos.sort(Comparator
                .comparing((ItemAlumno it) -> safe(it.est.getApellido1()))
                .thenComparing(it -> safe(it.est.getNombre()))
                .thenComparing(it -> safe(it.est.getApellido2()))
        );

        // Salida
        Path out = outPath("lista_estudiantes", fechaVigencia, scope, "pdf");

        // PDF
        try (PDDocument doc = new PDDocument()) {
            PDRectangle pageSize = PDRectangle.LETTER;
            float margin = 40f;
            float y = pageSize.getHeight() - margin;

            // Portada / encabezado
            PDPage page = new PDPage(pageSize);
            doc.addPage(page);
            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                y = drawTitle(cs, "Lista de Estudiantes", "Vigentes desde " + fechaVigencia, y);

                // Encabezados de tabla
                String[] headers = {"CursoID", "Curso", "Grupo", "Identificación", "Nombre completo", "Correo"};
                float[] widths = {60, 120, 50, 90, 170, 160}; // suma < ancho útil
                y = y - 16;
                y = drawTableHeader(cs, margin, y, headers, widths);

                // Filas (paginación simple)
                float rowHeight = 14f;
                for (ItemAlumno it : alumnos) {
                    String nombreCompleto = joinNames(it.est.getNombre(), it.est.getApellido1(), it.est.getApellido2());
                    String[] row = {
                            nz(it.idCurso), nz(it.nombreCurso),
                            String.valueOf(it.idGrupo),
                            nz(it.est.getIdUsuario()),
                            nz(nombreCompleto),
                            nz(it.est.getCorreo())
                    };
                    if (y - rowHeight < margin) {
                        cs.close();
                        page = new PDPage(pageSize);
                        doc.addPage(page);
                        y = pageSize.getHeight() - margin;
                        try (PDPageContentStream cs2 = new PDPageContentStream(doc, page)) {
                            y = drawTableHeader(cs2, margin, y, headers, widths);
                            y = drawRow(cs2, margin, y, row, widths, rowHeight);
                        }
                        y -= 0; // ya actualizado
                        continue;
                    }
                    y = drawRow(cs, margin, y, row, widths, rowHeight);
                }
            }

            doc.save(out.toFile());
        }

        return out;
    }

    @Override
    public Path generarEstadisticaMatricula(LocalDate fechaVigencia, Scope scope) throws Exception {
        // Conteo: CursoID -> (GrupoID -> cantidad)
        Map<String, String> cursoNombres = new LinkedHashMap<>();
        Map<String, Map<Integer, Integer>> conteo = new LinkedHashMap<>();

        for (Curso c : cursosFiltrados(cursoService.listarCursos(), scope)) {
            cursoNombres.put(c.getId(), c.getNombre());
            for (Grupo g : gruposFiltrados(c, scope, fechaVigencia)) {
                conteo.computeIfAbsent(c.getId(), __ -> new LinkedHashMap<>());
                conteo.get(c.getId()).merge(g.getIdGrupo(), g.getMatriculas().size(), Integer::sum);
            }
        }

        Path out = outPath("estadistica_matricula", fechaVigencia, scope, "pdf");

        try (PDDocument doc = new PDDocument()) {
            PDRectangle pageSize = PDRectangle.LETTER;
            float margin = 40f;
            float y = pageSize.getHeight() - margin;

            PDPage page = new PDPage(pageSize);
            doc.addPage(page);
            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                y = drawTitle(cs, "Estadística de Matrícula",
                        "Vigentes desde " + fechaVigencia, y);
                y -= 10;

                if (conteo.isEmpty()) {
                    writeLine(cs, "No hay datos para el alcance/fecha indicados.", margin, y, 12, HELVETICA);
                } else {
                    for (String idCurso : conteo.keySet()) {
                        String titulo = "Curso: " + idCurso + " - " + cursoNombres.getOrDefault(idCurso, "");
                        y = ensureSpace(doc, cs, pageSize, margin, y, 40); // nueva sección si no hay espacio
                        writeLine(cs, titulo, margin, y, 12, HELVETICA_BOLD);
                        y -= 12;

                        Map<Integer, Integer> mapG = conteo.get(idCurso);
                        int total = 0;
                        for (Map.Entry<Integer, Integer> e : mapG.entrySet()) {
                            y = ensureSpace(doc, cs, pageSize, margin, y, 16);
                            writeLine(cs, "  Grupo #" + e.getKey() + ": " + e.getValue() + " matriculados",
                                    margin, y, 12, HELVETICA);
                            y -= 14;
                            total += e.getValue();
                        }
                        y = ensureSpace(doc, cs, pageSize, margin, y, 16);
                        writeLine(cs, "  Total curso: " + total, margin, y, 12, HELVETICA_BOLD);
                        y -= 18;
                    }
                }
            }

            doc.save(out.toFile());
        }

        return out;
    }

    // ─────────────────────── Lógica de datos ───────────────────────

    private List<ItemAlumno> recolectarAlumnos(LocalDate fechaVigencia, Scope scope) {
        List<ItemAlumno> alumnos = new ArrayList<>();
        for (Curso c : cursosFiltrados(cursoService.listarCursos(), scope)) {
            for (Grupo g : gruposFiltrados(c, scope, fechaVigencia)) {
                for (Object m : g.getMatriculas()) {
                    Estudiante e = tryGetEstudianteFromMatricula(m);
                    if (e == null) continue;
                    alumnos.add(new ItemAlumno(c.getId(), c.getNombre(), g.getIdGrupo(), e));
                }
            }
        }
        return alumnos;
    }

    private List<Curso> cursosFiltrados(List<Curso> cursos, Scope scope) {
        if (scope == null || scope.tipo == ScopeType.TODOS) return cursos;
        if (scope.tipo == ScopeType.CURSO || scope.tipo == ScopeType.GRUPO) {
            return cursos.stream().filter(c -> c.getId().equals(scope.idCurso)).collect(Collectors.toList());
        }
        return cursos;
    }

    private List<Grupo> gruposFiltrados(Curso c, Scope scope, LocalDate fechaVig) {
        return c.grupos.stream()
                .filter(g -> !g.getFechaFinal().isBefore(fechaVig)) // vigente si fin >= fechaVig
                .filter(g -> scope == null
                        || scope.tipo == ScopeType.TODOS
                        || (scope.tipo == ScopeType.CURSO && c.getId().equals(scope.idCurso))
                        || (scope.tipo == ScopeType.GRUPO && c.getId().equals(scope.idCurso) && g.getIdGrupo() == scope.idGrupo))
                .collect(Collectors.toList());
    }

    private Estudiante tryGetEstudianteFromMatricula(Object matricula) {
        // 1) getEstudiante()
        Estudiante e = (Estudiante) tryInvoke(matricula, "getEstudiante");
        if (e != null) return e;
        // 2) getIdEstudiante() -> resolver contra UsuarioService
        Object id = tryInvoke(matricula, "getIdEstudiante");
        if (id != null) {
            String sid = String.valueOf(id);
            for (Estudiante x : usuarioService.listarEstudiantes()) {
                if (sid.equals(x.getIdUsuario())) return x;
            }
        }
        return null;
    }

    private Object tryInvoke(Object target, String getter) {
        try { var m = target.getClass().getMethod(getter); return m.invoke(target); }
        catch (Exception ignored) { return null; }
    }

    private static String nz(String s) { return s == null ? "" : s; }
    private static String safe(String s) { return s == null ? "" : s; }
    private static String joinNames(String n, String a1, String a2) {
        StringBuilder sb = new StringBuilder();
        if (n != null && !n.isBlank()) sb.append(n);
        if (a1 != null && !a1.isBlank()) sb.append(sb.length()>0?" ":"").append(a1);
        if (a2 != null && !a2.isBlank()) sb.append(" ").append(a2);
        return sb.toString();
    }

    private Path outPath(String baseName, LocalDate fecha, Scope scope, String ext) throws IOException {
        String suf = switch (scope.tipo) {
            case TODOS -> "todos";
            case CURSO -> "curso-" + scope.idCurso;
            case GRUPO -> "curso-" + scope.idCurso + "-grupo-" + scope.idGrupo;
        };
        String filename = baseName + "_" + suf + "_" + fecha + "." + ext;
        Path dir = Path.of(System.getProperty("user.home"), "reportes_sistema");
        Files.createDirectories(dir);
        return dir.resolve(filename);
    }

    // ─────────────────────── Helpers de PDF ───────────────────────

    private float drawTitle(PDPageContentStream cs, String title, String subtitle, float yTop) throws IOException {
        float x = 40f;
        writeLine(cs, title, x, yTop, 16, HELVETICA_BOLD);
        yTop -= 18;
        writeLine(cs, subtitle, x, yTop, 12, HELVETICA);
        return yTop - 10;
    }

    private float drawTableHeader(PDPageContentStream cs, float x, float y, String[] headers, float[] widths) throws IOException {
        // fondo simple (línea inferior)
        writeRow(cs, x, y, headers, widths, 12, HELVETICA_BOLD);
        drawLine(cs, x, y-2, x + sum(widths), y-2);
        return y - 16;
    }

    private float drawRow(PDPageContentStream cs, float x, float y, String[] cells, float[] widths, float rowHeight) throws IOException {
        writeRow(cs, x, y, cells, widths, 11, HELVETICA);
        return y - rowHeight;
    }

    private void writeRow(PDPageContentStream cs, float x, float y, String[] cells, float[] widths, int fontSize, PDType1Font font) throws IOException {
        float cx = x;
        for (int i = 0; i < cells.length; i++) {
            writeClipped(cs, cells[i], cx + 2, y, widths[i] - 4, fontSize, font);
            cx += widths[i];
        }
    }

    private void writeLine(PDPageContentStream cs, String text, float x, float y, int fontSize, PDType1Font font) throws IOException {
        cs.beginText();
        cs.setFont(font, fontSize);
        cs.newLineAtOffset(x, y);
        cs.showText(text == null ? "" : text);
        cs.endText();
    }

    private void writeClipped(PDPageContentStream cs, String text, float x, float y, float maxWidth, int fontSize, PDType1Font font) throws IOException {
        if (text == null) text = "";
        String clipped = clipToWidth(text, font, fontSize, maxWidth);
        writeLine(cs, clipped, x, y, fontSize, font);
    }

    private String clipToWidth(String s, PDType1Font font, int fontSize, float maxWidth) throws IOException {
        // recorta con "…" si no cabe
        String ell = "…";
        if (stringWidth(font, fontSize, s) <= maxWidth) return s;
        for (int i = Math.min(s.length(), 120); i > 0; i--) {
            String t = s.substring(0, i) + ell;
            if (stringWidth(font, fontSize, t) <= maxWidth) return t;
        }
        return ell;
    }

    private float stringWidth(PDType1Font font, int fontSize, String s) throws IOException {
        return font.getStringWidth(s) / 1000f * fontSize;
    }

    private void drawLine(PDPageContentStream cs, float x1, float y1, float x2, float y2) throws IOException {
        cs.moveTo(x1, y1);
        cs.lineTo(x2, y2);
        cs.stroke();
    }

    private float sum(float[] a) { float s=0; for (float v : a) s+=v; return s; }

    /** Asegura espacio; si no hay, inicia nueva página y devuelve el nuevo y. */
    private float ensureSpace(PDDocument doc, PDPageContentStream cs, PDRectangle pageSize, float margin, float y, float needed) throws IOException {
        if (y - needed >= margin) return y;
        cs.close();
        PDPage page = new PDPage(pageSize);
        doc.addPage(page);
        PDPageContentStream next = new PDPageContentStream(doc, page);
        // devolvemos el nuevo y available; el llamador debe continuar con 'next'
        // para simplificar el uso, este método sólo calcula; el patrón real lo usé arriba
        return pageSize.getHeight() - margin;
    }

    // DTO interno
    private static class ItemAlumno {
        final String idCurso; final String nombreCurso; final int idGrupo; final Estudiante est;
        ItemAlumno(String idCurso, String nombreCurso, int idGrupo, Estudiante est) {
            this.idCurso = idCurso; this.nombreCurso = nombreCurso; this.idGrupo = idGrupo; this.est = est;
        }
    }
}
