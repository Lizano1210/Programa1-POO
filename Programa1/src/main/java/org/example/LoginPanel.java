package org.example;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.function.BiConsumer;

/**
 * Panel gráfico de inicio de sesión con estilo simple tipo "card".
 * <p>
 * Mantiene exactamente la misma lógica y API pública: callbacks de login,
 * recuperación y manejo de mensajes; solo se ajusta la presentación visual.
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
     * Crea el formulario de inicio de sesión con estilo visual.
     * <p>
     * El contenido se presenta centrado dentro de una "card" con padding,
     * título centrado, y espaciado consistente entre controles.
     * </p>
     */
    public LoginPanel() {
        // Fondo general suave y contenedor centrado
        setLayout(new GridBagLayout());
        setBackground(new Color(245, 247, 250)); // gris muy claro

        JPanel card = new JPanel(new GridBagLayout());
        card.setOpaque(true);
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(
                new LineBorder(new Color(220, 224, 230), 1, true), // borde suave y redondeado
                new EmptyBorder(16, 18, 18, 18) // padding interno
        ));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 8, 8, 8);
        c.fill = GridBagConstraints.HORIZONTAL;

        // -- Fila 0: Título centrado --
        JLabel title = new JLabel("Iniciar sesión");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 22f));
        title.setHorizontalAlignment(SwingConstants.CENTER);

        c.gridx = 0; c.gridy = 0; c.gridwidth = 3;
        c.weightx = 1.0; c.fill = GridBagConstraints.HORIZONTAL;
        card.add(title, c);

        // -- Fila 1: Identificación --
        c.gridwidth = 1; c.weightx = 0;
        JLabel lblId = new JLabel("Identificación");
        c.gridx = 0; c.gridy = 1;
        card.add(lblId, c);

        c.gridx = 1; c.gridy = 1; c.weightx = 1.0;
        txtIdentificacion.setToolTipText("Ingrese su identificación");
        card.add(txtIdentificacion, c);

        c.gridx = 2; c.gridy = 1; c.weightx = 0;
        card.add(Box.createHorizontalStrut(1), c);

        // -- Fila 2: Contraseña + mostrar --
        JLabel lblPass = new JLabel("Contraseña");
        c.gridx = 0; c.gridy = 2; c.weightx = 0;
        card.add(lblPass, c);

        c.gridx = 1; c.gridy = 2; c.weightx = 1.0;
        pwdContrasena.setToolTipText("Ingrese su contraseña");
        card.add(pwdContrasena, c);

        c.gridx = 2; c.gridy = 2; c.weightx = 0;
        chkMostrar.setOpaque(false);
        card.add(chkMostrar, c);

        // -- Fila 3: Botón Ingresar --
        c.gridx = 0; c.gridy = 3; c.gridwidth = 3; c.weightx = 1.0;
        JPanel rowBtns = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        rowBtns.setOpaque(false);
        estilizarBoton(btnIngresar);
        estilizarBotonSecundario(btnRecuperar);
        rowBtns.add(btnIngresar);
        rowBtns.add(btnRecuperar);
        card.add(rowBtns, c);

        // -- Fila 4: Mensaje centrado --
        c.gridx = 0; c.gridy = 4; c.gridwidth = 3; c.weightx = 1.0;
        lblMensaje.setForeground(new Color(180, 0, 0));
        lblMensaje.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(lblMensaje, c);

        // Centrar la "card" en el panel principal
        GridBagConstraints root = new GridBagConstraints();
        root.gridx = 0; root.gridy = 0;
        root.anchor = GridBagConstraints.CENTER;
        add(card, root);

        // Guardar el carácter por defecto de ocultamiento
        this.echoDefault = pwdContrasena.getEchoChar();

        // Mostrar u ocultar contraseña
        chkMostrar.addActionListener(e ->
                pwdContrasena.setEchoChar(chkMostrar.isSelected() ? (char) 0 : echoDefault));

        // Acciones de los botones (misma lógica)
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

    // -- Estilos auxiliares (solo presentación) --

    /** Estilo primario para botones (relleno y tipografía). */
    private void estilizarBoton(JButton b) {
        b.setFocusPainted(false);
        b.setFont(b.getFont().deriveFont(Font.BOLD));
        b.setBackground(new Color(33, 150, 243));
        b.setForeground(Color.WHITE);
        b.setBorder(new EmptyBorder(8, 14, 8, 14));
    }

    /** Estilo secundario para botones planos. */
    private void estilizarBotonSecundario(JButton b) {
        b.setFocusPainted(false);
        b.setFont(b.getFont().deriveFont(Font.PLAIN));
        b.setContentAreaFilled(false);
        b.setBorder(new EmptyBorder(8, 10, 8, 10));
        b.setForeground(new Color(33, 150, 243));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }
}




