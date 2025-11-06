package org.example;

public interface IPregunta {
    int obtenerPuntos();
    String obtenerDescripcion();
    TipoPregunta getTipo();
    boolean validarDatos();
    int calificar(RespuestaEstudiante respuesta);
}
