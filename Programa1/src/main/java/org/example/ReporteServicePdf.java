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
import java.time.format.DateTimeFormatter;
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

    private static final PDType1Font HELVETICA = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
    private static final PDType1Font HELVETICA_BOLD = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

    public ReporteServicePdf(CursoService cursoService, UsuarioService usuarioService) {
        this.cursoService = cursoService;
        this.usuarioService = usuarioService;
    }

    @Override
    public Path generarListaEstudiantes(LocalDate fechaVigencia, Scope scope) throws Exception {
        List<ItemAlumno> alumnos = recolectarAlumnos(fechaVigencia, scope);
        alumnos.sort(Comparator
                .comparing((ItemAlumno it) -> safe(it.est.getApellido1()))
                .thenComparing(it -> safe(it.est.getNombre()))
                .thenComparing(it -> safe(it.est.getApellido2()))
        );

        Path out = outPath("lista_estudiantes", fechaVigencia, scope, "pdf");

        try (PDDocument doc = new PDDocument()) {
            PDRectangle pageSize = PDRectangle.LETTER;
            float margin = 40f;
            float y = pageSize.getHeight() - margin;

            PDPage page = new PDPage(pageSize);
            doc.addPage(page);
            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                y = drawTitle(cs, "Lista de Estudiantes", "Vigentes desde " + fechaVigencia, y);

                String[] headers = {"CursoID", "Curso", "Grupo", "Identificación", "Nombre completo", "Correo"};
                float[] widths = {60, 120, 50, 90, 170, 160};
                y = y - 16;
                y = drawTableHeader(cs, margin, y, headers, widths);

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
                        y = ensureSpace(doc, cs, pageSize, margin, y, 40);
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
                .filter(g -> !g.getFechaFinal().isBefore(fechaVig))
                .filter(g -> scope == null
                        || scope.tipo == ScopeType.TODOS
                        || (scope.tipo == ScopeType.CURSO && c.getId().equals(scope.idCurso))
                        || (scope.tipo == ScopeType.GRUPO && c.getId().equals(scope.idCurso) && g.getIdGrupo() == scope.idGrupo))
                .collect(Collectors.toList());
    }

    private Estudiante tryGetEstudianteFromMatricula(Object matricula) {
        Estudiante e = (Estudiante) tryInvoke(matricula, "getEstudiante");
        if (e != null) return e;
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

    private float drawTitle(PDPageContentStream cs, String title, String subtitle, float yTop) throws IOException {
        float x = 40f;
        writeLine(cs, title, x, yTop, 16, HELVETICA_BOLD);
        yTop -= 18;
        writeLine(cs, subtitle, x, yTop, 12, HELVETICA);
        return yTop - 10;
    }

    private float drawTableHeader(PDPageContentStream cs, float x, float y, String[] headers, float[] widths) throws IOException {
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

    private void writeClipped(PDPageContentStream cs, String text, float x, float y,
                              float maxWidth, int fontSize, PDType1Font font) throws IOException {
        if (text == null) text = "";
        String clipped = clipToWidth(font, fontSize, text, maxWidth);
        writeLine(cs, clipped, x, y, fontSize, font);
    }

    private String clipToWidth(PDType1Font font, int fontSize, String s, float maxWidth) throws IOException {
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

    private float ensureSpace(PDDocument doc, PDPageContentStream cs, PDRectangle pageSize, float margin, float y, float needed) throws IOException {
        if (y - needed >= margin) return y;
        cs.close();
        PDPage page = new PDPage(pageSize);
        doc.addPage(page);
        return pageSize.getHeight() - margin;
    }

    private static class ItemAlumno {
        final String idCurso; final String nombreCurso; final int idGrupo; final Estudiante est;
        ItemAlumno(String idCurso, String nombreCurso, int idGrupo, Estudiante est) {
            this.idCurso = idCurso; this.nombreCurso = nombreCurso; this.idGrupo = idGrupo; this.est = est;
        }
    }

    @Override
    public boolean exportarIntento(IntentoEvaluacion intento, java.io.File destino) {
        if (intento == null || destino == null) return false;
        java.io.File out = destino;
        String name = destino.getName().toLowerCase();
        if (!name.endsWith(".pdf")) {
            out = new java.io.File(destino.getParentFile(), destino.getName() + ".pdf");
        }

        PDRectangle pageSize = PDRectangle.LETTER;
        float margin = 40f;

        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage(pageSize);
            doc.addPage(page);
            PDPageContentStream cs = new PDPageContentStream(doc, page);

            float y = pageSize.getHeight() - margin;

            String est = (intento.getEstudiante() == null) ? "-"
                    : joinNames(intento.getEstudiante().getNombre(),
                    intento.getEstudiante().getApellido1(),
                    intento.getEstudiante().getApellido2());
            String eval = (intento.getEvaluacion() == null) ? "-"
                    : intento.getEvaluacion().getNombre();
            String grupo = (intento.getGrupo() == null) ? "-"
                    : String.valueOf(intento.getGrupo().getIdGrupo());
            int puntajeTotal = (intento.getEvaluacion() == null) ? 0
                    : intento.getEvaluacion().getPuntajeTotal();

            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            String ini = intento.getFechaHoraInicio() == null ? "-" : intento.getFechaHoraInicio().format(fmt);
            String fin = intento.getFechaHoraFinal() == null ? "-" : intento.getFechaHoraFinal().format(fmt);

            y = drawTitle(cs, "Reporte de intento de evaluación", "", y);
            writeLine(cs, "Estudiante: " + est, margin, y, 12, HELVETICA); y -= 14;
            writeLine(cs, "Evaluación: " + eval, margin, y, 12, HELVETICA); y -= 14;
            writeLine(cs, "Grupo: " + grupo, margin, y, 12, HELVETICA); y -= 14;
            writeLine(cs, "Inicio: " + ini, margin, y, 12, HELVETICA); y -= 14;
            writeLine(cs, "Fin: " + fin, margin, y, 12, HELVETICA); y -= 14;
            writeLine(cs, "Puntaje: " + intento.getPuntajeObtenido() + " / " + puntajeTotal, margin, y, 12, HELVETICA); y -= 14;
            writeLine(cs, String.format("Calificación: %.2f", intento.getCalificacion()), margin, y, 12, HELVETICA); y -= 18;

            List<IPregunta> preguntas = (intento.getEvaluacion() == null) ? null : intento.getEvaluacion().getPreguntas();
            List<RespuestaEstudiante> resps = intento.getRespuestasEstudiante();

            if (preguntas != null && !preguntas.isEmpty()) {
                writeLine(cs, "Detalle de preguntas", margin, y, 12, HELVETICA_BOLD); y -= 14;

                for (int i = 0; i < preguntas.size(); i++) {
                    IPregunta p = preguntas.get(i);
                    RespuestaEstudiante re = (resps != null && i < resps.size()) ? resps.get(i) : null;

                    if (y - 56 < margin) {
                        cs.close();
                        page = new PDPage(pageSize);
                        doc.addPage(page);
                        cs = new PDPageContentStream(doc, page);
                        y = pageSize.getHeight() - margin;
                    }

                    writeLine(cs, "Pregunta " + (i + 1) + ": " + safe(p.obtenerDescripcion()), margin, y, 12, HELVETICA); y -= 14;
                    writeLine(cs, "Tipo: " + String.valueOf(p.getTipo()), margin, y, 11, HELVETICA); y -= 12;
                    writeLine(cs, "Puntos: " + p.obtenerPuntos(), margin, y, 11, HELVETICA); y -= 12;

                    if (re != null) {
                        writeLine(cs, "Puntos obtenidos: " + re.getPuntosObtenidos(), margin, y, 11, HELVETICA); y -= 12;
                        List<Integer> ord = re.getOrdenesSeleccionados();
                        writeLine(cs, "Selecciones: " + (ord == null ? "—" : ord.toString()), margin, y, 11, HELVETICA); y -= 14;
                    } else {
                        writeLine(cs, "Sin respuesta del estudiante.", margin, y, 11, HELVETICA); y -= 14;
                    }
                }
            } else {
                writeLine(cs, "No hay preguntas para mostrar.", margin, y, 12, HELVETICA); y -= 14;
            }

            cs.close();
            if (out.getParentFile() != null) out.getParentFile().mkdirs();
            doc.save(out);
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }
}


