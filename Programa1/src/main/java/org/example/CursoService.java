package org.example;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Contrato del módulo de Cursos & Grupos. */
public interface CursoService {
    List<Curso> listarCursos();
    void agregarCurso(Curso c);
    void actualizarCurso(Curso c);
    void eliminarCurso(String idCurso);

    // Grupos (ligados a un curso)
    List<Grupo> listarGrupos(Curso curso);
    Grupo crearGrupo(Curso curso, LocalDate inicio, LocalDate fin);
    void actualizarGrupoFechas(Curso curso, int idGrupo, LocalDate inicio, LocalDate fin);
    void asignarProfesor(Curso curso, int idGrupo, Profesor profesor);
    void eliminarGrupo(Curso curso, int idGrupo);
}

