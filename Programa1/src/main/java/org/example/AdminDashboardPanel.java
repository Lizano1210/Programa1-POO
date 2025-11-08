package org.example;

import javax.swing.*;
import java.awt.*;

/**
 Panel principal del Administrador con navegación lateral y CardLayout.
 */
public class AdminDashboardPanel extends JPanel {

    private final JPanel centerCards = new JPanel(new CardLayout());
    private final UsuarioService usuarioService;

    public AdminDashboardPanel(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;

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

        // cards de los botones
        centerCards.add(new AdminUsuariosPanel(usuarioService), "USUARIOS");
        CursoServiceMem cursoService = new CursoServiceMem(usuarioService);
        centerCards.add(new AdminCursosPanel(cursoService, usuarioService), "CURSOS");
        centerCards.add(crearPlaceholder("Módulo de Reportes (pendiente)"), "REPORTES");
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
