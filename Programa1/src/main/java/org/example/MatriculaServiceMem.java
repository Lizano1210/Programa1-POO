package org.example;

import java.util.*;
import java.util.stream.Collectors;

public class MatriculaServiceMem implements MatriculaService {

    private final UsuarioService usuarioService;
    private final CursoService cursoService;

    public MatriculaServiceMem(UsuarioService usuarioService, CursoService cursoService) {
        this.usuarioService = usuarioService;
        this.cursoService = cursoService;
    }

    @Override
    public List<Grupo> gruposDelEstudiante(String idEstudiante) {
        List<Grupo> result = new ArrayList<>();
        for (Curso c : cursoService.listarCursos()) {
            for (Grupo g : c.grupos) {
                if (contieneEstudiante(g, idEstudiante)) {
                    result.add(g);
                }
            }
        }
        // Orden sugerido (opcional): por inicio y luego idGrupo
        result.sort(Comparator.comparing(Grupo::getFechaInicio)
                .thenComparing(Grupo::getIdGrupo));
        return result;
    }

    @Override
    public List<Matricula> listarMatriculas(String idEstudiante) {
        List<Matricula> res = new ArrayList<>();
        for (Curso c : cursoService.listarCursos()) {
            for (Grupo g : c.grupos) {
                for (Matricula m : g.getMatriculas()) {
                    Estudiante e = m.getEstudiante();
                    if (e != null && idEstudiante.equals(e.getIdUsuario())) {
                        res.add(m);
                    }
                }
            }
        }
        return res;
    }

    @Override
    public boolean matricular(String idEstudiante, Curso curso, int idGrupo) {
        Objects.requireNonNull(curso, "Curso requerido.");
        Grupo g = findGrupo(curso, idGrupo);
        if (g == null) {
            throw new IllegalArgumentException("No existe el grupo #" + idGrupo + " en el curso " + curso.getId());
        }

        // Ya matriculado
        if (contieneEstudiante(g, idEstudiante)) {
            throw new IllegalStateException("Ya estás matriculado en este grupo.");
        }

        // Cupo
        if (g.getMatriculas().size() >= curso.getMaxEstu()) {
            throw new IllegalStateException("No hay cupo disponible en este grupo.");
        }

        // Resolver estudiante
        Estudiante est = usuarioService.listarEstudiantes().stream()
                .filter(e -> idEstudiante.equals(e.getIdUsuario()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Estudiante no encontrado: " + idEstudiante));

        // >>> Firma fija:
        Matricula nueva = new Matricula(est, g);
        g.getMatriculas().add(nueva);
        return true;
    }

    @Override
    public boolean desmatricular(String idEstudiante, Curso curso, int idGrupo) {
        Objects.requireNonNull(curso, "Curso requerido.");
        Grupo g = findGrupo(curso, idGrupo);
        if (g == null) {
            throw new IllegalArgumentException("No existe el grupo #" + idGrupo + " en el curso " + curso.getId());
        }

        // Reglas adicionales (si las quieres): bloquear si ya inició el grupo, si tiene intentos, etc.
        // Por ahora, permitimos desmatricular siempre.

        int before = g.getMatriculas().size();
        g.getMatriculas().removeIf(m ->
                m.getEstudiante() != null
                        && idEstudiante.equals(m.getEstudiante().getIdUsuario())
        );
        return g.getMatriculas().size() < before;
    }

    // ─────────── helpers ───────────

    private Grupo findGrupo(Curso curso, int idGrupo) {
        for (Grupo g : curso.grupos) {
            if (g.getIdGrupo() == idGrupo) return g;
        }
        return null;
    }

    private boolean contieneEstudiante(Grupo g, String idEstudiante) {
        for (Matricula m : g.getMatriculas()) {
            Estudiante e = m.getEstudiante();
            if (e != null && idEstudiante.equals(e.getIdUsuario())) {
                return true;
            }
        }
        return false;
    }
}
