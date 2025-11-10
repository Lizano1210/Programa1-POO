package org.example;

import javax.swing.*;
import java.awt.*;

/**
 * Ventana principal del sistema de matrícula y calificaciones.
 * <p>
 * Utiliza un {@link CardLayout} para alternar entre diferentes vistas o paneles
 * de usuario según su rol: administrador, profesor o estudiante.
 * </p>
 * <p>
 * Además, gestiona la autenticación, la inicialización de servicios, la carga
 * de datos de demostración y la creación del menú principal de la aplicación.
 * </p>
 */
public class Ventana extends JFrame {

    // -- Atributos principales --

    /** Contenedor principal con diseño por tarjetas (vistas intercambiables). */
    private final JPanel mainContainer = new JPanel(new CardLayout());

    /** Servicio de autenticación global del sistema. */
    private final Autenticacion auth = new Autenticacion();

    /** Servicios principales del sistema. */
    private UsuarioServiceMem usuarioService;
    private CursoService cursoService;
    private ReporteService reporteService;
    private EvaluacionService evaluacionService;
    private IntentoService intentoService = new IntentoServiceMem();

    /** Panel temporal para el dashboard del estudiante. */
    private Component cardEstudiantePlaceholder;

    // -- Punto de entrada --

    /** Inicia la aplicación. */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Ventana().setVisible(true));
    }

    // -- Constructor --

    /**
     * Crea la ventana principal, inicializando el entorno gráfico y los servicios base.
     */
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

    // -- Inicialización de vistas y servicios --

    /** Crea los paneles iniciales y carga datos de demostración. */
    private void inicializarCards() {
        LoginPanel login = new LoginPanel();
        login.setOnLogin(this::procesarLogin);
        login.setOnRecover(this::procesarRecuperacion);
        mainContainer.add(login, "LOGIN");

        cardEstudiantePlaceholder = crearPlaceholder("Dashboard Estudiante");
        mainContainer.add(cardEstudiantePlaceholder, "ESTUDIANTE");
        mainContainer.add(crearPlaceholder("Dashboard Profesor"), "PROFESOR");

        usuarioService = new UsuarioServiceMem(auth);
        usuarioService.seedDemo();

        cursoService = new CursoServiceMem(usuarioService);
        ((CursoServiceMem) cursoService).seedCursosDemo();
        ((CursoServiceMem) cursoService).seedGruposDemo(usuarioService);

        evaluacionService = new EvaluacionServiceMem(usuarioService, cursoService);
        ((EvaluacionServiceMem) evaluacionService).seedEvaluacionesDemo5("P200USER!", usuarioService);

        MatriculaService matriculaService = new MatriculaServiceMem(usuarioService, cursoService);
        reporteService = new ReporteServicePdf(cursoService, usuarioService);
        intentoService = new IntentoServiceMem();

        AdminDashboardPanel admin = new AdminDashboardPanel(usuarioService, cursoService, reporteService, auth);
        mainContainer.add(admin, "ADMIN");
    }

    // -- Autenticación --

    /**
     * Procesa el inicio de sesión según la identificación y contraseña ingresadas.
     *
     * @param identificacion ID del usuario
     * @param password contraseña ingresada
     */
    private void procesarLogin(String identificacion, char[] password) {
        Roles rol = auth.login(identificacion, password).rol;
        LoginPanel login = (LoginPanel) obtenerCard("LOGIN");
        if (rol == null) {
            if (login != null) login.setMensaje("Credenciales inválidas o rol desconocido.");
            return;
        }
        if (login != null) login.setMensaje(" ");

        switch (rol) {
            case ESTUDIANTE -> iniciarSesionEstudiante(identificacion, login);
            case PROFESOR -> iniciarSesionProfesor(identificacion, login);
            case ADMIN -> mostrar("ADMIN");
        }
    }

    // -- Manejo de roles --

    /** Inicia la sesión de un estudiante, configurando su panel principal. */
    private void iniciarSesionEstudiante(String identificacion, LoginPanel login) {
        Estudiante estActual = usuarioService.listarEstudiantes().stream()
                .filter(e -> identificacion.equals(e.getIdUsuario()))
                .findFirst()
                .orElse(null);

        if (estActual == null) {
            if (login != null) login.setMensaje("No se encontró el perfil de Estudiante.");
            return;
        }

        EstudianteDashboardPanel panelEst = new EstudianteDashboardPanel(
                usuarioService,
                cursoService,
                estActual,
                (Matricula m) -> {
                    if (m == null || m.getGrupo() == null) {
                        throw new IllegalStateException("Matrícula inválida: grupo no definido.");
                    }
                    Grupo g = m.getGrupo();
                    if (g.getCurso() == null) {
                        throw new IllegalStateException("El grupo no tiene curso asociado.");
                    }
                    java.util.List<Matricula> matsEst = estActual.obtenerMatriculas();
                    if (matsEst == null) {
                        matsEst = new java.util.ArrayList<>();
                        estActual.setMatriculas(matsEst);
                    } else if (!(matsEst instanceof java.util.ArrayList)) {
                        matsEst = new java.util.ArrayList<>(matsEst);
                        estActual.setMatriculas(matsEst);
                    }
                    for (Matricula mm : matsEst) {
                        if (mm.getGrupo() != null && mm.getGrupo().equals(g)) {
                            throw new IllegalStateException("Ya estás matriculado en este grupo.");
                        }
                    }
                    java.util.List<Matricula> matsGrupo = g.getMatriculas();
                    if (matsGrupo == null) {
                        matsGrupo = new java.util.ArrayList<>();
                        g.setMatriculas(matsGrupo);
                    } else if (!(matsGrupo instanceof java.util.ArrayList)) {
                        matsGrupo = new java.util.ArrayList<>(matsGrupo);
                        g.setMatriculas(matsGrupo);
                    }
                    int cupoMax = g.getCurso().getMaxEstu();
                    if (cupoMax > 0 && matsGrupo.size() >= cupoMax) {
                        throw new IllegalStateException("El grupo ya alcanzó el cupo máximo.");
                    }
                    matsEst.add(m);
                    matsGrupo.add(m);
                },
                () -> {
                    java.util.List<EvaluacionAsignada> out = new java.util.ArrayList<>();
                    if (estActual.obtenerMatriculas() != null) {
                        for (Matricula mat : estActual.obtenerMatriculas()) {
                            Grupo g = mat.getGrupo();
                            if (g != null && g.getEvaluacionesAsignadas() != null) {
                                out.addAll(g.getEvaluacionesAsignadas());
                            }
                        }
                    }
                    return out;
                },
                () -> intentoService.listarPorEstudiante(estActual.getIdUsuario()),
                intentoService::guardar
        );

        if (cardEstudiantePlaceholder != null) {
            mainContainer.remove(cardEstudiantePlaceholder);
            cardEstudiantePlaceholder = null;
        }
        mainContainer.add(panelEst, "ESTUDIANTE");
        mainContainer.revalidate();
        mainContainer.repaint();
        mostrar("ESTUDIANTE");
    }

    /** Inicia la sesión de un profesor y prepara su panel de trabajo. */
    private void iniciarSesionProfesor(String identificacion, LoginPanel login) {
        Profesor profActual = usuarioService.listarProfesores().stream()
                .filter(p -> identificacion.equals(p.getIdUsuario()))
                .findFirst()
                .orElse(null);

        if (profActual == null) {
            if (login != null) login.setMensaje("No se encontró el perfil de Profesor.");
            return;
        }

        ProfesorDashboardPanel panelProf = new ProfesorDashboardPanel(
                profActual,
                usuarioService,
                cursoService,
                evaluacionService,
                intentoService,
                reporteService
        );

        Component ya = null;
        for (Component c : mainContainer.getComponents()) {
            if (c instanceof ProfesorDashboardPanel) {
                ya = c;
                break;
            }
        }
        if (ya != null) mainContainer.remove(ya);

        mainContainer.add(panelProf, "PROFESOR");
        mainContainer.revalidate();
        mainContainer.repaint();
        mostrar("PROFESOR");
    }

    // -- Recuperación de contraseña --

    /**
     * Procesa la recuperación de contraseña del usuario.
     *
     * @param identificacion ID del usuario a recuperar
     */
    private void procesarRecuperacion(String identificacion) {
        LoginPanel login = (LoginPanel) obtenerCard("LOGIN");
        boolean ok = auth.recuperarContrasena(identificacion);
        if (login != null) {
            login.setMensaje(ok
                    ? "Se envió una contraseña temporal al correo registrado."
                    : "Ingrese una identificación válida para recuperar.");
        }
    }

    // -- Cierre de sesión --

    /** Cierra la sesión actual y regresa a la pantalla de inicio de sesión. */
    private void cerrarSesion() {
        try {
            Component posibleDashEst = null;
            for (Component c : mainContainer.getComponents()) {
                if (c instanceof EstudianteDashboardPanel) {
                    posibleDashEst = c;
                    break;
                }
            }
            if (posibleDashEst != null) {
                mainContainer.remove(posibleDashEst);
            }
            mainContainer.add(crearPlaceholder("Dashboard Estudiante"), "ESTUDIANTE");

            LoginPanel login = (LoginPanel) obtenerCard("LOGIN");
            if (login != null) {
                login.setMensaje(" ");
            }

            mostrar("LOGIN");
            mainContainer.revalidate();
            mainContainer.repaint();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "No se pudo cerrar la sesión.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // -- Utilidades de interfaz --

    /** Muestra una vista específica dentro del contenedor principal. */
    private void mostrar(String name) {
        CardLayout cl = (CardLayout) mainContainer.getLayout();
        cl.show(mainContainer, name);
    }

    /** Busca y devuelve un panel específico por su nombre. */
    private Component obtenerCard(String name) {
        for (Component c : mainContainer.getComponents()) {
            if (c instanceof LoginPanel) return c;
        }
        return null;
    }

    /** Crea un panel genérico con texto centrado (placeholders). */
    private JPanel crearPlaceholder(String titulo) {
        JPanel p = new JPanel(new GridBagLayout());
        JLabel lbl = new JLabel(titulo);
        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD, 22f));
        p.add(lbl);
        return p;
    }

    /** Crea la barra de menú principal de la aplicación. */
    private JMenuBar crearMenu() {
        JMenuBar mb = new JMenuBar();

        JMenu archivo = new JMenu("Archivo");
        JMenuItem cerrarSesion = new JMenuItem("Cerrar sesión");
        cerrarSesion.addActionListener(e -> cerrarSesion());
        archivo.add(cerrarSesion);

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

