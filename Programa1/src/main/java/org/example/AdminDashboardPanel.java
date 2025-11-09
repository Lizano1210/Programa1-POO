package org.example;

import javax.swing.*;
import java.awt.*;

/**
 Panel principal del Administrador con navegación lateral y CardLayout.
 */
public class AdminDashboardPanel extends JPanel {

    private final JPanel centerCards = new JPanel(new CardLayout());
    private final UsuarioService usuarioService;
    private final CursoService cursoService;        // ← nuevo
    private final ReporteService reporteService;    // ← nuevo

    public AdminDashboardPanel(UsuarioService usuarioService, CursoService cursoService,
                               ReporteService reporteService) {
        this.usuarioService = usuarioService;
        this.cursoService = cursoService;
        this.reporteService = reporteService;

        setLayout(new BorderLayout());

        // barra lateral
        JPanel nav = new JPanel(new GridLayout(0,1,6,6));
        nav.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
        JButton btnUsuarios = new JButton("Usuarios");
        JButton btnCursosGrupos = new JButton("Cursos & Grupos");
        JButton btnReportes = new JButton("Reportes");
        nav.add(btnUsuarios);
        nav.add(btnCursosGrupos);
        nav.add(btnReportes);
        add(nav, BorderLayout.WEST);

        // cards de los botones  (ya NO creamos CursoServiceMem aquí)
        centerCards.add(new AdminUsuariosPanel(usuarioService), "USUARIOS");
        centerCards.add(new AdminCursosPanel(cursoService, usuarioService), "CURSOS");
        centerCards.add(new AdminReportesPanel(cursoService, reporteService), "REPORTES"); // ← reemplaza placeholder
        add(centerCards, BorderLayout.CENTER);

        // botones
        btnUsuarios.addActionListener(e -> show("USUARIOS"));
        btnCursosGrupos.addActionListener(e -> show("CURSOS"));
        btnReportes.addActionListener(e -> show("REPORTES"));

        show("USUARIOS");
    }

    private void show(String card) {
        ((CardLayout) centerCards.getLayout()).show(centerCards, card);
    }

    private JComponent crearPlaceholder(String texto) {
        JPanel p = new JPanel(new GridBagLayout());
        JLabel lbl = new JLabel(texto);
        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD, 18f));
        p.add(lbl);
        return p;
    }
}

