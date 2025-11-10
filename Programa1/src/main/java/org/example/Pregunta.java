package org.example;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Representa una pregunta cerrada de tipo selección única, múltiple o verdadero/falso.
 * <p>
 * Esta clase implementa la interfaz {@link IPregunta} y define el comportamiento común
 * de las preguntas que tienen un conjunto limitado de opciones de respuesta visibles.
 * </p>
 */
public class Pregunta implements IPregunta {

    // -- Atributos --

    /** Identificador único de la pregunta. */
    private int id;

    /** Tipo de pregunta (única, múltiple o verdadero/falso). */
    private TipoPregunta tipo;

    /** Texto o enunciado principal de la pregunta. */
    private String descripcion;

    /** Puntaje total asignado a la pregunta. */
    private int puntos;

    /** Lista de respuestas posibles (opciones mostradas al estudiante). */
    private final List<Respuesta> respuestas = new ArrayList<>();

    // -- Constructor --

    /**
     * Crea una nueva pregunta cerrada.
     *
     * @param id identificador único
     * @param tipo tipo de pregunta (debe ser válido)
     * @param descripcion texto descriptivo de la pregunta
     * @param puntos valor en puntos que otorga
     */
    public Pregunta(int id, TipoPregunta tipo, String descripcion, int puntos) {
        this.id = id;
        this.tipo = Objects.requireNonNull(tipo, "Tipo requerido");
        this.descripcion = (descripcion == null ? "" : descripcion.trim());
        this.puntos = puntos;
    }

    // -- Gestión de respuestas --

    /**
     * Agrega una respuesta a la lista de opciones visibles.
     * <p>
     * Si la respuesta no tiene un número de orden asignado, se genera automáticamente.
     * </p>
     *
     * @param r respuesta a agregar
     * @return {@code true} si se agregó correctamente
     */
    public boolean agregarRespuesta(Respuesta r) {
        if (r == null || r.getTexto() == null || r.getTexto().trim().isEmpty()) {
            System.out.println("Respuesta inválida.");
            return false;
        }
        if (r.getOrden() <= 0) r.setOrden(respuestas.size() + 1);
        respuestas.add(r);
        return true;
    }

    /**
     * Devuelve la lista de respuestas correctas.
     *
     * @return lista de respuestas marcadas como correctas
     */
    public List<Respuesta> obtenerRespuestasCorrectas() {
        List<Respuesta> out = new ArrayList<>();
        for (Respuesta r : respuestas)
            if (r != null && r.isCorrecta()) out.add(r);
        return out;
    }

    // -- Calificación --

    /**
     * Califica las respuestas dadas por el estudiante comparando sus selecciones
     * con las respuestas correctas de la pregunta.
     * <p>
     * Reglas:
     * <ul>
     *     <li><b>SELECCION_UNICA</b>: Debe haber exactamente una opción correcta
     *         y una seleccionada que coincidan.</li>
     *     <li><b>SELECCION_MULTIPLE</b>: Debe coincidir exactamente el conjunto
     *         de respuestas seleccionadas con el conjunto de correctas.</li>
     *     <li><b>VERDADERO_FALSO</b>: Debe haber dos opciones y una correcta.</li>
     * </ul>
     * </p>
     *
     * @param respuestaEst respuesta del estudiante
     * @return puntos obtenidos (0 si es incorrecta)
     */
    @Override
    public int calificar(RespuestaEstudiante respuestaEst) {
        if (respuestaEst == null) return 0;
        List<Integer> sel = respuestaEst.getOrdenesSeleccionados();
        if (sel == null) return 0;

        List<Integer> correctas = new ArrayList<>();
        for (Respuesta r : respuestas)
            if (r != null && r.isCorrecta()) correctas.add(r.getOrden());

        switch (tipo) {
            case SELECCION_UNICA -> {
                if (correctas.size() != 1 || sel.size() != 1) return 0;
                return sel.get(0).equals(correctas.get(0)) ? puntos : 0;
            }
            case SELECCION_MULTIPLE -> {
                if (correctas.isEmpty()) return 0;
                List<Integer> a = new ArrayList<>(sel);
                List<Integer> b = new ArrayList<>(correctas);
                Collections.sort(a);
                Collections.sort(b);
                return a.equals(b) ? puntos : 0;
            }
            case VERDADERO_FALSO -> {
                if (respuestas.size() != 2 || correctas.size() != 1) return 0;
                if (sel.size() != 1) return 0;
                return sel.get(0).equals(correctas.get(0)) ? puntos : 0;
            }
            default -> {
                return 0;
            }
        }
    }

    // -- Validación --

    /**
     * Verifica que la pregunta tenga datos válidos según su tipo.
     *
     * @return {@code true} si cumple con los criterios mínimos
     */
    @Override
    public boolean validarDatos() {
        if (puntos < 1) return false;
        if (descripcion == null || descripcion.isBlank()) return false;
        if (respuestas == null || respuestas.isEmpty()) return false;

        int correctas = 0;
        for (Respuesta r : respuestas)
            if (r != null && r.isCorrecta()) correctas++;

        return switch (tipo) {
            case SELECCION_UNICA -> respuestas.size() >= 2 && correctas == 1;
            case SELECCION_MULTIPLE -> respuestas.size() >= 2 && correctas >= 1;
            case VERDADERO_FALSO -> respuestas.size() == 2 && correctas == 1;
            default -> false;
        };
    }

    // -- Implementación de IPregunta --

    @Override public int obtenerPuntos() { return puntos; }

    @Override public String obtenerDescripcion() { return descripcion; }

    @Override public TipoPregunta getTipo() { return tipo; }

    // -- Getters y Setters --

    public int getId() { return id; }

    public void setDescripcion(String descripcion) {
        this.descripcion = (descripcion == null ? "" : descripcion.trim());
    }

    public void setPuntos(int puntos) { this.puntos = puntos; }

    public List<Respuesta> getRespuestas() {
        return Collections.unmodifiableList(respuestas);
    }
}
