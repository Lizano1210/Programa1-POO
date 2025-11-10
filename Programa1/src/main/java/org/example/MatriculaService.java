package org.example;

import java.util.List;

public interface MatriculaService {
    /** Grupos donde está matriculado el estudiante. */
    List<Grupo> gruposDelEstudiante(String idEstudiante);

    /** Lista de matrículas del estudiante (si necesitas mostrar más detalle). */
    List<Matricula> listarMatriculas(String idEstudiante);

    /** Intenta matricular al estudiante en el grupo indicado. Lanza excepción si no procede. */
    boolean matricular(String idEstudiante, Curso curso, int idGrupo);

    /** Intenta desmatricularlo del grupo. Lanza excepción si no procede. */
    boolean desmatricular(String idEstudiante, Curso curso, int idGrupo);
}
