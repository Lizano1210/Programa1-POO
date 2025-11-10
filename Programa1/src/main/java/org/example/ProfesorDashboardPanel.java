package org.example;

import javax.swing.*;
import java.awt.*;

/**
 * Panel principal del profesor dentro de la aplicación.
 * <p>
 * Sirve como panel de control o "dashboard" para acceder a todas las
 * secciones relacionadas con la gestión docente:
 * información personal, cursos, evaluaciones, asignaciones,
 * seguimiento e intentos de estudiantes.
 * </p>
 * <p>
 * Cada pestaña del panel corresponde a un submódulo funcional,
 * implementado en paneles independientes.
 * </p>
 */
public class ProfesorDashboardPanel extends JPanel {

    // -- Dependencias principales --

    /** Profesor actualmente autenticado. */
    private final Profesor profesor;

    /** Servicio de usuarios del sistema. */
    private final UsuarioService usuarioService;

    /** Servicio de cursos. */
    private final CursoService cursoService;

    /** Servicio de evaluaciones. */
    private final EvaluacionService evaluacionService;

    /** Servicio de intentos de evaluación. */
    private final IntentoService intentoService;

    /** Servicio de reportes (para exportar resultados o seguimientos). */
    private final ReporteService reporteService;

    // -- Constructor --

    /**
     * Crea el panel principal del profesor, incluyendo todas las pestañas de gestión.
     *
     * @param profesor profesor autenticado
     * @param usuarioService servicio de usuarios
     * @param cursoService servicio de cursos
     * @param evaluacionService servicio de evaluaciones
     * @param intentoService servicio de intentos
     * @param reporteService servicio de reportes
     */
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

        // -- Pestaña: información personal --
        tabs.addTab("Mi información",
                new ProfesorInfoPanel((UsuarioServiceMem) this.usuarioService, this.profesor));

        // -- Pestaña: cursos y grupos --
        tabs.addTab("Mis cursos y grupos",
                new ProfesorCursosGruposPanel((UsuarioServiceMem) this.usuarioService, this.cursoService, this.profesor));

        // -- Pestaña: evaluaciones --
        tabs.addTab("Mis evaluaciones",
                new ProfesorEvaluacionesPanel(this.profesor, this.evaluacionService));

        // -- Pestaña: asignaciones --
        tabs.addTab("Asignaciones",
                new ProfesorAsignacionesPanel(
                        (UsuarioServiceMem) this.usuarioService,
                        this.cursoService,
                        this.evaluacionService,
                        this.profesor
                ));

        // -- Pestaña: seguimiento --
        tabs.addTab("Seguimiento",
                new ProfesorSeguimientoPanel(
                        (UsuarioServiceMem) this.usuarioService,
                        this.cursoService,
                        this.evaluacionService,
                        this.profesor));

        // -- Pestaña: intentos de estudiantes --
        tabs.addTab("Intentos",
                new ProfesorIntentosPanel(
                        (UsuarioServiceMem) this.usuarioService,
                        this.cursoService,
                        this.profesor,
                        // Proveedor de intentos por grupo
                        (Grupo g) -> this.intentoService.listarPorGrupo(g.getIdGrupo()),
                        // Exportador PDF mediante ReporteServicePdf
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

    // -- Auxiliar --

    /**
     * Crea un panel de marcador de posición con texto centrado.
     * <p>
     * Este método puede usarse para pruebas o pestañas temporales.
     * </p>
     *
     * @param texto texto a mostrar
     * @return panel simple con el texto centrado
     */
    private JPanel crearPlaceholder(String texto) {
        JPanel p = new JPanel(new GridBagLayout());
        JLabel lbl = new JLabel(texto);
        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD, 16f));
        p.add(lbl);
        return p;
    }
}
