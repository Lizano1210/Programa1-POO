package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class IntentoServiceMem implements IntentoService {

    private final List<IntentoEvaluacion> intentos = new ArrayList<>();

    @Override
    public synchronized void guardar(IntentoEvaluacion intento) {
        if (intento == null) return;
        // Reemplazar si ya existe un intento del mismo estudiante para la misma evaluación+grupo
        String estId = intento.getEstudiante() == null ? null : intento.getEstudiante().getIdUsuario();
        Integer evalId = intento.getEvaluacion() == null ? null : intento.getEvaluacion().getId();
        Integer grpId  = intento.getGrupo() == null ? null : intento.getGrupo().getIdGrupo();

        int idx = -1;
        for (int i = 0; i < intentos.size(); i++) {
            IntentoEvaluacion it = intentos.get(i);
            String eId = it.getEstudiante() == null ? null : it.getEstudiante().getIdUsuario();
            Integer ev = it.getEvaluacion() == null ? null : it.getEvaluacion().getId();
            Integer gr = it.getGrupo() == null ? null : it.getGrupo().getIdGrupo();
            if (Objects.equals(estId, eId) && Objects.equals(evalId, ev) && Objects.equals(grpId, gr)) {
                idx = i; break;
            }
        }
        if (idx >= 0) intentos.set(idx, intento);
        else intentos.add(intento);
    }

    @Override
    public synchronized List<IntentoEvaluacion> listarPorEstudiante(String idEstudiante) {
        if (idEstudiante == null) return List.of();
        return intentos.stream()
                .filter(it -> it.getEstudiante() != null && idEstudiante.equals(it.getEstudiante().getIdUsuario()))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public synchronized List<IntentoEvaluacion> listarPorGrupo(int idGrupo) {
        return intentos.stream()
                .filter(it -> it.getGrupo() != null && it.getGrupo().getIdGrupo() == idGrupo)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public synchronized List<IntentoEvaluacion> listarPorEvaluacion(int idEvaluacion) {
        return intentos.stream()
                .filter(it -> it.getEvaluacion() != null && it.getEvaluacion().getId() == idEvaluacion)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public synchronized List<IntentoEvaluacion> listarTodos() {
        return new ArrayList<>(intentos);
    }
}
