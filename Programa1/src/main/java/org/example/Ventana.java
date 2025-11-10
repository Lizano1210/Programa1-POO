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

    private UsuarioServiceMem usuarioService;
    private CursoService cursoService;
    private MatriculaService matriculaService;
    private ReporteService reporteService;
    private EvaluacionService evaluacionService;
    private IntentoService intentoService;

    // referencia al componente/card de Estudiante (para reemplazar placeholder)
    private Component cardEstudiantePlaceholder;

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

        // ESTUDIANTE: placeholder (se reemplaza al iniciar sesión)
        cardEstudiantePlaceholder = crearPlaceholder("Dashboard Estudiante");
        mainContainer.add(cardEstudiantePlaceholder, "ESTUDIANTE");

        // PROFESOR: placeholder (se reemplaza al iniciar sesión)
        mainContainer.add(crearPlaceholder("Dashboard Profesor"), "PROFESOR");

        // ==== Servicios (una sola instancia de cada uno) ====
        usuarioService = new UsuarioServiceMem(auth);
        usuarioService.seedDemo(); // admin, estudiantes, profes (con credenciales)

        cursoService = new CursoServiceMem(usuarioService);
        ((CursoServiceMem) cursoService).seedCursosDemo();                 // cursos base
        ((CursoServiceMem) cursoService).seedGruposDemo(usuarioService);   // grupo vinculado a curso+profesor

        evaluacionService = new EvaluacionServiceMem(usuarioService, cursoService);
        // Semilla evaluación de 5 preguntas para P200 (Mario Rojas)
        ((EvaluacionServiceMem) evaluacionService).seedEvaluacionesDemo5("P200", usuarioService);

        matriculaService = new MatriculaServiceMem(usuarioService, cursoService);

        reporteService = new ReporteServicePdf(cursoService, usuarioService);

        intentoService = new IntentoServiceMem(); // ⬅️ NUEVO

        // ADMIN
        AdminDashboardPanel admin = new AdminDashboardPanel(usuarioService, cursoService, reporteService);
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
            case ESTUDIANTE -> {
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
                        () -> intentoService.listarPorEstudiante(estActual.getIdUsuario())
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


            case PROFESOR -> {
                Profesor profActual = usuarioService.listarProfesores().stream()
                        .filter(p -> identificacion.equals(p.getIdUsuario()))
                        .findFirst()
                        .orElse(null);

                if (profActual == null) {
                    if (login != null) login.setMensaje("No se encontró el perfil de Profesor.");
                    return;
                }

                ProfesorSeguimientoPanel pnlSeg = new ProfesorSeguimientoPanel(
                        usuarioService,
                        cursoService,
                        evaluacionService,
                        profActual
                );

                ProfesorIntentosPanel pnlIntentos = new ProfesorIntentosPanel(
                        usuarioService,
                        cursoService,
                        profActual,
                        // proveedorIntentos: trae intentos del grupo via IntentoService
                        (Grupo g) -> intentoService.listarPorGrupo(g.getIdGrupo()),
                        // exportadorPdf: delega en tu ReporteServicePdf
                        (IntentoEvaluacion ie, java.io.File destino) -> {
                            try {
                                return ((ReporteServicePdf) reporteService).exportarIntento(ie, destino);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                                return false;
                            }
                        }
                );

                ProfesorDashboardPanel panelProf = new ProfesorDashboardPanel(
                        profActual,
                        usuarioService,
                        cursoService,
                        evaluacionService,
                        intentoService,
                        reporteService
                );

                // Reemplaza el placeholder
                Component ya = null;
                for (Component c : mainContainer.getComponents()) {
                    if (c instanceof ProfesorDashboardPanel) { ya = c; break; }
                }
                if (ya != null) mainContainer.remove(ya);

                mainContainer.add(panelProf, "PROFESOR");
                mainContainer.revalidate();
                mainContainer.repaint();
                mostrar("PROFESOR");
            }


            case ADMIN -> mostrar("ADMIN");
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

    /** Vuelve al login y restaura el placeholder del estudiante. */
    private void cerrarSesion() {
        try {
            // 1) Restaurar placeholder del Estudiante
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

            // 2) Limpiar el LoginPanel (si lo encontramos)
            LoginPanel login = (LoginPanel) obtenerCard("LOGIN");
            if (login != null) {
                login.setMensaje(" ");
            }

            // 3) Mostrar LOGIN
            mostrar("LOGIN");

            mainContainer.revalidate();
            mainContainer.repaint();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "No se pudo cerrar la sesión.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Muestra una tarjeta por nombre. */
    private void mostrar(String name) {
        CardLayout cl = (CardLayout) mainContainer.getLayout();
        cl.show(mainContainer, name);
    }

    /** Obtiene el componente de una card por nombre (para mensajes). */
    private Component obtenerCard(String name) {
        // Implementación simple: devolvemos el LoginPanel si lo encontramos
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

        // NUEVO: Cerrar sesión
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

