package org.example;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementación en memoria del servicio de matrículas.
 * <p>
 * Permite matricular y desmatricular estudiantes, así como consultar
 * sus grupos y matrículas sin usar una base de datos real.
 * Toda la información se almacena temporalmente en estructuras de memoria.
 * </p>
 */
public class MatriculaServiceMem implements MatriculaService {

    // -- Dependencias --

    /** Servicio de usuarios, usado para localizar estudiantes. */
    private final UsuarioService usuarioService;

    /** Servicio de cursos, usado para acceder a los grupos. */
    private final CursoService cursoService;

    // -- Constructor --

    /**
     * Crea un servicio de matrículas en memoria.
     *
     * @param usuarioService servicio de usuarios
     * @param cursoService servicio de cursos
     */
    public MatriculaServiceMem(UsuarioService usuarioService, CursoService cursoService) {
        this.usuarioService = usuarioService;
        this.cursoService = cursoService;
    }

    // -- Consultas --

    /**
     * {@inheritDoc}
     * <p>
     * Devuelve todos los grupos en los que el estudiante está inscrito,
     * ordenados por fecha de inicio y luego por ID del grupo.
     * </p>
     */
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
        result.sort(Comparator.comparing(Grupo::getFechaInicio)
                .thenComparing(Grupo::getIdGrupo));
        return result;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Recorre todos los cursos y grupos para encontrar las matrículas
     * asociadas al estudiante.
     * </p>
     */
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

    // -- Operaciones principales --

    /**
     * {@inheritDoc}
     * <p>
     * Verifica que el grupo exista, tenga cupo disponible y que el estudiante
     * no esté ya matriculado. Si todo es válido, crea una nueva matrícula.
     * </p>
     *
     * @throws IllegalArgumentException si los parámetros son inválidos
     * @throws IllegalStateException si ya está matriculado o no hay cupo
     */
    @Override
    public boolean matricular(String idEstudiante, Curso curso, int idGrupo) {
        Objects.requireNonNull(curso, "Curso requerido.");
        Grupo g = findGrupo(curso, idGrupo);
        if (g == null) {
            throw new IllegalArgumentException("No existe el grupo #" + idGrupo + " en el curso " + curso.getId());
        }

        if (contieneEstudiante(g, idEstudiante)) {
            throw new IllegalStateException("Ya estás matriculado en este grupo.");
        }

        if (g.getMatriculas().size() >= curso.getMaxEstu()) {
            throw new IllegalStateException("No hay cupo disponible en este grupo.");
        }

        Estudiante est = usuarioService.listarEstudiantes().stream()
                .filter(e -> idEstudiante.equals(e.getIdUsuario()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Estudiante no encontrado: " + idEstudiante));

        Matricula nueva = new Matricula(est, g);
        g.getMatriculas().add(nueva);
        return true;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Elimina la matrícula de un estudiante del grupo indicado.
     * No aplica restricciones adicionales (por ejemplo, intentos ya realizados).
     * </p>
     */
    @Override
    public boolean desmatricular(String idEstudiante, Curso curso, int idGrupo) {
        Objects.requireNonNull(curso, "Curso requerido.");
        Grupo g = findGrupo(curso, idGrupo);
        if (g == null) {
            throw new IllegalArgumentException("No existe el grupo #" + idGrupo + " en el curso " + curso.getId());
        }

        int before = g.getMatriculas().size();
        g.getMatriculas().removeIf(m ->
                m.getEstudiante() != null &&
                        idEstudiante.equals(m.getEstudiante().getIdUsuario())
        );
        return g.getMatriculas().size() < before;
    }

    // -- Métodos auxiliares --

    /**
     * Busca un grupo específico dentro de un curso según su ID.
     *
     * @param curso curso que contiene los grupos
     * @param idGrupo identificador del grupo buscado
     * @return el grupo encontrado o {@code null} si no existe
     */
    private Grupo findGrupo(Curso curso, int idGrupo) {
        for (Grupo g : curso.grupos) {
            if (g.getIdGrupo() == idGrupo) return g;
        }
        return null;
    }

    /**
     * Verifica si un estudiante está matriculado en un grupo.
     *
     * @param g grupo a verificar
     * @param idEstudiante identificador del estudiante
     * @return {@code true} si el estudiante pertenece al grupo
     */
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
