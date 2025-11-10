package org.example;

import javax.swing.*;
import java.awt.*;

/**
 * Panel principal del administrador.
 * Contiene la navegación lateral y las distintas secciones
 * (usuarios, cursos/grupos y reportes) mostradas mediante un {@link CardLayout}.
 */
public class AdminDashboardPanel extends JPanel {

    /** Panel central que contiene las distintas vistas (cards). */
    private final JPanel centerCards = new JPanel(new CardLayout());

    /** Servicio de usuarios, utilizado para la gestión de cuentas. */
    private final UsuarioService usuarioService;

    /** Servicio de cursos, utilizado en la gestión de cursos y grupos. */
    private final CursoService cursoService;

    /** Servicio de reportes, encargado de generar los informes del sistema. */
    private final ReporteService reporteService;

    /**
     * Crea el panel principal del administrador.
     *
     * @param usuarioService servicio de usuarios
     * @param cursoService servicio de cursos
     * @param reporteService servicio de reportes
     */
    public AdminDashboardPanel(UsuarioService usuarioService, CursoService cursoService,
                               ReporteService reporteService) {
        this.usuarioService = usuarioService;
        this.cursoService = cursoService;
        this.reporteService = reporteService;

        setLayout(new BorderLayout());

        // --- Barra lateral de navegación ---
        JPanel nav = new JPanel(new GridLayout(0,1,6,6));
        nav.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));

        JButton btnUsuarios = new JButton("Usuarios");
        JButton btnCursosGrupos = new JButton("Cursos & Grupos");
        JButton btnReportes = new JButton("Reportes");

        nav.add(btnUsuarios);
        nav.add(btnCursosGrupos);
        nav.add(btnReportes);
        add(nav, BorderLayout.WEST);

        // --- Paneles centrales con CardLayout ---
        centerCards.add(new AdminUsuariosPanel(usuarioService), "USUARIOS");
        centerCards.add(new AdminCursosPanel(cursoService, usuarioService), "CURSOS");
        centerCards.add(new AdminReportesPanel(cursoService, reporteService), "REPORTES");

        add(centerCards, BorderLayout.CENTER);

        // --- Acciones de los botones ---
        btnUsuarios.addActionListener(e -> show("USUARIOS"));
        btnCursosGrupos.addActionListener(e -> show("CURSOS"));
        btnReportes.addActionListener(e -> show("REPORTES"));

        // Muestra por defecto la vista de usuarios
        show("USUARIOS");
    }

    /**
     * Muestra una vista (card) específica en el panel central.
     *
     * @param card nombre de la vista a mostrar
     */
    private void show(String card) {
        ((CardLayout) centerCards.getLayout()).show(centerCards, card);
    }

    /**
     * Crea un panel temporal de marcador de posición.
     * Este método puede ser útil para pruebas o pantallas vacías.
     *
     * @param texto texto que se mostrará en el marcador
     * @return componente generado con el texto centrado
     */
    private JComponent crearPlaceholder(String texto) {
        JPanel p = new JPanel(new GridBagLayout());
        JLabel lbl = new JLabel(texto);
        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD, 18f));
        p.add(lbl);
        return p;
    }
}


