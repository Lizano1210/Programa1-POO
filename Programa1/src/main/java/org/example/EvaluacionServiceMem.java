package org.example;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementación en memoria del servicio de evaluaciones.
 * <p>
 * Gestiona las evaluaciones creadas por los profesores, validando propiedad,
 * asociaciones con grupos y permitiendo la administración de preguntas.
 * Incluye una función para generar datos de ejemplo.
 * </p>
 */
public class EvaluacionServiceMem implements EvaluacionService {

    // -- Dependencias --

    /** Servicio de usuarios. */
    private final UsuarioServiceMem usuarioService;

    /** Servicio de cursos (para validar asociaciones con grupos). */
    private final CursoService cursoService;

    // -- Estructuras internas --

    /** Evaluaciones registradas por profesor (idProfesor → lista de evaluaciones). */
    private final Map<String, List<Evaluacion>> porProfesor = new HashMap<>();

    /** Índice de propietarios (idEvaluacion → idProfesor). */
    private final Map<Integer, String> ownerIndex = new HashMap<>();

    // -- Constructor --

    /**
     * Crea el servicio de evaluaciones en memoria.
     *
     * @param usuarioService servicio de usuarios
     * @param cursoService servicio de cursos
     */
    public EvaluacionServiceMem(UsuarioServiceMem usuarioService, CursoService cursoService) {
        this.usuarioService = Objects.requireNonNull(usuarioService);
        this.cursoService = Objects.requireNonNull(cursoService);
    }

    // -- CRUD de evaluaciones --

    /** {@inheritDoc} */
    @Override
    public List<Evaluacion> listarPorProfesor(String idProfesor) {
        List<Evaluacion> src = porProfesor.getOrDefault(idProfesor, List.of());
        return src.stream().filter(Objects::nonNull).collect(Collectors.toCollection(ArrayList::new));
    }

    /** {@inheritDoc} */
    @Override
    public Evaluacion crear(String idProfesor, Evaluacion evaluacion) {
        if (idProfesor == null || idProfesor.isBlank()) throw new IllegalArgumentException("Profesor requerido.");
        if (evaluacion == null) throw new IllegalArgumentException("Evaluación requerida.");

        List<Evaluacion> lst = porProfesor.computeIfAbsent(idProfesor, k -> new ArrayList<>());
        for (Evaluacion e : lst) {
            if (e != null && e.getId() == evaluacion.getId()) {
                throw new IllegalStateException("Ya existe una evaluación con id " + evaluacion.getId());
            }
        }
        lst.add(evaluacion);
        ownerIndex.put(evaluacion.getId(), idProfesor);
        return evaluacion;
    }

