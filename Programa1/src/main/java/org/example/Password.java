package org.example;

// Imports
import org.mindrot.jbcrypt.BCrypt;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Password {
    // Atributos
    String idUsuario;
    String password;
    boolean temp; // Para saber si es una contraseña temporal
    // Listas para elegir caracteres aleatorios.
    List<String> chars = Arrays.asList("a", "A", "b", "B", "c", "C", "d", "D", "e", "E", "f", "F", "g", "G", "h", "H", "i", "I", "j", "J", "k", "K", "l", "L", "m", "M", "n", "N", "o", "O", "p", "P", "q", "Q", "r", "R", "s", "S", "t", "T", "u", "U", "v", "V", "w", "W", "x", "X", "y", "Y", "z", "Z");
    ArrayList<String> letras = new ArrayList<>(chars);
    List<String> nums = Arrays.asList("0", "1", "2", "3", "4", "5", "6", "7", "8", "9");
    ArrayList<String> numeros = new ArrayList<>(nums);
    List<String> simb = Arrays.asList("!", "@", "#", "$", "%", "&", "*", "(", ")", "-", "_", "=", "+", "[", "]", "{", "}", "|", ";", ":", "'", ",", ".", "<", ">", "/", "?", "~", "`");
    ArrayList<String> simbolos = new ArrayList<>(simb);

    // Constructor
    public Password(String pIdUsuario, String pPassword, boolean pTemp) {
        this.idUsuario = pIdUsuario;
        this.password = pPassword;
        this.temp = pTemp;
    }

    // Setters y Getters
    public String getIdUsuario() {return idUsuario;}
    public  void  setIdUsuario(String pIdUsuario) {this.idUsuario = pIdUsuario;}
    public String getPassword() {return password;}
    public  void setPassword(String pPassword) {this.password = pPassword;}
    public boolean getTemp() {return temp;}
    public void setTemp(boolean pTemp) {this.temp = pTemp;}

    // Métodos

    public void encriptar(String password) {
        String nPsw; // Espacio de memoria para almacenar la contraseña encriptada
        nPsw = BCrypt.hashpw(password, BCrypt.gensalt());
        this.password = nPsw;
    }

    public boolean verificar(String password) {
        return BCrypt.checkpw(password, this.password);
    }

    public String tempPassword() {
        String tPsw;
        tPsw = randomX(0) + randomX(0) + randomX(1) + randomX(1) +
                randomX(0) + randomX(0) + randomX(2) + randomX(2) +
                randomX(1) + randomX(1);
        encriptar(tPsw);
        this.temp = true;
        return tPsw;
    }

    public String randomX(int opcion) {
        Random random = new Random();
        if (opcion == 0) { // Letras
            int index = random.nextInt(letras.size());
            return letras.get(index);
        } else if (opcion == 1) { // nums
            int index = random.nextInt(numeros.size());
            return numeros.get(index);
        } else { // Símbolos
            int index = random.nextInt(simbolos.size());
            return simbolos.get(index);
        }
    }
}
