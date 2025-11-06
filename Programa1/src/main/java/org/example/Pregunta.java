package org.example;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Pregunta cerrada: selección única, múltiple o verdadero/falso.
 * Implementa IPregunta.
 */
public class Pregunta implements IPregunta {

    int id;
    TipoPregunta tipo;
    String descripcion;
    int puntos;
    List<Respuesta> respuestas = new ArrayList<>(); // opciones visibles

    public Pregunta(int id, TipoPregunta tipo, String descripcion, int puntos) {
        this.id = id;
        this.tipo = Objects.requireNonNull(tipo, "Tipo requerido");
        this.descripcion = (descripcion == null ? "" : descripcion.trim());
        this.puntos = puntos;
    }

    public boolean agregarRespuesta(Respuesta r) {
        if (r == null || r.getTexto() == null || r.getTexto().trim().isEmpty()) {
            System.out.println("Respuesta inválida.");
            return false;
        }
        if (r.getOrden() <= 0) r.setOrden(respuestas.size() + 1);
        respuestas.add(r);
        return true;
    }

    // Devuelve respuestas correctas
    public List<Respuesta> obtenerRespuestasCorrectas() {
        List<Respuesta> out = new ArrayList<>();
        for (Respuesta r : respuestas) if (r != null && r.isCorrecta()) out.add(r);
        return out;
    }

    /**
     * Califica comparando los "orden" seleccionados por el estudiante
     * contra los "orden" de las respuestas correctas.
     * Reglas:
     * - SELECCION_UNICA: debe haber exactamente 1 correcta y 1 seleccionada que coincida.
     * - SELECCION_MULTIPLE: debe coincidir el conjunto exacto.
     * - VERDADERO_FALSO: exactamente 2 opciones y 1 correcta; una selección válida.
     */
    @Override
    public int calificar(RespuestaEstudiante respuestaEst) {
        if (respuestaEst == null) return 0;
        List<Integer> sel = respuestaEst.getOrdenesSeleccionados();
        if (sel == null) return 0;

        // Construye set/list de correctas por "orden"
        List<Integer> correctas = new ArrayList<>();
        for (Respuesta r : respuestas) {
            if (r != null && r.isCorrecta()) {
                correctas.add(r.getOrden());
            }
        }

        switch (tipo) {
            case SELECCION_UNICA -> {
                if (correctas.size() != 1) return 0;
                if (sel.size() != 1) return 0;
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


        @Override
        public boolean validarDatos () {
            if (puntos < 1) return false;
            if (descripcion == null || descripcion.isBlank()) return false;
            if (respuestas == null || respuestas.isEmpty()) return false;

            int correctas = 0;
            for (Respuesta r : respuestas) if (r != null && r.isCorrecta()) correctas++;

            return switch (tipo) {
                case SELECCION_UNICA -> respuestas.size() >= 2 && correctas == 1;
                case SELECCION_MULTIPLE -> respuestas.size() >= 2 && correctas >= 1;
                case VERDADERO_FALSO -> respuestas.size() == 2 && correctas == 1;
                default -> false; // otros tipos no aplican a esta clase
            };
        }

        // ---------------- Implementación IPregunta ----------------
        @Override public int obtenerPuntos () {
            return puntos;
        }
        @Override public String obtenerDescripcion () {
            return descripcion;
        }
        @Override public TipoPregunta getTipo () {
            return tipo;
        }

        // ---------------- Getters/Setters básicos ----------------
        public int getId () {
            return id;
        }
        public void setDescripcion (String descripcion){
            this.descripcion = (descripcion == null ? "" : descripcion.trim());
        }
        public void setPuntos ( int puntos){
            this.puntos = puntos;
        }
        public List<Respuesta> getRespuestas () {
            return Collections.unmodifiableList(respuestas);
        }
    }
