package org.example;

public class Respuesta {
    // Atributos
    String texto; // visible al estudiante
    boolean esCorrecta;
    int orden;

    public Respuesta(String texto, boolean esCorrecta, int orden) {
        this.texto = texto == null ? "" : texto.trim();
        this.esCorrecta = esCorrecta;
        this.orden = orden;
    }

    // Getters y Setters
    public String getTexto() { return texto; }
    public void setTexto(String texto) { this.texto = texto == null ? "" : texto.trim(); }

    public boolean isCorrecta() { return esCorrecta; }
    public void setCorrecta(boolean correcta) { esCorrecta = correcta; }

    public int getOrden() { return orden; }
    public void setOrden(int orden) { this.orden = orden; }

    @Override
    public String toString() {
        return "Respuesta{orden=" + orden + ", correcta=" + esCorrecta + ", texto='" + texto + "'}";
    }
}
