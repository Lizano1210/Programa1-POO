package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Implementación en memoria del servicio de gestión de intentos de evaluación.
 * <p>
 * Permite registrar, actualizar y consultar intentos de estudiantes según distintos criterios:
 * por estudiante, grupo o evaluación. Utiliza una lista en memoria como almacenamiento temporal.
 * </p>
 */
public class IntentoServiceMem implements IntentoService {

    // -- Atributos --

    /** Lista de intentos almacenados en memoria. */
    private final List<IntentoEvaluacion> intentos = new ArrayList<>();

    // -- Registro de intentos --

    /**
     * Guarda un intento de evaluación.
     * <p>
     * Si ya existe un intento del mismo estudiante para la misma evaluación y grupo,
     * lo reemplaza. En caso contrario, agrega un nuevo intento a la lista.
     * </p>
     *
     * @param intento intento de evaluación a registrar
     */
    @Override
    public synchronized void guardar(IntentoEvaluacion intento) {
        if (intento == null) return;

        // Identificadores base
        String estId = intento.getEstudiante() == null ? null : intento.getEstudiante().getIdUsuario();
        Integer evalId = intento.getEvaluacion() == null ? null : intento.getEvaluacion().getId();
        Integer grpId  = intento.getGrupo() == null ? null : intento.getGrupo().getIdGrupo();

        // Buscar si ya existe
        int idx = -1;
        for (int i = 0; i < intentos.size(); i++) {
            IntentoEvaluacion it = intentos.get(i);
            String eId = it.getEstudiante() == null ? null : it.getEstudiante().getIdUsuario();
            Integer ev = it.getEvaluacion() == null ? null : it.getEvaluacion().getId();
            Integer gr = it.getGrupo() == null ? null : it.getGrupo().getIdGrupo();

            if (Objects.equals(estId, eId) && Objects.equals(evalId, ev) && Objects.equals(grpId, gr)) {
                idx = i;
                break;
            }
        }

        // Reemplazar o agregar
        if (idx >= 0) intentos.set(idx, intento);
        else intentos.add(intento);
    }

    // -- Consultas --

    /**
     * Obtiene todos los intentos realizados por un estudiante específico.
     *
     * @param idEstudiante identificador del estudiante
     * @return lista de intentos del estudiante (vacía si no tiene)
     */
    @Override
    public synchronized List<IntentoEvaluacion> listarPorEstudiante(String idEstudiante) {
        if (idEstudiante == null) return List.of();
        return intentos.stream()
                .filter(it -> it.getEstudiante() != null && idEstudiante.equals(it.getEstudiante().getIdUsuario()))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Obtiene todos los intentos asociados a un grupo específico.
     *
     * @param idGrupo identificador del grupo
     * @return lista de intentos del grupo
     */
    @Override
    public synchronized List<IntentoEvaluacion> listarPorGrupo(int idGrupo) {
        return intentos.stream()
                .filter(it -> it.getGrupo() != null && it.getGrupo().getIdGrupo() == idGrupo)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Obtiene todos los intentos asociados a una evaluación específica.
     *
     * @param idEvaluacion identificador de la evaluación
     * @return lista de intentos relacionados con la evaluación
     */
    @Override
    public synchronized List<IntentoEvaluacion> listarPorEvaluacion(int idEvaluacion) {
        return intentos.stream()
                .filter(it -> it.getEvaluacion() != null && it.getEvaluacion().getId() == idEvaluacion)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Devuelve una copia de todos los intentos almacenados en memoria.
     *
     * @return lista completa de intentos
     */
    @Override
    public synchronized List<IntentoEvaluacion> listarTodos() {
        return new ArrayList<>(intentos);
    }
}
