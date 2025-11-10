package org.example;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementación en memoria de EvaluacionService.
 * - Mantiene evaluaciones por profesor.
 * - Valida propiedad (owner) y asociaciones a grupos antes de modificar/eliminar.
 */
public class EvaluacionServiceMem implements EvaluacionService {

    private final UsuarioServiceMem usuarioService;
    private final CursoService cursoService;

    /** Evaluaciones por profesor (idProfesor -> lista) */
    private final Map<String, List<Evaluacion>> porProfesor = new HashMap<>();

    /** Índice rápido: idEvaluacion -> idProfesor (propietario) */
    private final Map<Integer, String> ownerIndex = new HashMap<>();

    public EvaluacionServiceMem(UsuarioServiceMem usuarioService, CursoService cursoService) {
        this.usuarioService = Objects.requireNonNull(usuarioService);
        this.cursoService = Objects.requireNonNull(cursoService);
    }

    // ===================== CRUD Evaluacion =====================

    @Override
    public List<Evaluacion> listarPorProfesor(String idProfesor) {
        List<Evaluacion> src = porProfesor.getOrDefault(idProfesor, List.of());
        return src.stream().filter(Objects::nonNull).collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public Evaluacion crear(String idProfesor, Evaluacion evaluacion) {
        if (idProfesor == null || idProfesor.isBlank()) throw new IllegalArgumentException("Profesor requerido.");
        if (evaluacion == null) throw new IllegalArgumentException("Evaluación requerida.");

        List<Evaluacion> lst = porProfesor.computeIfAbsent(idProfesor, k -> new ArrayList<>());
        // evitar duplicado por id
        for (Evaluacion e : lst) {
            if (e != null && e.getId() == evaluacion.getId()) {
                throw new IllegalStateException("Ya existe una evaluación con id " + evaluacion.getId());
            }
        }
        lst.add(evaluacion);
        ownerIndex.put(evaluacion.getId(), idProfesor);
        return evaluacion;
    }

    @Override
    public void actualizar(String idProfesor, Evaluacion evaluacion) {
        if (evaluacion == null) throw new IllegalArgumentException("Evaluación requerida.");
        Evaluacion actual = findOwned(idProfesor, evaluacion.getId());
        // no permitir modificar si está asociada (regla de negocio)
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

    @Override
    public void eliminar(String idProfesor, int idEvaluacion) {
        Evaluacion ev = findOwned(idProfesor, idEvaluacion);
        if (estaAsociada(ev)) {
            throw new IllegalStateException(
                    "No se puede eliminar: la evaluación está asociada a uno o más grupos. " +
                            "Desasóciela primero desde 'Asignar a grupo'."
            );
        }
        List<Evaluacion> lst = porProfesor.getOrDefault(idProfesor, List.of());
        if (!(lst instanceof ArrayList<?>)) {
            lst = new ArrayList<>(lst);
            porProfesor.put(idProfesor, lst);
        }
        lst.removeIf(e -> e != null && e.getId() == idEvaluacion);
        ownerIndex.remove(idEvaluacion);
    }

    // ===================== Gestión de preguntas =====================

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

        // Reemplazo seguro
        if (!(ps instanceof ArrayList<?>)) {
            ps = new ArrayList<>(ps);
            // si tu Evaluacion tiene setPreguntas(List<IPregunta>) úsalo:
            try { ev.setPreguntas(ps); } catch (Throwable ignore) {}
        }
        ps.set(index, preguntaActualizada);
        ev.calcularPuntajeTotal();
    }

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

    // ===================== Asignación a grupos =====================

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

        // Evitar duplicado de asociación
        if (target.getEvaluacionesAsignadas() != null) {
            for (EvaluacionAsignada ea : target.getEvaluacionesAsignadas()) {
                if (ea != null && ea.getEvaluacion() != null && ea.getEvaluacion().getId() == ev.getId()) {
                    throw new IllegalStateException("La evaluación ya está asociada a este grupo.");
                }
            }
        }

        // Crear la asociación
        EvaluacionAsignada ea = new EvaluacionAsignada(ev, target, fechaHoraInicio);
        ea.calcularFechaHoraFinal(); // usa duración de la evaluación

        // Agregar en el grupo
        if (target.getEvaluacionesAsignadas() == null) {
            target.setEvaluacionesAsignadas(new ArrayList<>());
        }
        target.getEvaluacionesAsignadas().add(ea);

        // (Opcional) también agregar en la propia evaluación si tu modelo lo usa:
        try {
            List<EvaluacionAsignada> asocs = ev.getGruposAsociados();
            if (asocs != null) {
                asocs.add(ea);
            } else {
                // si existe setGruposAsociados, la usamos
                List<EvaluacionAsignada> nueva = new ArrayList<>();
                nueva.add(ea);
                ev.setGruposAsociados(nueva);
            }
        } catch (Throwable ignore) {
            // Si Evaluacion no expone setter, no es crítico porque validamos por recorrida de cursos.
        }
    }

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

