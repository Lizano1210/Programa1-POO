package org.example;

import javax.swing.*;
import java.awt.*;

public class ProfesorDashboardPanel extends JPanel {

    private final Profesor profesor;
    private final UsuarioService usuarioService;
    private final CursoService cursoService;
    private final EvaluacionService evaluacionService;
    private final IntentoService intentoService;
    private final ReporteService reporteService;

    public ProfesorDashboardPanel(Profesor profesor,
                                  UsuarioService usuarioService,
                                  CursoService cursoService,
                                  EvaluacionService evaluacionService,
                                  IntentoService intentoService,
                                  ReporteService reporteService) {
        this.profesor = profesor;
        this.usuarioService = usuarioService;
        this.cursoService = cursoService;
        this.evaluacionService = evaluacionService;
        this.intentoService = intentoService;
        this.reporteService = reporteService;

        setLayout(new BorderLayout());
        JTabbedPane tabs = new JTabbedPane();

        tabs.addTab("Mi información",
                new ProfesorInfoPanel((UsuarioServiceMem) this.usuarioService, this.profesor));

        tabs.addTab("Mis cursos y grupos",
                new ProfesorCursosGruposPanel((UsuarioServiceMem) this.usuarioService, this.cursoService, this.profesor));

        tabs.addTab("Mis evaluaciones",
                new ProfesorEvaluacionesPanel(this.profesor, this.evaluacionService));

        tabs.addTab("Asignaciones",
                new ProfesorAsignacionesPanel(
                        (UsuarioServiceMem) this.usuarioService,
                        this.cursoService,
                        this.evaluacionService,
                        this.profesor
                ));

        tabs.addTab("Seguimiento",
                new ProfesorSeguimientoPanel((UsuarioServiceMem) this.usuarioService, this.cursoService, this.evaluacionService, this.profesor));

        tabs.addTab("Intentos",
                new ProfesorIntentosPanel(
                        (UsuarioServiceMem) this.usuarioService,
                        this.cursoService,
                        this.profesor,
                        // proveedorIntentos por grupo:
                        (Grupo g) -> this.intentoService.listarPorGrupo(g.getIdGrupo()),
                        // exportador PDF usando ReporteServicePdf:
                        (IntentoEvaluacion ie, java.io.File destino) -> {
                            try {
                                return ((ReporteServicePdf) this.reporteService).exportarIntento(ie, destino);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                                return false;
                            }
                        }
                ));

        add(tabs, BorderLayout.CENTER);
    }


    private JPanel crearPlaceholder(String texto) {
        JPanel p = new JPanel(new GridBagLayout());
        JLabel lbl = new JLabel(texto);
        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD, 16f));
        p.add(lbl);
        return p;
    }
}
