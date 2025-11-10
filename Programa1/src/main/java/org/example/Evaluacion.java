package org.example;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Representa una evaluación que contiene preguntas, duración y configuración
 * de aleatoriedad en preguntas y opciones.
 * <p>
 * Permite gestionar preguntas, calcular puntajes, validar datos y controlar
 * su asociación con grupos de aplicación.
 * </p>
 */
public class Evaluacion {

    // -- Atributos --

    /** Contador interno para generar IDs únicos. */
    static int contador = 1;

    /** Identificador de la evaluación. */
    int id;

    /** Nombre de la evaluación (5–20 caracteres). */
    String nombre;

    /** Instrucciones visibles para el estudiante (5–400 caracteres). */
    String instrucciones;

    /** Lista de objetivos del aprendizaje (cada uno de 10–40 caracteres). */
    List<String> objetivos = new ArrayList<>();

    /** Duración total de la evaluación en minutos (mínimo 1). */
    int duracionMinutos;

    /** Indica si el orden de las preguntas se mostrará de forma aleatoria. */
    boolean preguntasAleatorias;

    /** Indica si el orden de las opciones dentro de las preguntas será aleatorio. */
    boolean opcionesAleatorias;

    /** Lista de preguntas incluidas en la evaluación. */
    List<IPregunta> preguntas = new ArrayList<>();

    /** Puntaje total calculado automáticamente según las preguntas. */
    int puntajeTotal;

    /** Grupos con los que la evaluación está asociada. */
    List<EvaluacionAsignada> gruposAsociados = new ArrayList<>();

    // -- Constructor --

    /**
     * Crea una nueva evaluación con los parámetros especificados.
     *
     * @param nombre nombre de la evaluación
     * @param instrucciones texto con las instrucciones
     * @param objetivos lista de objetivos de aprendizaje
     * @param duracionMinutos duración total en minutos
     * @param preguntasAleatorias si las preguntas deben mostrarse en orden aleatorio
     * @param opcionesAleatorias si las opciones de respuesta deben mostrarse en orden aleatorio
     */
    public Evaluacion(String nombre, String instrucciones, List<String> objetivos, int duracionMinutos,
                      boolean preguntasAleatorias, boolean opcionesAleatorias) {
        this.id = contador++;
        validarDatos(nombre, instrucciones, objetivos, duracionMinutos, preguntasAleatorias, opcionesAleatorias);
        calcularPuntajeTotal(); // inicia con 0
    }

    // -- Gestión de preguntas --

    /**
     * Agrega una nueva pregunta a la evaluación.
     *
     * @param p pregunta a agregar
     * @return {@code true} si se agregó correctamente
     */
    public boolean agregarPregunta(IPregunta p) {
        if (p == null) {
            System.out.println("La pregunta no puede ser nula.");
            return false;
        }
        if (!p.validarDatos()) {
            System.out.println("La pregunta no cumple validaciones.");
            return false;
        }
        preguntas.add(p);
        calcularPuntajeTotal();
        return true;
    }

    /**
     * Calcula y actualiza el puntaje total de la evaluación.
     *
     * @return puntaje total
     */
    public int calcularPuntajeTotal() {
        int suma = 0;
        for (IPregunta p : preguntas) {
            if (p != null) suma += Math.max(0, p.obtenerPuntos());
        }
        this.puntajeTotal = suma;
        return this.puntajeTotal;
    }

    /**
     * Genera el orden de preguntas según la configuración de aleatoriedad.
     *
     * @param rng instancia opcional de {@link Random} para mezclar
     * @return lista de preguntas en el orden correspondiente
     */
    public List<IPregunta> generarOrdenPreguntas(Random rng) {
        List<IPregunta> orden = new ArrayList<>(preguntas);
        if (preguntasAleatorias) {
            Collections.shuffle(orden, rng == null ? new Random() : rng);
        }
        return orden;
    }

    // -- Validaciones --

    /**
     * Valida y asigna los datos base de la evaluación.
     *
     * @return {@code true} si los datos son válidos
     * @throws IllegalArgumentException si alguno de los valores no cumple las reglas
     */
    public boolean validarDatos(String nombre, String instrucciones, List<String> objetivos, int duracionMinutos,
                                boolean preguntasAleatorias, boolean opcionesAleatorias) {
        // nombre 5-20
        if (nombre == null || nombre.trim().length() < 5 || nombre.trim().length() > 20)
            throw new IllegalArgumentException("Nombre inválido (5-20).");
        // instrucciones 5-400
        if (instrucciones == null || instrucciones.trim().length() < 5 || instrucciones.trim().length() > 400)
            throw new IllegalArgumentException("Instrucciones inválidas (5-400).");
        // objetivos: cada uno 10-40
        if (objetivos == null || objetivos.isEmpty())
            throw new IllegalArgumentException("Objetivos requeridos.");
        for (String obj : objetivos) {
            if (obj == null) throw new IllegalArgumentException("Objetivo nulo.");
            int len = obj.trim().length();
            if (len < 10 || len > 40)
                throw new IllegalArgumentException("Objetivo inválido (10-40): " + obj);
        }
        // duración >=1 (temporizador)
        if (duracionMinutos < 1)
            throw new IllegalArgumentException("Duración inválida (>=1).");

        // asignamos si sirve
        this.nombre = nombre.trim();
        this.instrucciones = instrucciones.trim();
        this.objetivos = new ArrayList<>();
        for (String s : objetivos) this.objetivos.add(s.trim());
        this.duracionMinutos = duracionMinutos;
        this.preguntasAleatorias = preguntasAleatorias;
        this.opcionesAleatorias = opcionesAleatorias;
        return true;
    }

    // -- Restricciones y estado --

