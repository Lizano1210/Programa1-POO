package org.example;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CursoServiceMem implements CursoService {

    private final List<Curso> cursos = new ArrayList<>();

    public CursoServiceMem(UsuarioService usuarioService) {
        // Se inyecta por si luego quieres validaciones con profesores, etc.
        // (No es estrictamente necesario para este CRUD básico.)
    }

    // Cursos

    @Override
    public List<Curso> listarCursos() {
        return Collections.unmodifiableList(cursos);
    }

    @Override
    public void agregarCurso(Curso c) {
        if (c == null) throw new IllegalArgumentException("Curso nulo.");
        // Valida con la propia clase (asumiendo que expone validarDatos())
        if (!c.validarDatos()) throw new IllegalArgumentException("Datos del curso inválidos.");
        // Unicidad por ID
        if (indexOfCurso(c.getId()) >= 0) {
            throw new IllegalArgumentException("Ya existe un curso con ID " + c.getId());
        }
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
        // Regla: no eliminar si hay grupos vigentes hoy
        for (Grupo g : cur.grupos) {
            if (g.esVigente(LocalDate.now())) {
                throw new IllegalStateException("No se puede eliminar el curso: hay grupos vigentes.");
            }
        }
        cursos.remove(idx);
    }

    // Grupos

    @Override
    public List<Grupo> listarGrupos(Curso curso) {
        if (curso == null) return List.of();
        // Asumimos que obtenerGrupos() devuelve la lista real (modificable).
        return Collections.unmodifiableList(curso.grupos);
    }

    @Override
    public Grupo crearGrupo(Curso curso, LocalDate inicio, LocalDate fin) {
        if (curso == null) throw new IllegalArgumentException("Curso requerido.");
        validarFechas(inicio, fin);
        // Usa el método del dominio que ya crea y añade el grupo (auto-id)
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
        g.setProfesor(profesor); // puede ser null si deseas desasignar
    }

    @Override
    public void eliminarGrupo(Curso curso, int idGrupo) {
        if (curso == null) throw new IllegalArgumentException("Curso requerido.");
        Grupo g = findGrupo(curso, idGrupo);
        if (g == null) throw new IllegalArgumentException("No existe el grupo #" + idGrupo);

        // Bloqueos típicos antes de eliminar
        if (g.getMatriculas() != null && !g.getMatriculas().isEmpty())
            throw new IllegalStateException("No se puede eliminar: el grupo tiene matrículas.");
        if (g.getEvaluacionesAsignadas() != null && !g.getEvaluacionesAsignadas().isEmpty())
            throw new IllegalStateException("No se puede eliminar: el grupo tiene evaluaciones asociadas.");

        curso.grupos.remove(g); // modificamos la lista del curso
    }

    // ===================== Helpers =====================

    private int indexOfCurso(String id) {
        if (id == null) return -1;
        for (int i = 0; i < cursos.size(); i++) {
            if (id.equals(cursos.get(i).getId())) return i;
        }
        return -1;
    }

    private Grupo findGrupo(Curso curso, int idGrupo) {
        if (curso == null) return null;
        for (Grupo g : curso.grupos) {
            if (g.getIdGrupo() == idGrupo) return g;
        }
        return null;
    }

    private void validarFechas(LocalDate inicio, LocalDate fin) {
        if (inicio == null || fin == null) throw new IllegalArgumentException("Fechas requeridas.");
        if (fin.isBefore(inicio)) throw new IllegalArgumentException("La fecha final no puede ser anterior al inicio.");
    }

    public void seedCursosDemo() {
        // Tomamos el primer valor disponible por si cambian los enums (evita fallos por nombres).
        TipoModalidad modalidadDef = TipoModalidad.values()[0];
        TipoCurso tipoDef = TipoCurso.TEORICO;

        // Cursos de ejemplo (usa tu constructor: id, nombre, descripcion, hrsDia, modalidad, min, max, tipo, aprob)
        Curso c1 = new Curso("C10100", "Programación I", "Introducción a Java y fundamentos de programación",
                2, modalidadDef, 5, 20, tipoDef, 70);

        Curso c2 = new Curso("C10200", "Estructuras de Datos", "Listas, colas, pilas, árboles y complejidad básica",
                2, modalidadDef, 5, 20, tipoDef, 70);

        Curso c3 = new Curso("C10300", "Bases de Datos I", "Modelo relacional, normalización y SQL básico",
                2, modalidadDef, 5, 20, tipoDef, 70);

        Curso c4 = new Curso("C10400", "Redes I", "Conceptos de redes, OSI/TCP-IP y direccionamiento",
                2, modalidadDef, 5, 20, tipoDef, 70);

        // Intentamos agregarlos (si ya existen por ID, se ignora el error y continúa)
        try { agregarCurso(c1); } catch (Exception ignored) {}
        try { agregarCurso(c2); } catch (Exception ignored) {}
        try { agregarCurso(c3); } catch (Exception ignored) {}
        try { agregarCurso(c4); } catch (Exception ignored) {}
    }

    public void seedGruposDemo(UsuarioServiceMem usuarioService) {
        try {
            // 1) Buscar curso Programación I
            Curso c1 = listarCursos().stream()
                    .filter(c -> "C10100".equals(c.getId()))
                    .findFirst()
                    .orElse(null);

            // 2) Buscar profesor Mario Rojas (P200)
            Profesor p1 = usuarioService.listarProfesores().stream()
                    .filter(p -> "P200".equals(p.getIdUsuario()))
                    .findFirst()
                    .orElse(null);

            if (c1 == null || p1 == null) {
                // No sembramos si falta alguno
                return;
            }

            // 3) Variables para fechas del grupo
            LocalDate fechaInicio = LocalDate.now();
            LocalDate fechaFinal  = fechaInicio.plusMonths(4);

            // 4) Crear grupo usando el CONSTRUCTOR (ID se autogenera)
            Grupo g = new Grupo(c1, fechaInicio, fechaFinal);

            // 5) Asignar profesor (tu constructor lo deja en null)
            g.setProfesor(p1);

            // 6) Asegurar listas MUTABLES en curso y profesor
            if (c1.grupos == null) {
                c1.grupos = new java.util.ArrayList<>();
            } else if (!(c1.grupos instanceof java.util.ArrayList)) {
                c1.grupos = new java.util.ArrayList<>(c1.grupos);
            }
            if (p1.getGrupos() == null) {
                p1.setGrupos(new java.util.ArrayList<>());
            } else if (!(p1.getGrupos() instanceof java.util.ArrayList)) {
                p1.setGrupos(new java.util.ArrayList<>(p1.getGrupos()));
            }

            // 7) Vincular en ambos lados evitando duplicados por ID
            boolean yaEnCurso = c1.grupos.stream().anyMatch(xx -> xx != null && xx.getIdGrupo() == g.getIdGrupo());
            if (!yaEnCurso) c1.grupos.add(g);

            boolean yaEnProf = p1.getGrupos().stream().anyMatch(xx -> xx != null && xx.getIdGrupo() == g.getIdGrupo());
            if (!yaEnProf) p1.getGrupos().add(g);

            // (Opcional) Inicializaciones extra ya las hace tu constructor:
            // g.setMatriculas(new ArrayList<>()); g.setEvaluacionesAsignadas(new ArrayList<>());
            // así que no hace falta repetirlas.

            // (Opcional) Log para saber qué ID autogeneró:
            System.out.println("[seed] Grupo creado ID=" + g.getIdGrupo()
                    + " para curso=" + c1.getId() + " profesor=" + p1.getIdUsuario());

        } catch (Exception ignored) {
            // silencioso para no romper el arranque si algo no está listo
        }
    }


}
