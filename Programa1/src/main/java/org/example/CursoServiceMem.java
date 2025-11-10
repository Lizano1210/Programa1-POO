package org.example;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Implementación en memoria de {@link CursoService}.
 * <p>
 * Gestiona cursos y grupos dentro de listas locales, sin base de datos.
 * Incluye métodos para agregar, actualizar y eliminar cursos,
 * así como crear, editar y eliminar grupos asociados.
 * </p>
 */
public class CursoServiceMem implements CursoService {

    /** Lista interna de cursos en memoria. */
    private final List<Curso> cursos = new ArrayList<>();

    /**
     * Crea una nueva instancia del servicio de cursos.
     *
     * @param usuarioService servicio de usuarios (opcional para validaciones futuras)
     */
    public CursoServiceMem(UsuarioService usuarioService) {
        // Se inyecta por si luego se requiere validación con profesores, etc.
    }

    // -- Cursos --

    @Override
    public List<Curso> listarCursos() {
        return Collections.unmodifiableList(cursos);
    }

    @Override
    public void agregarCurso(Curso c) {
        if (c == null) throw new IllegalArgumentException("Curso nulo.");
        if (!c.validarDatos()) throw new IllegalArgumentException("Datos del curso inválidos.");
        if (indexOfCurso(c.getId()) >= 0)
            throw new IllegalArgumentException("Ya existe un curso con ID " + c.getId());
        cursos.add(c);
    }

    @Override
    public void actualizarCurso(Curso c) {
        if (c == null) throw new IllegalArgumentException("Curso nulo.");
        if (!c.validarDatos()) throw new IllegalArgumentException("Datos del curso inválidos.");
        int idx = indexOfCurso(c.getId());
        if (idx < 0) throw new IllegalArgumentException("No existe curso con ID " + c.getId());
        cursos.set(idx, c);
    }

    @Override
    public void eliminarCurso(String idCurso) {
        int idx = indexOfCurso(idCurso);
        if (idx < 0) throw new IllegalArgumentException("No existe curso con ID " + idCurso);
        Curso cur = cursos.get(idx);

        // Regla: no se puede eliminar si hay grupos vigentes hoy
        for (Grupo g : cur.grupos) {
            if (g.esVigente(LocalDate.now())) {
                throw new IllegalStateException("No se puede eliminar el curso: hay grupos vigentes.");
            }
        }
        cursos.remove(idx);
    }

    // -- Grupos --

    @Override
    public List<Grupo> listarGrupos(Curso curso) {
        if (curso == null) return List.of();
        return Collections.unmodifiableList(curso.grupos);
    }

    @Override
    public Grupo crearGrupo(Curso curso, LocalDate inicio, LocalDate fin) {
        if (curso == null) throw new IllegalArgumentException("Curso requerido.");
        validarFechas(inicio, fin);
        curso.crearGrupo(inicio, fin);
        List<Grupo> gs = curso.grupos;
        return gs.isEmpty() ? null : gs.get(gs.size() - 1); // último creado
    }

    @Override
    public void actualizarGrupoFechas(Curso curso, int idGrupo, LocalDate inicio, LocalDate fin) {
        validarFechas(inicio, fin);
        Grupo g = findGrupo(curso, idGrupo);
        if (g == null) throw new IllegalArgumentException("No existe el grupo #" + idGrupo);
        g.setFechaInicio(inicio);
        g.setFechaFinal(fin);
    }

    @Override
    public void asignarProfesor(Curso curso, int idGrupo, Profesor profesor) {
        Grupo g = findGrupo(curso, idGrupo);
        if (g == null) throw new IllegalArgumentException("No existe el grupo #" + idGrupo);
        g.setProfesor(profesor); // puede ser null si se desea desasignar
    }

    @Override
    public void eliminarGrupo(Curso curso, int idGrupo) {
        if (curso == null) throw new IllegalArgumentException("Curso requerido.");
        Grupo g = findGrupo(curso, idGrupo);
        if (g == null) throw new IllegalArgumentException("No existe el grupo #" + idGrupo);

        // No se puede eliminar si tiene matrículas o evaluaciones
        if (g.getMatriculas() != null && !g.getMatriculas().isEmpty())
            throw new IllegalStateException("No se puede eliminar: el grupo tiene matrículas.");
        if (g.getEvaluacionesAsignadas() != null && !g.getEvaluacionesAsignadas().isEmpty())
            throw new IllegalStateException("No se puede eliminar: el grupo tiene evaluaciones asociadas.");

        curso.grupos.remove(g);
    }

    // -- Métodos auxiliares --

