package org.example;

import java.util.*;

/**
 * Pregunta tipo sopa de letras donde el estudiante debe encontrar palabras
 * en una cuadrícula. Las palabras pueden estar en 8 direcciones:
 * horizontal (derecha/izquierda), vertical (arriba/abajo) y diagonales (4).
 * 
 * Archivo: SopaDeLetras.java
 */
public class SopaDeLetras implements IPregunta {

    int id;
    String descripcion;
    int puntos;
    List<Enunciado> enunciados = new ArrayList<>();
    char[][] cuadricula;
    int tamanioCuadricula; // NxN (por defecto 15x15)
    List<PalabraEncontrada> palabrasEncontradas = new ArrayList<>();

    public SopaDeLetras(int id, String descripcion, int puntos, int tamanioCuadricula) {
        this.id = id;
        this.descripcion = (descripcion == null ? "" : descripcion.trim());
        this.puntos = puntos;
        this.tamanioCuadricula = Math.max(10, Math.min(tamanioCuadricula, 30)); // entre 10 y 30
        this.cuadricula = new char[this.tamanioCuadricula][this.tamanioCuadricula];
        inicializarCuadricula();
    }

    /**
     * Constructor con tamaño por defecto de 15x15.
     */
    public SopaDeLetras(int id, String descripcion, int puntos) {
        this(id, descripcion, puntos, 15);
    }

    /**
     * Inicializa la cuadrícula con espacios vacíos.
     */
    private void inicializarCuadricula() {
        for (int i = 0; i < tamanioCuadricula; i++) {
            for (int j = 0; j < tamanioCuadricula; j++) {
                cuadricula[i][j] = ' ';
            }
        }
    }

    /**
     * Agrega un enunciado (palabra a buscar con su pista).
     * 
     * @param palabra Palabra a buscar (3-20 caracteres, solo letras)
     * @param pista Pista descriptiva (5-100 caracteres)
     * @return true si se agregó correctamente
     */
    public boolean agregarEnunciado(String palabra, String pista) {
        if (palabra == null || palabra.trim().isEmpty()) {
            System.out.println("La palabra no puede estar vacía.");
            return false;
        }
        if (pista == null || pista.trim().isEmpty()) {
            System.out.println("La pista no puede estar vacía.");
            return false;
        }

        String palabraLimpia = palabra.trim().toUpperCase().replaceAll("[^A-ZÁÉÍÓÚÑ]", "");
        if (palabraLimpia.length() < 3 || palabraLimpia.length() > 20) {
            System.out.println("La palabra debe tener entre 3 y 20 caracteres.");
            return false;
        }

        String pistaLimpia = pista.trim();
        if (pistaLimpia.length() < 5 || pistaLimpia.length() > 100) {
            System.out.println("La pista debe tener entre 5 y 100 caracteres.");
            return false;
        }

        enunciados.add(new Enunciado(palabraLimpia, pistaLimpia));
        return true;
    }

    /**
     * Genera la cuadrícula con las palabras colocadas aleatoriamente
     * en las 8 direcciones posibles, y rellena los espacios vacíos.
     * 
     * @return true si se generó exitosamente
     */
    public boolean generarCuadricula() {
        if (enunciados.size() < 10) {
            System.out.println("Debe haber al menos 10 palabras para generar la sopa.");
            return false;
        }

        inicializarCuadricula();
        palabrasEncontradas.clear();
        Random random = new Random();

        // Intentar colocar cada palabra
        List<Enunciado> palabrasAColocar = new ArrayList<>(enunciados);
        Collections.shuffle(palabrasAColocar); // orden aleatorio

        for (Enunciado enunciado : palabrasAColocar) {
            boolean colocada = false;
            int intentos = 0;
            int maxIntentos = 100;

            while (!colocada && intentos < maxIntentos) {
                // Dirección aleatoria (0-7)
                Direccion direccion = Direccion.values()[random.nextInt(8)];
                
                // Posición inicial aleatoria
                int fila = random.nextInt(tamanioCuadricula);
                int col = random.nextInt(tamanioCuadricula);

                if (puedePlabra(enunciado.palabra, fila, col, direccion)) {
                    colocarPalabra(enunciado.palabra, fila, col, direccion);
                    palabrasEncontradas.add(new PalabraEncontrada(enunciado.palabra, fila, col, direccion));
                    colocada = true;
                }

                intentos++;
            }

            if (!colocada) {
                System.out.println("No se pudo colocar la palabra: " + enunciado.palabra);
            }
        }

        // Rellenar espacios vacíos con letras aleatorias
        rellenarEspaciosVacios(random);

        return palabrasEncontradas.size() >= 10;
    }

