package org.example;

import java.util.List;

public interface IntentoService {
    void guardar(IntentoEvaluacion intento);

    List<IntentoEvaluacion> listarPorEstudiante(String idEstudiante);

    List<IntentoEvaluacion> listarPorGrupo(int idGrupo);
    List<IntentoEvaluacion> listarPorEvaluacion(int idEvaluacion);
    List<IntentoEvaluacion> listarTodos();
}
