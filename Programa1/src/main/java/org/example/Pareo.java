package org.example;

import java.util.*;

/**
 * Pregunta de tipo emparejamiento entre conceptos y definiciones.
 * El estudiante debe relacionar enunciados de la columna izquierda
 * con respuestas de la columna derecha.
 * 
 * Archivo: Pareo.java
 */
public class Pareo implements IPregunta {

    int id;
    String descripcion;
    int puntos;
    List<String> enunciados = new ArrayList<>();
    List<String> respuestas = new ArrayList<>();
    Map<Integer, Integer> asociaciones = new HashMap<>(); // índice enunciado -> índice respuesta correcta

    public Pareo(int id, String descripcion, int puntos) {
        this.id = id;
        this.descripcion = (descripcion == null ? "" : descripcion.trim());
        this.puntos = puntos;
    }

    /**
     * Agrega un elemento a emparejar (columna izquierda).
     * 
     * @param enunciado Texto del enunciado (5-100 caracteres)
     * @return true si se agregó correctamente
     */
    public boolean agregarEnunciado(String enunciado) {
        if (enunciado == null || enunciado.trim().isEmpty()) {
            System.out.println("El enunciado no puede estar vacío.");
            return false;
        }
        String texto = enunciado.trim();
        if (texto.length() < 5 || texto.length() > 100) {
            System.out.println("El enunciado debe tener entre 5 y 100 caracteres.");
            return false;
        }
        enunciados.add(texto);
        return true;
    }

    /**
     * Agrega una opción en la columna derecha (puede ser correcta o distractor).
     * 
     * @param respuesta Texto de la respuesta (5-100 caracteres)
     * @return true si se agregó correctamente
     */
    public boolean agregarRespuesta(String respuesta) {
        if (respuesta == null || respuesta.trim().isEmpty()) {
            System.out.println("La respuesta no puede estar vacía.");
            return false;
        }
        String texto = respuesta.trim();
        if (texto.length() < 5 || texto.length() > 100) {
            System.out.println("La respuesta debe tener entre 5 y 100 caracteres.");
            return false;
        }
        respuestas.add(texto);
        return true;
    }

    /**
     * Define qué enunciado se asocia con qué respuesta correcta.
     * 
     * @param enunciadoIdx Índice del enunciado (0-based)
     * @param respuestaIdx Índice de la respuesta correcta (0-based)
     * @return true si la asociación se definió correctamente
     */
    public boolean definirAsociacion(int enunciadoIdx, int respuestaIdx) {
        if (enunciadoIdx < 0 || enunciadoIdx >= enunciados.size()) {
            System.out.println("Índice de enunciado fuera de rango.");
            return false;
        }
        if (respuestaIdx < 0 || respuestaIdx >= respuestas.size()) {
            System.out.println("Índice de respuesta fuera de rango.");
            return false;
        }
        asociaciones.put(enunciadoIdx, respuestaIdx);
        return true;
    }

    /**
     * Genera el orden aleatorio de las respuestas para mostrar al estudiante.
     * 
     * @param rng Generador de números aleatorios (null usa uno nuevo)
     * @return Lista con los índices en orden aleatorio
     */
    public List<Integer> generarOrdenRespuestas(Random rng) {
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < respuestas.size(); i++) {
            indices.add(i);
        }
        Collections.shuffle(indices, rng == null ? new Random() : rng);
        return indices;
    }

    /**
     * Califica la respuesta del estudiante comparando sus asociaciones
     * con las correctas. Otorga puntos proporcionales.
     * 
     * @param respuesta Respuesta del estudiante con sus emparejamientos
     * @return Puntos obtenidos (puede ser parcial)
     */
    @Override
    public int calificar(RespuestaEstudiante respuesta) {
        if (respuesta == null) return 0;
        
        // Obtener las asociaciones del estudiante
        // Asumimos que RespuestaEstudiante tiene un método para esto
        // Por ahora usamos ordenesSeleccionados como lista de pares [enunciadoIdx, respuestaIdx]
        List<Integer> selecciones = respuesta.getOrdenesSeleccionados();
        if (selecciones == null || selecciones.isEmpty()) return 0;

        int correctas = 0;
        int totalAsociaciones = asociaciones.size();

        // Validar emparejamientos (formato esperado: pares consecutivos)
        for (int i = 0; i < selecciones.size() - 1; i += 2) {
            int enunciadoIdx = selecciones.get(i);
            int respuestaIdx = selecciones.get(i + 1);

            // Verificar si la asociación es correcta
            Integer respuestaCorrecta = asociaciones.get(enunciadoIdx);
            if (respuestaCorrecta != null && respuestaCorrecta == respuestaIdx) {
                correctas++;
            }
        }

        // Calificación proporcional
        if (totalAsociaciones == 0) return 0;
        double proporcion = (double) correctas / totalAsociaciones;
        return (int) Math.round(puntos * proporcion);
    }

    @Override
    public boolean validarDatos() {
        // Debe tener al menos 2 enunciados
        if (enunciados == null || enunciados.size() < 2) {
            System.out.println("Debe haber al menos 2 enunciados.");
            return false;
        }

        // Debe tener al menos tantas respuestas como enunciados
        if (respuestas == null || respuestas.size() < enunciados.size()) {
            System.out.println("Debe haber al menos tantas respuestas como enunciados.");
            return false;
        }

        // Cada enunciado debe tener una asociación definida
        for (int i = 0; i < enunciados.size(); i++) {
            if (!asociaciones.containsKey(i)) {
                System.out.println("El enunciado " + i + " no tiene asociación definida.");
                return false;
            }
        }

        // Los puntos deben ser válidos
        if (puntos < 1) {
            System.out.println("Los puntos deben ser al menos 1.");
            return false;
        }

        // La descripción debe ser válida
        if (descripcion == null || descripcion.trim().isEmpty()) {
            System.out.println("La descripción no puede estar vacía.");
            return false;
        }

        return true;
    }

    // ---------------- Implementación IPregunta ----------------
    @Override
    public int obtenerPuntos() {
        return puntos;
    }

    @Override
    public String obtenerDescripcion() {
        return descripcion;
    }

    @Override
    public TipoPregunta getTipo() {
        return TipoPregunta.PAREO;
    }

    // ---------------- Getters básicos ----------------
    public int getId() {
        return id;
    }

    public List<String> getEnunciados() {
        return Collections.unmodifiableList(enunciados);
    }

    public List<String> getRespuestas() {
        return Collections.unmodifiableList(respuestas);
    }

    public Map<Integer, Integer> getAsociaciones() {
        return Collections.unmodifiableMap(asociaciones);
    }

    public void setPuntos(int puntos) {
        this.puntos = puntos;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = (descripcion == null ? "" : descripcion.trim());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Pareo{");
        sb.append("id=").append(id);
        sb.append(", descripcion='").append(descripcion).append('\'');
        sb.append(", puntos=").append(puntos);
        sb.append(", enunciados=").append(enunciados.size());
        sb.append(", respuestas=").append(respuestas.size());
        sb.append(", asociaciones=").append(asociaciones.size());
        sb.append('}');
        return sb.toString();
    }
}