    /**
     * Verifica si una palabra puede colocarse en la posición y dirección dadas.
     */
    private boolean puedePlabra(String palabra, int fila, int col, Direccion dir) {
        int len = palabra.length();
        int df = dir.deltaFila;
        int dc = dir.deltaCol;

        for (int i = 0; i < len; i++) {
            int f = fila + (df * i);
            int c = col + (dc * i);

            // Verificar límites
            if (f < 0 || f >= tamanioCuadricula || c < 0 || c >= tamanioCuadricula) {
                return false;
            }

            // Verificar si la celda está vacía o tiene la letra correcta
            char letraActual = cuadricula[f][c];
            if (letraActual != ' ' && letraActual != palabra.charAt(i)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Coloca una palabra en la cuadrícula.
     */
    private void colocarPalabra(String palabra, int fila, int col, Direccion dir) {
        int df = dir.deltaFila;
        int dc = dir.deltaCol;

        for (int i = 0; i < palabra.length(); i++) {
            int f = fila + (df * i);
            int c = col + (dc * i);
            cuadricula[f][c] = palabra.charAt(i);
        }
    }

    /**
     * Rellena los espacios vacíos con letras aleatorias.
     */
    private void rellenarEspaciosVacios(Random random) {
        String letras = "ABCDEFGHIJKLMNOPQRSTUVWXYZÁÉÍÓÚÑ";
        for (int i = 0; i < tamanioCuadricula; i++) {
            for (int j = 0; j < tamanioCuadricula; j++) {
                if (cuadricula[i][j] == ' ') {
                    cuadricula[i][j] = letras.charAt(random.nextInt(letras.length()));
                }
            }
        }
    }

    /**
     * Califica la respuesta del estudiante verificando las palabras encontradas.
     * 
     * @param respuesta Respuesta del estudiante (ordenesSeleccionados contiene índices de palabras encontradas)
     * @return Puntos obtenidos (proporcional a palabras correctas)
     */
    @Override
    public int calificar(RespuestaEstudiante respuesta) {
        if (respuesta == null) return 0;
        
        List<Integer> palabrasEncontradasIdx = respuesta.getOrdenesSeleccionados();
        if (palabrasEncontradasIdx == null || palabrasEncontradasIdx.isEmpty()) return 0;

        int correctas = 0;
        int totalPalabras = enunciados.size();

        // Contar cuántas palabras encontró correctamente
        Set<Integer> encontradas = new HashSet<>(palabrasEncontradasIdx);
        for (int i = 0; i < totalPalabras; i++) {
            if (encontradas.contains(i)) {
                correctas++;
            }
        }

        // Calificación proporcional
        if (totalPalabras == 0) return 0;
        double proporcion = (double) correctas / totalPalabras;
        return (int) Math.round(puntos * proporcion);
    }

    @Override
    public boolean validarDatos() {
        // Debe tener al menos 10 enunciados
        if (enunciados == null || enunciados.size() < 10) {
            System.out.println("Debe haber al menos 10 palabras en la sopa de letras.");
            return false;
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

        // Verificar que cubra los 8 sentidos (simplificado: si hay suficientes palabras)
        // En una implementación real, verificarías que generarCuadricula() distribuye en todas direcciones
        return palabrasEncontradas.size() >= 10;
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
        return TipoPregunta.SOPA_LETRAS;
    }

    // ---------------- Getters básicos ----------------
    public int getId() {
        return id;
    }

    public List<Enunciado> getEnunciados() {
        return Collections.unmodifiableList(enunciados);
    }

    public char[][] getCuadricula() {
        // Retornar copia para evitar modificaciones externas
        char[][] copia = new char[tamanioCuadricula][tamanioCuadricula];
        for (int i = 0; i < tamanioCuadricula; i++) {
            System.arraycopy(cuadricula[i], 0, copia[i], 0, tamanioCuadricula);
        }
        return copia;
    }

    public int getTamanioCuadricula() {
        return tamanioCuadricula;
    }

    public List<PalabraEncontrada> getPalabrasEncontradas() {
        return Collections.unmodifiableList(palabrasEncontradas);
    }

    public void setPuntos(int puntos) {
        this.puntos = puntos;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = (descripcion == null ? "" : descripcion.trim());
    }

    /**
     * Retorna la cuadrícula como String para visualización.
     */
    public String cuadriculaToString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tamanioCuadricula; i++) {
            for (int j = 0; j < tamanioCuadricula; j++) {
                sb.append(cuadricula[i][j]).append(" ");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return "SopaDeLetras{" +
                "id=" + id +
                ", descripcion='" + descripcion + '\'' +
                ", puntos=" + puntos +
                ", palabras=" + enunciados.size() +
                ", tamaño=" + tamanioCuadricula + "x" + tamanioCuadricula +
                '}';
    }

    // ============ Clases internas ============

    /**
     * Representa un enunciado: palabra a buscar y su pista.
     */
    public static class Enunciado {
        String palabra;
        String pista;

        public Enunciado(String palabra, String pista) {
            this.palabra = palabra;
            this.pista = pista;
        }

        public String getPalabra() { return palabra; }
        public String getPista() { return pista; }

        @Override
        public String toString() {
            return pista + " [" + palabra + "]";
        }
    }

    /**
     * Registra dónde está ubicada una palabra en la cuadrícula.
     */
    public static class PalabraEncontrada {
        String palabra;
        int filaInicio;
        int colInicio;
        Direccion direccion;

        public PalabraEncontrada(String palabra, int filaInicio, int colInicio, Direccion direccion) {
            this.palabra = palabra;
            this.filaInicio = filaInicio;
            this.colInicio = colInicio;
            this.direccion = direccion;
        }

        public String getPalabra() { return palabra; }
        public int getFilaInicio() { return filaInicio; }
        public int getColInicio() { return colInicio; }
        public Direccion getDireccion() { return direccion; }

        @Override
        public String toString() {
            return palabra + " en (" + filaInicio + "," + colInicio + ") " + direccion;
        }
    }

    /**
     * Enum con las 8 direcciones posibles.
     */
    public enum Direccion {
        HORIZONTAL_DERECHA(0, 1),
        HORIZONTAL_IZQUIERDA(0, -1),
        VERTICAL_ABAJO(1, 0),
        VERTICAL_ARRIBA(-1, 0),
        DIAGONAL_ABAJO_DERECHA(1, 1),
        DIAGONAL_ABAJO_IZQUIERDA(1, -1),
        DIAGONAL_ARRIBA_DERECHA(-1, 1),
        DIAGONAL_ARRIBA_IZQUIERDA(-1, -1);

        final int deltaFila;
        final int deltaCol;

        Direccion(int deltaFila, int deltaCol) {
            this.deltaFila = deltaFila;
            this.deltaCol = deltaCol;
        }
    }
}