    /**
     * Verifica si la evaluación puede modificarse en una fecha determinada.
     * <p>
     * No se permite modificar si está asociada a algún grupo vigente en la fecha indicada.
     * </p>
     *
     * @param fechaReferencia fecha de referencia (si es {@code null}, usa la fecha actual)
     * @return {@code true} si puede modificarse
     */
    public boolean canModificar(LocalDate fechaReferencia) {
        if (gruposAsociados == null || gruposAsociados.isEmpty()) return true;
        if (fechaReferencia == null) fechaReferencia = LocalDate.now();
        for (EvaluacionAsignada ea : gruposAsociados) {
            if (ea == null || ea.getGrupo() == null) continue;
            if (ea.getGrupo().getFechaFinal() != null && !ea.getGrupo().getFechaFinal().isBefore(fechaReferencia)) {
                return false; // grupo vigente: no se puede modificar
            }
        }
        return true;
    }

    /**
     * Indica si una evaluación puede desasociarse de un grupo antes de que comience.
     *
     * @param asignacion evaluación asignada a un grupo
     * @return {@code true} si puede desasociarse
     */
    public boolean canDesasociar(EvaluacionAsignada asignacion) {
        if (asignacion == null || asignacion.getFechaHoraInicio() == null) return false;
        return asignacion.getFechaHoraInicio().isAfter(LocalDateTime.now());
    }

    /**
     * Versión agregada: indica si todas las asociaciones pueden desasociarse hoy.
     *
     * @return {@code true} si todas las asociaciones son eliminables
     */
    public boolean canDesasociar() {
        if (gruposAsociados == null || gruposAsociados.isEmpty()) return true;
        LocalDateTime now = LocalDateTime.now();
        for (EvaluacionAsignada ea : gruposAsociados) {
            if (ea == null || ea.getFechaHoraInicio() == null) return false;
            if (!ea.getFechaHoraInicio().isAfter(now)) return false;
        }
        return true;
    }

    // -- Getters y Setters --

    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public String getInstrucciones() { return instrucciones; }
    public List<String> getObjetivos() { return Collections.unmodifiableList(objetivos); }
    public int getDuracionMinutos() { return duracionMinutos; }
    public boolean isPreguntasAleatorias() { return preguntasAleatorias; }
    public boolean isOpcionesAleatorias() { return opcionesAleatorias; }
    public List<IPregunta> getPreguntas() { return Collections.unmodifiableList(preguntas); }
    public int getPuntajeTotal() { return puntajeTotal; }
    public List<EvaluacionAsignada> getGruposAsociados() { return Collections.unmodifiableList(gruposAsociados); }

    public void setId(int id) { this.id = id; }

    public void setNombre(String nombre) {
        if (nombre == null || nombre.trim().length() < 5 || nombre.trim().length() > 20)
            throw new IllegalArgumentException("Nombre inválido (5-20).");
        this.nombre = nombre.trim();
    }

    public void setInstrucciones(String instrucciones) {
        if (instrucciones == null || instrucciones.trim().length() < 5 || instrucciones.trim().length() > 400)
            throw new IllegalArgumentException("Instrucciones inválidas (5-400).");
        this.instrucciones = instrucciones.trim();
    }

    public void setObjetivos(List<String> objetivos) {
        if (objetivos == null || objetivos.isEmpty())
            throw new IllegalArgumentException("Objetivos requeridos.");
        for (String obj : objetivos) {
            if (obj == null) throw new IllegalArgumentException("Objetivo nulo.");
            int len = obj.trim().length();
            if (len < 10 || len > 40)
                throw new IllegalArgumentException("Objetivo inválido (10-40): " + obj);
        }
        this.objetivos = new ArrayList<>();
        for (String s : objetivos) this.objetivos.add(s.trim());
    }

    public void setDuracionMinutos(int duracionMinutos) {
        if (duracionMinutos < 1)
            throw new IllegalArgumentException("Duración inválida (>=1).");
        this.duracionMinutos = duracionMinutos;
    }

    public void setPreguntasAleatorias(boolean preguntasAleatorias) {
        this.preguntasAleatorias = preguntasAleatorias;
    }

    public void setOpcionesAleatorias(boolean opcionesAleatorias) {
        this.opcionesAleatorias = opcionesAleatorias;
    }

    public void setPreguntas(List<IPregunta> preguntas) {
        this.preguntas = preguntas == null ? new ArrayList<>() : new ArrayList<>(preguntas);
        calcularPuntajeTotal();
    }

    public void setPuntajeTotal(int puntajeTotal) { this.puntajeTotal = puntajeTotal; }

    public void setGruposAsociados(List<EvaluacionAsignada> gruposAsociados) {
        this.gruposAsociados = gruposAsociados == null ? new ArrayList<>() : new ArrayList<>(gruposAsociados);
    }

    // -- Auxiliares --

    /**
     * Cambia el orden de una pregunta dentro de la lista.
     *
     * @param from posición original
     * @param to posición destino
     */
    public void moverPregunta(int from, int to) {
        if (preguntas == null) return;
        int n = preguntas.size();
        if (from < 0 || to < 0 || from >= n || to >= n || from == to) return;
        Collections.swap(preguntas, from, to);
        calcularPuntajeTotal();
    }

    /**
     * Elimina una pregunta por su índice.
     *
     * @param idx índice de la pregunta a eliminar
     */
    public void eliminarPregunta(int idx) {
        if (preguntas == null) return;
        if (idx < 0 || idx >= preguntas.size()) return;
        preguntas.remove(idx);
        calcularPuntajeTotal();
    }

    @Override
    public String toString() {
        return "Evaluación{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", duracion=" + duracionMinutos +
                ", preguntas=" + preguntas.size() +
                ", puntajeTotal=" + puntajeTotal +
                '}';
    }
}