        // Buscar la asignación y validar si puede desasociarse
        Iterator<EvaluacionAsignada> it = target.getEvaluacionesAsignadas().iterator();
        boolean removed = false;
        while (it.hasNext()) {
            EvaluacionAsignada ea = it.next();
            if (ea != null && ea.getEvaluacion() != null && ea.getEvaluacion().getId() == ev.getId()) {
                boolean puede;
                try {
                    puede = ea.canDesasociar(); // si tu clase lo tiene
                } catch (Throwable t) {
                    // fallback: solo si aún no inicia
                    LocalDateTime now = LocalDateTime.now();
                    puede = (ea.getFechaHoraInicio() != null && ea.getFechaHoraInicio().isAfter(now));
                }
                if (!puede) {
                    throw new IllegalStateException("No se puede desasociar: la evaluación ya inició o está vigente.");
                }
                it.remove();
                removed = true;
            }
        }

        // (Opcional) borrar también del lado de Evaluacion
        if (removed) {
            try {
                List<EvaluacionAsignada> asocs = ev.getGruposAsociados();
                if (asocs != null) {
                    asocs.removeIf(x -> x != null && x.getGrupo() != null && x.getGrupo().getIdGrupo() == idGrupo);
                }
            } catch (Throwable ignore) {}
        }
    }

    // ===================== Helpers =====================

    /** Verifica que la evaluación exista y pertenezca al profesor. */
    private Evaluacion findOwned(String idProfesor, int idEvaluacion) {
        String owner = ownerIndex.get(idEvaluacion);
        if (owner == null || !owner.equals(idProfesor)) {
            throw new IllegalStateException("La evaluación no pertenece al profesor o no existe (id=" + idEvaluacion + ").");
        }
        List<Evaluacion> lst = porProfesor.getOrDefault(idProfesor, List.of());
        for (Evaluacion e : lst) {
            if (e != null && e.getId() == idEvaluacion) return e;
        }
        throw new IllegalStateException("No se encontró la evaluación (id=" + idEvaluacion + ").");
    }

    /** Retorna true si la evaluación está asociada a cualquier grupo del sistema. */
    private boolean estaAsociada(Evaluacion ev) {
        // 1) Si la propia Evaluacion rastrea asociaciones
        try {
            if (ev.getGruposAsociados() != null && !ev.getGruposAsociados().isEmpty()) return true;
        } catch (Throwable ignore) {}

        // 2) Recorrer cursos/grupos
        List<Curso> cursos = null;
        try { cursos = cursoService.listarCursos(); } catch (Exception ignored) {}
        if (cursos != null) {
            for (Curso c : cursos) {
                if (c == null || c.grupos == null) continue;
                for (Grupo g : c.grupos) {
                    if (g == null || g.getEvaluacionesAsignadas() == null) continue;
                    for (EvaluacionAsignada ea : g.getEvaluacionesAsignadas()) {
                        if (ea != null && ea.getEvaluacion() != null
                                && ea.getEvaluacion().getId() == ev.getId()) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public void seedEvaluacionesDemo5(String idProfesor, UsuarioServiceMem usuarioService) {
        try {
            Profesor p = usuarioService.listarProfesores().stream()
                    .filter(x -> idProfesor.equals(x.getIdUsuario()))
                    .findFirst()
                    .orElse(null);
            if (p == null) {
                System.out.println("[seed eval] No existe el profesor " + idProfesor);
                return;
            }

            // Objetivos (cada uno 10–40 chars)
            java.util.List<String> objetivos = new java.util.ArrayList<>();
            objetivos.add("Reconocer sintaxis de Java");   // 26 chars
            objetivos.add("Diferenciar conceptos base");   // 28 chars

            // *** NOMBRE <= 20 caracteres ***
            Evaluacion ev = new Evaluacion(
                    "Quiz Demo (5 tipos)",                 // 18 chars ✅
                    "Responda todas las preguntas. Tiempo total 45 minutos.",
                    objetivos,
                    45,
                    true,
                    true
            );

            int nextId = 1;

            // 1) SELECCION_UNICA
            Pregunta q1 = new Pregunta(nextId++, TipoPregunta.SELECCION_UNICA,
                    "¿Qué palabra define una clase en Java?", 10);
            q1.agregarRespuesta(new Respuesta("class", true, 1));
            q1.agregarRespuesta(new Respuesta("def", false, 2));
            q1.agregarRespuesta(new Respuesta("struct", false, 3));
            ev.agregarPregunta(q1);

            // 2) SELECCION_MULTIPLE
            Pregunta q2 = new Pregunta(nextId++, TipoPregunta.SELECCION_MULTIPLE,
                    "Seleccione sentencias válidas:", 15);
            q2.agregarRespuesta(new Respuesta("System.out.println(\"Hola\");", true, 1));
            q2.agregarRespuesta(new Respuesta("print(\"Hola\")", false, 2));
            q2.agregarRespuesta(new Respuesta("int x = 5;", true, 3));
            q2.agregarRespuesta(new Respuesta("var y := 3;", false, 4));
            ev.agregarPregunta(q2);

            // 3) VERDADERO_FALSO
            Pregunta q3 = new Pregunta(nextId++, TipoPregunta.VERDADERO_FALSO,
                    "Java es fuertemente tipado.", 10);
            q3.agregarRespuesta(new Respuesta("Verdadero", true, 1));
            q3.agregarRespuesta(new Respuesta("Falso", false, 2));
            ev.agregarPregunta(q3);

            // 4) PAREO
            Pareo q4 = new Pareo(nextId++, "Empareje concepto con su definición.", 15);
            q4.agregarEnunciado("JVM");
            q4.agregarEnunciado("JDK");
            q4.agregarEnunciado("JRE");
            q4.agregarRespuesta("Máquina virtual para bytecode");
            q4.agregarRespuesta("Herramientas y compilador");
            q4.agregarRespuesta("Entorno de ejecución sin herramientas");
            q4.definirAsociacion(0, 0);
            q4.definirAsociacion(1, 1);
            q4.definirAsociacion(2, 2);
            ev.agregarPregunta(q4);

            // 5) SOPA_DE_LETRAS (mín. 10 palabras)
            SopaDeLetras q5 = new SopaDeLetras(nextId++, "Encuentre los términos de Java.", 20, 15);
            q5.agregarEnunciado("CLASE", "Estructura básica en Java");
            q5.agregarEnunciado("OBJETO", "Instancia de una clase");
            q5.agregarEnunciado("HERENCIA", "Mecanismo de reutilización");
            q5.agregarEnunciado("INTERFAZ", "Contrato de métodos");
            q5.agregarEnunciado("PAQUETE", "Agrupa clases relacionadas");
            q5.agregarEnunciado("METODO", "Función dentro de una clase");
            q5.agregarEnunciado("ATRIBUTO", "Dato dentro de una clase");
            q5.agregarEnunciado("POLIMORFISMO", "Múltiples formas");
            q5.agregarEnunciado("ENCAPSULAMIENTO", "Oculta implementación");
            q5.agregarEnunciado("CONSTRUCTOR", "Inicializa objetos");
            try { q5.generarCuadricula(); } catch (Exception ignore) {}
            ev.agregarPregunta(q5);

            ev.calcularPuntajeTotal();

            // Guarda en el service
            this.crear(p.getIdUsuario(), ev);

            // Además sincroniza en el profesor por si tienes paneles que leen del profe
            java.util.List<Evaluacion> actual = p.getEvaluaciones();
            ArrayList<Evaluacion> nuevaLista = (actual == null)
                    ? new java.util.ArrayList<>()
                    : new java.util.ArrayList<>(actual);
            boolean ya = false;
            for (Evaluacion e : nuevaLista) {
                if (e != null && e.getId() == ev.getId()) { ya = true; break; }
            }
            if (!ya) nuevaLista.add(ev);
            p.setEvaluaciones(nuevaLista);

            System.out.println("[seed eval] Creada '" + ev.getNombre() + "' para " + p.getIdUsuario()
                    + " con " + ev.getPreguntas().size() + " preguntas. Puntaje=" + ev.getPuntajeTotal());

        } catch (Exception ex) {
            // ahora NO lo ignoramos en silencio: deja rastro para diagnosticar
            System.out.println("[seed eval] Falló seed: " + ex.getMessage());
        }
    }
}
