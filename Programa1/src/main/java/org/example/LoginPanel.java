package org.example;

import javax.swing.*;
import java.awt.*;
import java.util.function.BiConsumer;

/**
 * Panel gráfico de inicio de sesión.
 * <p>
 * Permite al usuario ingresar su identificación y contraseña, con opción para mostrar/ocultar la contraseña.
 * Incluye botones para iniciar sesión y recuperar contraseña, con callbacks configurables
 * para manejar las acciones correspondientes.
 * </p>
 */
public class LoginPanel extends JPanel {

    // -- Componentes principales --

    private final JTextField txtIdentificacion = new JTextField(18);
    private final JPasswordField pwdContrasena = new JPasswordField(18);
    private final JButton btnIngresar = new JButton("Ingresar");
    private final JButton btnRecuperar = new JButton("Olvidé mi contraseña");
    private final JCheckBox chkMostrar = new JCheckBox("Mostrar");
    private final JLabel lblMensaje = new JLabel(" ");

    /** Caracter usado para ocultar la contraseña (por defecto). */
    private final char echoDefault;

    // -- Callbacks externos --

    /** Acción a ejecutar al intentar iniciar sesión. */
    private BiConsumer<String, char[]> onLogin;

    /** Acción a ejecutar al solicitar recuperación de contraseña. */
    private java.util.function.Consumer<String> onRecover;

    // -- Constructor --

    /**
     * Crea el formulario de inicio de sesión con todos sus componentes.
     * <p>
     * Organiza los elementos en una cuadrícula, asignando posiciones y configurando los botones,
     * el campo de contraseña y el checkbox para mostrar/ocultar caracteres.
     * </p>
     */
    public LoginPanel() {
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("Iniciar sesión");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));

        // -- Fila 0: título --
        c.gridx = 0; c.gridy = 0; c.gridwidth = 3;
        add(title, c);

        // -- Fila 1: identificación --
        c.gridwidth = 1; c.weightx = 0;
        JLabel lblId = new JLabel("Identificación");
        c.gridx = 0; c.gridy = 1;
        add(lblId, c);

        c.gridx = 1; c.gridy = 1; c.weightx = 1;
        add(txtIdentificacion, c);

        c.gridx = 2; c.gridy = 1; c.weightx = 0;
        add(Box.createHorizontalStrut(1), c);

        // -- Fila 2: contraseña y checkbox --
        JLabel lblPass = new JLabel("Contraseña");
        c.gridx = 0; c.gridy = 2; c.weightx = 0;
        add(lblPass, c);

        c.gridx = 1; c.gridy = 2; c.weightx = 1;
        add(pwdContrasena, c);

        c.gridx = 2; c.gridy = 2; c.weightx = 0;
        add(chkMostrar, c);

        // -- Fila 3: botón ingresar --
        c.gridx = 1; c.gridy = 3; c.gridwidth = 1;
        add(btnIngresar, c);

        // -- Fila 4: botón recuperar contraseña --
        c.gridx = 1; c.gridy = 4;
        add(btnRecuperar, c);

        // -- Fila 5: mensajes --
        c.gridx = 0; c.gridy = 5; c.gridwidth = 3;
        lblMensaje.setForeground(new Color(180, 0, 0));
        add(lblMensaje, c);

        // Guardar el carácter por defecto de ocultamiento
        this.echoDefault = pwdContrasena.getEchoChar();

        // Mostrar u ocultar contraseña
        chkMostrar.addActionListener(e ->
                pwdContrasena.setEchoChar(chkMostrar.isSelected() ? (char) 0 : echoDefault));

        // Acciones de los botones
        btnIngresar.addActionListener(e -> intentarLogin());
        btnRecuperar.addActionListener(e -> {
            if (onRecover != null) onRecover.accept(txtIdentificacion.getText().trim());
        });
    }

    // -- Configuración de callbacks --

    /** Define la acción a ejecutar al hacer clic en “Ingresar”. */
    public void setOnLogin(BiConsumer<String, char[]> onLogin) { this.onLogin = onLogin; }

    /** Define la acción a ejecutar al hacer clic en “Olvidé mi contraseña”. */
    public void setOnRecover(java.util.function.Consumer<String> onRecover) { this.onRecover = onRecover; }

    // -- Utilidades --

    /** Muestra un mensaje informativo o de error en la interfaz. */
    public void setMensaje(String msg) { lblMensaje.setText(msg == null ? " " : msg); }

    /**
     * Intenta validar los datos ingresados y ejecutar la acción de inicio de sesión.
     * <p>
     * Si los campos están vacíos, muestra mensajes de error localmente sin llamar al callback.
     * </p>
     */
    private void intentarLogin() {
        String id = txtIdentificacion.getText().trim();
        char[] pass = pwdContrasena.getPassword();

        if (id.isEmpty()) {
            setMensaje("Ingrese su identificación.");
            txtIdentificacion.requestFocusInWindow();
            return;
        }
        if (pass == null || pass.length == 0) {
            setMensaje("Ingrese su contraseña.");
            pwdContrasena.requestFocusInWindow();
            return;
        }

        if (onLogin != null) {
            try {
                onLogin.accept(id, pass);
            } catch (IllegalArgumentException ex) {
                setMensaje(ex.getMessage() == null ? "Datos inválidos." : ex.getMessage());
            } catch (Exception ex) {
                setMensaje("Ocurrió un error al iniciar sesión.");
            }
        }
    }
}