    /** {@inheritDoc} */
    @Override
    public void actualizar(String idProfesor, Evaluacion evaluacion) {
        if (evaluacion == null) throw new IllegalArgumentException("Evaluación requerida.");
        Evaluacion actual = findOwned(idProfesor, evaluacion.getId());

        if (estaAsociada(actual)) {
            throw new IllegalStateException("No se puede modificar: la evaluación está asociada a uno o más grupos.");
        }

        List<Evaluacion> lst = porProfesor.getOrDefault(idProfesor, List.of());
        if (!(lst instanceof ArrayList<?>)) {
            lst = new ArrayList<>(lst);
            porProfesor.put(idProfesor, lst);
        }
        for (int i = 0; i < lst.size(); i++) {
            Evaluacion e = lst.get(i);
            if (e != null && e.getId() == evaluacion.getId()) {
                lst.set(i, evaluacion);
                break;
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void eliminar(String idProfesor, int idEvaluacion) {
        Evaluacion ev = findOwned(idProfesor, idEvaluacion);
        if (estaAsociada(ev)) {
            throw new IllegalStateException("No se puede eliminar: la evaluación está asociada a uno o más grupos. " +
                    "Desasóciela primero desde 'Asignar a grupo'.");
        }
        List<Evaluacion> lst = porProfesor.getOrDefault(idProfesor, List.of());
        if (!(lst instanceof ArrayList<?>)) {
            lst = new ArrayList<>(lst);
            porProfesor.put(idProfesor, lst);
        }
        lst.removeIf(e -> e != null && e.getId() == idEvaluacion);
        ownerIndex.remove(idEvaluacion);
    }

    // -- Gestión de preguntas --

    /** {@inheritDoc} */
    @Override
    public void agregarPregunta(String idProfesor, int idEvaluacion, IPregunta pregunta) {
        if (pregunta == null) throw new IllegalArgumentException("Pregunta requerida.");
        Evaluacion ev = findOwned(idProfesor, idEvaluacion);
        if (estaAsociada(ev)) {
            throw new IllegalStateException("No se puede modificar: la evaluación está asociada a uno o más grupos.");
        }
        ev.agregarPregunta(pregunta);
        ev.calcularPuntajeTotal();
    }

    /** {@inheritDoc} */
    @Override
    public void actualizarPregunta(String idProfesor, int idEvaluacion, int index, IPregunta preguntaActualizada) {
        if (preguntaActualizada == null) throw new IllegalArgumentException("Pregunta requerida.");
        Evaluacion ev = findOwned(idProfesor, idEvaluacion);
        if (estaAsociada(ev)) {
            throw new IllegalStateException("No se puede modificar: la evaluación está asociada a uno o más grupos.");
        }

        List<IPregunta> ps = ev.getPreguntas();
        if (ps == null) throw new IllegalStateException("La evaluación no tiene lista de preguntas.");
        if (index < 0 || index >= ps.size()) throw new IndexOutOfBoundsException("Índice de pregunta fuera de rango.");

        if (!(ps instanceof ArrayList<?>)) {
            ps = new ArrayList<>(ps);
            try { ev.setPreguntas(ps); } catch (Throwable ignore) {}
        }
        ps.set(index, preguntaActualizada);
        ev.calcularPuntajeTotal();
    }

    /** {@inheritDoc} */
    @Override
    public void eliminarPregunta(String idProfesor, int idEvaluacion, int index) {
        Evaluacion ev = findOwned(idProfesor, idEvaluacion);
        if (estaAsociada(ev)) {
            throw new IllegalStateException("No se puede modificar: la evaluación está asociada a uno o más grupos.");
        }
        List<IPregunta> ps = ev.getPreguntas();
        if (ps == null) return;
        if (index < 0 || index >= ps.size()) throw new IndexOutOfBoundsException("Índice de pregunta fuera de rango.");

        if (!(ps instanceof ArrayList<?>)) {
            ps = new ArrayList<>(ps);
            try { ev.setPreguntas(ps); } catch (Throwable ignore) {}
        }
        ps.remove(index);
        ev.calcularPuntajeTotal();
    }

    // -- Asignación a grupos --

    /** {@inheritDoc} */
    @Override
    public void asociarAGrupo(String idProfesor, int idEvaluacion, Curso curso, int idGrupo, LocalDateTime fechaHoraInicio) {
        Evaluacion ev = findOwned(idProfesor, idEvaluacion);
        if (curso == null) throw new IllegalArgumentException("Curso requerido.");
        if (fechaHoraInicio == null) throw new IllegalArgumentException("Fecha/hora de inicio requerida.");

        // Buscar el grupo dentro del curso
        Grupo target = null;
        if (curso.grupos != null) {
            for (Grupo g : curso.grupos) {
                if (g != null && g.getIdGrupo() == idGrupo) { target = g; break; }
            }
        }
        if (target == null) throw new IllegalStateException("No se encontró el grupo " + idGrupo + " dentro del curso " + curso.getId());

        // Verifica si ya está asociada
        if (target.getEvaluacionesAsignadas() != null) {
            for (EvaluacionAsignada ea : target.getEvaluacionesAsignadas()) {
                if (ea != null && ea.getEvaluacion() != null && ea.getEvaluacion().getId() == ev.getId()) {
                    throw new IllegalStateException("La evaluación ya está asociada a este grupo.");
                }
            }
        }

        // Crear la asociación
        EvaluacionAsignada ea = new EvaluacionAsignada(ev, target, fechaHoraInicio);
        ea.calcularFechaHoraFinal();

        if (target.getEvaluacionesAsignadas() == null) {
            target.setEvaluacionesAsignadas(new ArrayList<>());
        }
        target.getEvaluacionesAsignadas().add(ea);

        try {
            List<EvaluacionAsignada> asocs = ev.getGruposAsociados();
            if (asocs != null) {
                asocs.add(ea);
            } else {
                List<EvaluacionAsignada> nueva = new ArrayList<>();
                nueva.add(ea);
                ev.setGruposAsociados(nueva);
            }
        } catch (Throwable ignore) {}
    }

    /** {@inheritDoc} */
    @Override
    public void desasociarDeGrupo(String idProfesor, int idEvaluacion, Curso curso, int idGrupo) {
        Evaluacion ev = findOwned(idProfesor, idEvaluacion);
        if (curso == null) throw new IllegalArgumentException("Curso requerido.");

        Grupo target = null;
        if (curso.grupos != null) {
            for (Grupo g : curso.grupos) {
                if (g != null && g.getIdGrupo() == idGrupo) { target = g; break; }
            }
        }
        if (target == null) throw new IllegalStateException("No se encontró el grupo " + idGrupo + " dentro del curso " + curso.getId());

        if (target.getEvaluacionesAsignadas() == null) return;

        Iterator<EvaluacionAsignada> it = target.getEvaluacionesAsignadas().iterator();
        boolean removed = false;
        while (it.hasNext()) {
            EvaluacionAsignada ea = it.next();
            if (ea != null && ea.getEvaluacion() != null && ea.getEvaluacion().getId() == ev.getId()) {
                boolean puede;
                try { puede = ea.canDesasociar(); }
                catch (Throwable t) {
                    LocalDateTime now = LocalDateTime.now();
                    puede = (ea.getFechaHoraInicio() != null && ea.getFechaHoraInicio().isAfter(now));
                }
                if (!puede)
                    throw new IllegalStateException("No se puede desasociar: la evaluación ya inició o está vigente.");
                it.remove();
                removed = true;
            }
        }

        if (removed) {
            try {
                List<EvaluacionAsignada> asocs = ev.getGruposAsociados();
                if (asocs != null) {
                    asocs.removeIf(x -> x != null && x.getGrupo() != null && x.getGrupo().getIdGrupo() == idGrupo);
                }
            } catch (Throwable ignore) {}
        }
    }

    // -- Métodos auxiliares --

    /** Verifica que la evaluación exista y pertenezca al profesor. */
    private Evaluacion findOwned(String idProfesor, int idEvaluacion) {
        String owner = ownerIndex.get(idEvaluacion);
        if (owner == null || !owner.equals(idProfesor))
            throw new IllegalStateException("La evaluación no pertenece al profesor o no existe (id=" + idEvaluacion + ").");

        List<Evaluacion> lst = porProfesor.getOrDefault(idProfesor, List.of());
        for (Evaluacion e : lst) {
            if (e != null && e.getId() == idEvaluacion) return e;
        }
        throw new IllegalStateException("No se encontró la evaluación (id=" + idEvaluacion + ").");
    }

    /** Retorna true si la evaluación está asociada a cualquier grupo del sistema. */
    private boolean estaAsociada(Evaluacion ev) {
        try {
            if (ev.getGruposAsociados() != null && !ev.getGruposAsociados().isEmpty()) return true;
        } catch (Throwable ignore) {}

        List<Curso> cursos = null;
        try { cursos = cursoService.listarCursos(); } catch (Exception ignored) {}
        if (cursos != null) {
            for (Curso c : cursos) {
                if (c == null || c.grupos == null) continue;
                for (Grupo g : c.grupos) {
                    if (g == null || g.getEvaluacionesAsignadas() == null) continue;
                    for (EvaluacionAsignada ea : g.getEvaluacionesAsignadas()) {
                        if (ea != null && ea.getEvaluacion() != null && ea.getEvaluacion().getId() == ev.getId())
                            return true;
                    }
                }
            }
        }
        return false;
    }

    // -- Datos de demostración --

    /**
     * Genera una evaluación de ejemplo para pruebas y demostración.
     *
     * @param idProfesor identificador del profesor
     * @param usuarioService servicio de usuarios
     */
    public void seedEvaluacionesDemo5(String idProfesor, UsuarioServiceMem usuarioService) {
        try {
            Profesor p = usuarioService.listarProfesores().stream()
                    .filter(x -> idProfesor.equals(x.getIdUsuario()))
                    .findFirst().orElse(null);
            if (p == null) {
                System.out.println("[seed eval] No existe el profesor " + idProfesor);
                return;
            }

            List<String> objetivos = List.of(
                    "Reconocer sintaxis de Java",
                    "Diferenciar conceptos base"
            );

            Evaluacion ev = new Evaluacion(
                    "Quiz Demo (5 tipos)",
                    "Responda todas las preguntas. Tiempo total 45 minutos.",
                    new ArrayList<>(objetivos),
                    45,
                    true,
                    true
            );

            // Preguntas de ejemplo
            int nextId = 1;
            Pregunta q1 = new Pregunta(nextId++, TipoPregunta.SELECCION_UNICA,
                    "¿Qué palabra define una clase en Java?", 10);
            q1.agregarRespuesta(new Respuesta("class", true, 1));
            q1.agregarRespuesta(new Respuesta("def", false, 2));
            q1.agregarRespuesta(new Respuesta("struct", false, 3));
            ev.agregarPregunta(q1);

            ev.calcularPuntajeTotal();

            this.crear(p.getIdUsuario(), ev);
            List<Evaluacion> actual = p.getEvaluaciones();
            ArrayList<Evaluacion> nueva = actual == null ? new ArrayList<>() : new ArrayList<>(actual);
            if (nueva.stream().noneMatch(e -> e != null && e.getId() == ev.getId())) nueva.add(ev);
            p.setEvaluaciones(nueva);

            System.out.println("[seed eval] Creada '" + ev.getNombre() + "' para " + p.getIdUsuario() +
                    " con " + ev.getPreguntas().size() + " preguntas. Puntaje=" + ev.getPuntajeTotal());
        } catch (Exception ex) {
            System.out.println("[seed eval] Falló seed: " + ex.getMessage());
        }
    }
}

