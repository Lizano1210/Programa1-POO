package org.example;

import java.util.*;

/**
 * Representa una pregunta de tipo emparejamiento (pareo) entre conceptos y definiciones.
 * <p>
 * El estudiante debe asociar correctamente los elementos de la columna izquierda (enunciados)
 * con las opciones de la columna derecha (respuestas). Cada emparejamiento correcto otorga
 * una fracción del puntaje total.
 * </p>
 */
public class Pareo implements IPregunta {

    // -- Atributos --

    /** Identificador único de la pregunta. */
    private int id;

    /** Texto descriptivo o enunciado principal. */
    private String descripcion;

    /** Puntaje total asignado a la pregunta. */
    private int puntos;

    /** Lista de enunciados (columna izquierda). */
    private final List<String> enunciados = new ArrayList<>();

    /** Lista de respuestas disponibles (columna derecha). */
    private final List<String> respuestas = new ArrayList<>();

    /** Mapa que define las asociaciones correctas: índice del enunciado → índice de respuesta. */
    private final Map<Integer, Integer> asociaciones = new HashMap<>();

    // -- Constructor --

    /**
     * Crea una nueva pregunta de tipo pareo.
     *
     * @param id identificador único
     * @param descripcion enunciado de la pregunta
     * @param puntos puntaje total que otorga
     */
    public Pareo(int id, String descripcion, int puntos) {
        this.id = id;
        this.descripcion = (descripcion == null ? "" : descripcion.trim());
        this.puntos = puntos;
    }

    // -- Gestión de enunciados y respuestas --

    /**
     * Agrega un enunciado (columna izquierda).
     *
     * @param enunciado texto del enunciado (entre 5 y 100 caracteres)
     * @return {@code true} si se agregó correctamente
     */
    public boolean agregarEnunciado(String enunciado) {
        if (enunciado == null || enunciado.trim().isEmpty()) return false;
        String texto = enunciado.trim();
        if (texto.length() < 5 || texto.length() > 100) return false;
        enunciados.add(texto);
        return true;
    }

    /**
     * Agrega una respuesta (columna derecha).
     *
     * @param respuesta texto de la respuesta (entre 5 y 100 caracteres)
     * @return {@code true} si se agregó correctamente
     */
    public boolean agregarRespuesta(String respuesta) {
        if (respuesta == null || respuesta.trim().isEmpty()) return false;
        String texto = respuesta.trim();
        if (texto.length() < 5 || texto.length() > 100) return false;
        respuestas.add(texto);
        return true;
    }

    /**
     * Define la asociación correcta entre un enunciado y una respuesta.
     *
     * @param enunciadoIdx índice del enunciado (0 basado)
     * @param respuestaIdx índice de la respuesta correcta (0 basado)
     * @return {@code true} si la asociación se definió correctamente
     */
    public boolean definirAsociacion(int enunciadoIdx, int respuestaIdx) {
        if (enunciadoIdx < 0 || enunciadoIdx >= enunciados.size()) return false;
        if (respuestaIdx < 0 || respuestaIdx >= respuestas.size()) return false;
        asociaciones.put(enunciadoIdx, respuestaIdx);
        return true;
    }

    // -- Utilidades de presentación --

    /**
     * Genera un orden aleatorio de las respuestas para mostrar al estudiante.
     *
     * @param rng generador de números aleatorios (puede ser {@code null})
     * @return lista con los índices de respuesta en orden aleatorio
     */
    public List<Integer> generarOrdenRespuestas(Random rng) {
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < respuestas.size(); i++) {
            indices.add(i);
        }
        Collections.shuffle(indices, rng == null ? new Random() : rng);
        return indices;
    }

    // -- Calificación --

    /**
     * Califica las asociaciones del estudiante comparándolas con las correctas.
     * <p>
     * Cada emparejamiento correcto otorga una fracción proporcional del puntaje total.
     * </p>
     *
     * @param respuesta respuesta del estudiante que contiene sus emparejamientos
     * @return puntos obtenidos (pueden ser parciales)
     */
    @Override
    public int calificar(RespuestaEstudiante respuesta) {
        if (respuesta == null) return 0;
        List<Integer> selecciones = respuesta.getOrdenesSeleccionados();
        if (selecciones == null || selecciones.isEmpty()) return 0;

        int correctas = 0;
        int total = asociaciones.size();

        for (int i = 0; i < selecciones.size() - 1; i += 2) {
            int enunciadoIdx = selecciones.get(i);
            int respuestaIdx = selecciones.get(i + 1);
            Integer correcta = asociaciones.get(enunciadoIdx);
            if (correcta != null && correcta == respuestaIdx) correctas++;
        }

        if (total == 0) return 0;
        double proporcion = (double) correctas / total;
        return (int) Math.round(puntos * proporcion);
    }

    // -- Validación --

    /**
     * Verifica que la pregunta esté correctamente definida.
     *
     * @return {@code true} si todos los datos son válidos
     */
    @Override
    public boolean validarDatos() {
        if (enunciados.size() < 2) return false;
        if (respuestas.size() < enunciados.size()) return false;
        if (asociaciones.size() != enunciados.size()) return false;
        if (puntos < 1) return false;
        return descripcion != null && !descripcion.trim().isEmpty();
    }

    // -- Implementación de IPregunta --

    @Override
    public int obtenerPuntos() { return puntos; }

    @Override
    public String obtenerDescripcion() { return descripcion; }

    @Override
    public TipoPregunta getTipo() { return TipoPregunta.PAREO; }

    // -- Getters y Setters --

    public int getId() { return id; }

    public List<String> getEnunciados() { return Collections.unmodifiableList(enunciados); }

    public List<String> getRespuestas() { return Collections.unmodifiableList(respuestas); }

    public Map<Integer, Integer> getAsociaciones() { return Collections.unmodifiableMap(asociaciones); }

    public void setPuntos(int puntos) { this.puntos = puntos; }

    public void setDescripcion(String descripcion) {
        this.descripcion = (descripcion == null ? "" : descripcion.trim());
    }

    // -- Representación --

    @Override
    public String toString() {
        return String.format("Pareo{id=%d, enunciados=%d, respuestas=%d, puntos=%d}",
                id, enunciados.size(), respuestas.size(), puntos);
    }
}
