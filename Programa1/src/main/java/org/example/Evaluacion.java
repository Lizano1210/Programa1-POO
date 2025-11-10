package org.example;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Define una evaluación con preguntas, duración y reglas de aleatoriedad.
 */
public class Evaluacion {
    // Atributos
    static int contador = 1;           // Para no repetir id    
    int id;
    String nombre;
    String instrucciones;
    List<String> objetivos = new ArrayList<>();
    int duracionMinutos;
    boolean preguntasAleatorias;
    boolean opcionesAleatorias;
    List<IPregunta> preguntas = new ArrayList<>();
    int puntajeTotal;
    List<EvaluacionAsignada> gruposAsociados = new ArrayList<>();

    // Constructor
    public Evaluacion(String nombre, String instrucciones, List<String> objetivos, int duracionMinutos,
                      boolean preguntasAleatorias, boolean opcionesAleatorias) {
        this.id = contador++;
        validarDatos(nombre, instrucciones, objetivos, duracionMinutos, preguntasAleatorias, opcionesAleatorias);
        calcularPuntajeTotal(); // arranca en 0
    }

    // Valida y añade una pregunta
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

    public int calcularPuntajeTotal() {
        int suma = 0;
        for (IPregunta p : preguntas) {
            if (p != null) {suma += Math.max(0, p.obtenerPuntos());}
        }
        this.puntajeTotal = suma;
        return this.puntajeTotal;
    }

    /**
     * Devuelve el orden de preguntas a mostrar.
     * Si preguntasAleatorias=true, retorna una copia mezclada; si no, el orden natural.
     */
    public List<IPregunta> generarOrdenPreguntas(Random rng) {
        List<IPregunta> orden = new ArrayList<>(preguntas);
        if (preguntasAleatorias) {
            Collections.shuffle(orden, rng == null ? new Random() : rng);
        }
        return orden;
    }

    /** Valida y asigna los datos base de la evaluación. */
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

    /**
     * Retorna false si la evaluación está asociada a algún grupo VIGENTE para la fecha dada.
     * (Atajo práctico a partir de la definición de vigencia de grupos en reportes).
     */
    public boolean canModificar(LocalDate fechaReferencia) {
        if (gruposAsociados == null || gruposAsociados.isEmpty()) return true;
        if (fechaReferencia == null) fechaReferencia = LocalDate.now();
        for (EvaluacionAsignada ea : gruposAsociados) {
            if (ea == null || ea.grupo == null) continue;
            if (ea.grupo.getFechaFinal() != null && !ea.grupo.getFechaFinal().isBefore(fechaReferencia)) {
                return false; // hay al menos un grupo vigente NO se puede modificar
            }
        }
        return true;
    }

    public boolean canDesasociar(EvaluacionAsignada asignacion) {
        if (asignacion == null || asignacion.fechaHoraInicio == null) return false;
        return asignacion.fechaHoraInicio.isAfter(LocalDateTime.now());
    }

    /** Versión agregada: true si TODAS las asociaciones pueden desasociarse "hoy". */
    public boolean canDesasociar() {
        if (gruposAsociados == null || gruposAsociados.isEmpty()) return true;
        LocalDateTime now = LocalDateTime.now();
        for (EvaluacionAsignada ea : gruposAsociados) {
            if (ea == null || ea.fechaHoraInicio == null) return false;
            if (!ea.fechaHoraInicio.isAfter(now)) return false;
        }
        return true;
    }

    // Getters
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

    // Setters
    public void setId(int id) {
        this.id = id;
    }

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
        if (preguntas == null) {
            this.preguntas = new ArrayList<>();
        } else {
            this.preguntas = new ArrayList<>(preguntas);
        }
        calcularPuntajeTotal();
    }

    public void setPuntajeTotal(int puntajeTotal) {
        this.puntajeTotal = puntajeTotal;
    }

    public void setGruposAsociados(List<EvaluacionAsignada> gruposAsociados) {
        if (gruposAsociados == null) {
            this.gruposAsociados = new ArrayList<>();
        } else {
            this.gruposAsociados = new ArrayList<>(gruposAsociados);
        }
    }

    // Auxiliares
    public void moverPregunta(int from, int to) {
        if (preguntas == null) return;
        int n = preguntas.size();
        if (from < 0 || to < 0 || from >= n || to >= n || from == to) return;
        java.util.Collections.swap(preguntas, from, to);
        calcularPuntajeTotal();
    }

    public void eliminarPregunta(int idx) {
        if (preguntas == null) return;
        if (idx < 0 || idx >= preguntas.size()) return;
        preguntas.remove(idx);
        calcularPuntajeTotal();
    }


    @Override public String toString() {
        return "Evaluación{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", duracion=" + duracionMinutos +
                ", preguntas=" + preguntas.size() +
                ", puntajeTotal=" + puntajeTotal +
                '}';
    }
}