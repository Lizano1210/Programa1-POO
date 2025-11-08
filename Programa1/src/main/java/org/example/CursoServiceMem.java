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
}
