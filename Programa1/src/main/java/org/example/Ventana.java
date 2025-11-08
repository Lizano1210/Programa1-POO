package org.example;
import javax.swing.*;
import java.awt.*;

/**
 * Ventana principal con CardLayout para alternar vistas (LOGIN/ROL).
 * Crea menú y orquesta navegación básica.
 */
public class Ventana extends JFrame {

    private final JPanel mainContainer = new JPanel(new CardLayout());
    private final Autenticacion auth = new Autenticacion();

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Ventana().setVisible(true));
    }

    public Ventana() {
        super("Sistema de Matrícula y Calificaciones");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(800, 600));
        setLocationRelativeTo(null);

        setJMenuBar(crearMenu());
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(mainContainer, BorderLayout.CENTER);

        inicializarCards();
        mostrar("LOGIN");
        pack();
    }

    /** Define las tarjetas iniciales en el contenedor principal. */
    private void inicializarCards() {
        // LOGIN
        LoginPanel login = new LoginPanel();
        login.setOnLogin(this::procesarLogin);
        login.setOnRecover(this::procesarRecuperacion);
        mainContainer.add(login, "LOGIN");

        // Placeholders de dashboards (luego reemplazas por paneles reales)
        mainContainer.add(crearPlaceholder("Dashboard Estudiante"), "ESTUDIANTE");
        mainContainer.add(crearPlaceholder("Dashboard Profesor"), "PROFESOR");

        // Administrador
        UsuarioServiceMem usuarioService = new UsuarioServiceMem(auth);
        usuarioService.seedDemo(); // datos de prueba

        AdminDashboardPanel admin = new AdminDashboardPanel(usuarioService);
        mainContainer.add(admin, "ADMIN");

    }

    /** Procesa login con Autenticacion y muestra la vista del rol o error. */
    private void procesarLogin(String identificacion, char[] password) {
        Roles rol = auth.login(identificacion, password).rol;
        LoginPanel login = (LoginPanel) obtenerCard("LOGIN");
        if (rol == null) {
            if (login != null) login.setMensaje("Credenciales inválidas o rol desconocido.");
            return;
        }
        if (login != null) login.setMensaje(" ");
        switch (rol) {
            case ESTUDIANTE -> mostrar("ESTUDIANTE");
            case PROFESOR   -> mostrar("PROFESOR");
            case ADMIN      -> mostrar("ADMIN");
        }
    }

    /** Procesa recuperación de contraseña (stub). */
    private void procesarRecuperacion(String identificacion) {
        LoginPanel login = (LoginPanel) obtenerCard("LOGIN");
        boolean ok = auth.recuperarContrasena(identificacion);
        if (login != null) {
            login.setMensaje(ok
                    ? "Se envió una contraseña temporal al correo registrado."
                    : "Ingrese una identificación válida para recuperar.");
        }
    }

    /** Muestra una tarjeta por nombre. */
    private void mostrar(String name) {
        CardLayout cl = (CardLayout) mainContainer.getLayout();
        cl.show(mainContainer, name);
    }

    /** Obtiene el componente de una card por nombre (para mensajes). */
    private Component obtenerCard(String name) {
        for (Component c : mainContainer.getComponents()) {
            if (name.equals(mainContainer.getLayout().getClass() == CardLayout.class ? ((CardLayout) mainContainer.getLayout()).toString() : "")) {
                // no confiable; mejor iterar con nombres guardados — usamos getComponents directo
            }
        }
        // Recorremos y devolvemos por comparación directa con el layout
        // (en Swing no hay API pública para mapear name→comp; usamos un truco simple)
        // Alternativa: guardar referencias en un Map<String, Component>.
        for (Component c : mainContainer.getComponents()) {
            if (name.equals(mainContainer.getClientProperty(c))) { /* no disponible */ }
        }
        // Implementación simple: guardamos referencias al crear si lo necesitas.
        // Para este mensaje solo necesitamos el login recién creado, así que:
        for (Component c : mainContainer.getComponents()) {
            if (c instanceof LoginPanel) return c;
        }
        return null;
    }

    /** Crea un panel temporal para visualizar la navegación. */
    private JPanel crearPlaceholder(String titulo) {
        JPanel p = new JPanel(new GridBagLayout());
        JLabel lbl = new JLabel(titulo);
        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD, 22f));
        p.add(lbl);
        return p;
    }

    /** Construye el menú superior básico. */
    private JMenuBar crearMenu() {
        JMenuBar mb = new JMenuBar();
        JMenu archivo = new JMenu("Archivo");
        JMenuItem salir = new JMenuItem("Salir");
        salir.addActionListener(e -> dispose());
        archivo.add(salir);

        JMenu ayuda = new JMenu("Ayuda");
        JMenuItem acerca = new JMenuItem("Acerca de");
        acerca.addActionListener(e -> JOptionPane.showMessageDialog(this, "Versión 1.0"));
        ayuda.add(acerca);

        mb.add(archivo);
        mb.add(ayuda);
        return mb;
    }
}