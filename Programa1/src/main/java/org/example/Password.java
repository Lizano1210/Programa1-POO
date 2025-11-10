package org.example;

import org.mindrot.jbcrypt.BCrypt;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Clase que representa una contraseña de usuario en el sistema.
 * <p>
 * Permite encriptar, verificar y generar contraseñas temporales
 * utilizando el algoritmo {@link BCrypt}. También incluye funciones
 * para generar caracteres aleatorios (letras, números y símbolos).
 * </p>
 */
public class Password {

    // -- Atributos --

    /** Identificador del usuario propietario de la contraseña. */
    private String idUsuario;

    /** Contraseña encriptada del usuario. */
    private String password;

    /** Indica si la contraseña es temporal. */
    private boolean temp;

    /** Lista de letras mayúsculas y minúsculas. */
    private final ArrayList<String> letras = new ArrayList<>(Arrays.asList(
            "a", "A", "b", "B", "c", "C", "d", "D", "e", "E", "f", "F", "g", "G", "h", "H",
            "i", "I", "j", "J", "k", "K", "l", "L", "m", "M", "n", "N", "o", "O", "p", "P",
            "q", "Q", "r", "R", "s", "S", "t", "T", "u", "U", "v", "V", "w", "W", "x", "X",
            "y", "Y", "z", "Z"));

    /** Lista de números del 0 al 9. */
    private final ArrayList<String> numeros = new ArrayList<>(Arrays.asList(
            "0", "1", "2", "3", "4", "5", "6", "7", "8", "9"));

    /** Lista de símbolos especiales. */
    private final ArrayList<String> simbolos = new ArrayList<>(Arrays.asList(
            "!", "@", "#", "$", "%", "&", "*", "(", ")", "-", "_", "=", "+", "[", "]", "{", "}",
            "|", ";", ":", "'", ",", ".", "<", ">", "/", "?", "~", "`"));

    // -- Constructor --

    /**
     * Crea un nuevo objeto de tipo {@code Password}.
     *
     * @param pIdUsuario identificador del usuario
     * @param pPassword contraseña original o encriptada
     * @param pTemp indica si la contraseña es temporal
     */
    public Password(String pIdUsuario, String pPassword, boolean pTemp) {
        this.idUsuario = pIdUsuario;
        this.password = pPassword;
        this.temp = pTemp;
    }

    // -- Getters y Setters --

    public String getIdUsuario() { return idUsuario; }
    public void setIdUsuario(String pIdUsuario) { this.idUsuario = pIdUsuario; }

    public String getPassword() { return password; }
    public void setPassword(String pPassword) { this.password = pPassword; }

    public boolean getTemp() { return temp; }
    public void setTemp(boolean pTemp) { this.temp = pTemp; }

    // -- Métodos principales --

    /**
     * Devuelve una representación en texto de la información básica de la contraseña.
     *
     * @return cadena con los datos principales
     */
    @Override
    public String toString() {
        return "\n-- PASSWORD --\n" +
                "ID: " + idUsuario +
                "\nTemporal: " + temp;
    }

    /**
     * Encripta la contraseña usando el algoritmo {@link BCrypt}.
     *
     * @param password texto plano de la contraseña a encriptar
     */
    public void encriptar(String password) {
        this.password = BCrypt.hashpw(password, BCrypt.gensalt());
    }

    /**
     * Verifica si una contraseña ingresada coincide con la contraseña encriptada.
     *
     * @param password contraseña en texto plano a verificar
     * @return {@code true} si la contraseña es correcta, {@code false} en caso contrario
     */
    public boolean verificar(String password) {
        return BCrypt.checkpw(password, this.password);
    }

    /**
     * Genera una nueva contraseña temporal con letras, números y símbolos aleatorios.
     * <p>
     * La nueva contraseña se encripta automáticamente y se marca como temporal.
     * </p>
     *
     * @return contraseña temporal generada en texto plano (sin encriptar)
     */
    public String tempPassword() {
        String tPsw = randomX(0) + randomX(0) + randomX(1) + randomX(1) +
                randomX(0) + randomX(0) + randomX(2) + randomX(2) +
                randomX(1) + randomX(1);

        encriptar(tPsw);
        this.temp = true;
        return tPsw;
    }

    // -- Auxiliares --

    /**
     * Devuelve un carácter aleatorio de acuerdo al tipo especificado.
     *
     * @param opcion tipo de carácter a generar:
     *               <ul>
     *                   <li>0 → letra</li>
     *                   <li>1 → número</li>
     *                   <li>2 → símbolo</li>
     *               </ul>
     * @return carácter aleatorio del tipo indicado
     */
    public String randomX(int opcion) {
        Random random = new Random();
        if (opcion == 0) {
            return letras.get(random.nextInt(letras.size()));
        } else if (opcion == 1) {
            return numeros.get(random.nextInt(numeros.size()));
        } else {
            return simbolos.get(random.nextInt(simbolos.size()));
        }
    }
}