    /**
     * Busca la posición de un curso por su ID.
     *
     * @param id identificador del curso
     * @return índice en la lista o -1 si no existe
     */
    private int indexOfCurso(String id) {
        if (id == null) return -1;
        for (int i = 0; i < cursos.size(); i++) {
            if (id.equals(cursos.get(i).getId())) return i;
        }
        return -1;
    }

    /**
     * Busca un grupo dentro de un curso por su ID.
     *
     * @param curso curso donde buscar
     * @param idGrupo identificador del grupo
     * @return grupo encontrado o {@code null}
     */
    private Grupo findGrupo(Curso curso, int idGrupo) {
        if (curso == null) return null;
        for (Grupo g : curso.grupos) {
            if (g.getIdGrupo() == idGrupo) return g;
        }
        return null;
    }

    /**
     * Valida que las fechas de inicio y fin sean correctas.
     *
     * @param inicio fecha de inicio
     * @param fin fecha de finalización
     */
    private void validarFechas(LocalDate inicio, LocalDate fin) {
        if (inicio == null || fin == null) throw new IllegalArgumentException("Fechas requeridas.");
        if (fin.isBefore(inicio)) throw new IllegalArgumentException("La fecha final no puede ser anterior al inicio.");
    }

    // -- Datos de demostración --

    /**
     * Agrega cursos de ejemplo al sistema (semilla de demostración).
     */
    public void seedCursosDemo() {
        TipoModalidad modalidadDef = TipoModalidad.values()[0];
        TipoCurso tipoDef = TipoCurso.TEORICO;

        Curso c1 = new Curso("C10100", "Programación I", "Introducción a Java y fundamentos de programación",
                2, modalidadDef, 5, 20, tipoDef, 70);
        Curso c2 = new Curso("C10200", "Estructuras de Datos", "Listas, colas, pilas, árboles y complejidad básica",
                2, modalidadDef, 5, 20, tipoDef, 70);
        Curso c3 = new Curso("C10300", "Bases de Datos I", "Modelo relacional, normalización y SQL básico",
                2, modalidadDef, 5, 20, tipoDef, 70);
        Curso c4 = new Curso("C10400", "Redes I", "Conceptos de redes, OSI/TCP-IP y direccionamiento",
                2, modalidadDef, 5, 20, tipoDef, 70);

        try { agregarCurso(c1); } catch (Exception ignored) {}
        try { agregarCurso(c2); } catch (Exception ignored) {}
        try { agregarCurso(c3); } catch (Exception ignored) {}
        try { agregarCurso(c4); } catch (Exception ignored) {}
    }

    /**
     * Crea un grupo de demostración vinculado a un curso y profesor.
     *
     * @param usuarioService servicio de usuarios en memoria
     */
    public void seedGruposDemo(UsuarioServiceMem usuarioService) {
        try {
            Curso c1 = listarCursos().stream()
                    .filter(c -> "C10100".equals(c.getId()))
                    .findFirst()
                    .orElse(null);

            Profesor p1 = usuarioService.listarProfesores().stream()
                    .filter(p -> "P200".equals(p.getIdUsuario()))
                    .findFirst()
                    .orElse(null);

            if (c1 == null || p1 == null) return;

            LocalDate fechaInicio = LocalDate.now();
            LocalDate fechaFinal  = fechaInicio.plusMonths(4);

            Grupo g = new Grupo(c1, fechaInicio, fechaFinal);
            g.setProfesor(p1);

            if (c1.grupos == null) c1.grupos = new ArrayList<>();
            else if (!(c1.grupos instanceof ArrayList)) c1.grupos = new ArrayList<>(c1.grupos);

            if (p1.getGrupos() == null) p1.setGrupos(new ArrayList<>());
            else if (!(p1.getGrupos() instanceof ArrayList)) p1.setGrupos(new ArrayList<>(p1.getGrupos()));

            boolean yaEnCurso = c1.grupos.stream().anyMatch(xx -> xx != null && xx.getIdGrupo() == g.getIdGrupo());
            if (!yaEnCurso) c1.grupos.add(g);

            boolean yaEnProf = p1.getGrupos().stream().anyMatch(xx -> xx != null && xx.getIdGrupo() == g.getIdGrupo());
            if (!yaEnProf) p1.getGrupos().add(g);

            System.out.println("[seed] Grupo creado ID=" + g.getIdGrupo()
                    + " para curso=" + c1.getId() + " profesor=" + p1.getIdUsuario());

        } catch (Exception ignored) {
            // silencioso para no interrumpir el arranque
        }
    }
}

