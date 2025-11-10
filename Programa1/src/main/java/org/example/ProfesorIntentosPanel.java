package org.example;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Panel de interfaz gráfica que permite al profesor visualizar y exportar
 * los intentos de evaluación realizados por sus estudiantes.
 * <p>
 * Permite seleccionar un grupo, revisar los intentos asociados y generar
 * un archivo PDF con el detalle de un intento específico.
 * </p>
 */
public class ProfesorIntentosPanel extends JPanel {

    // -- Servicios --

    /** Servicio de usuarios utilizado para obtener información de estudiantes y profesores. */
    private final UsuarioServiceMem usuarioService;

    /** Servicio de cursos utilizado para acceder a los grupos del profesor. */
    private final CursoService cursoService;

    /** Profesor autenticado que usa el panel. */
    private final Profesor profesor;

    // -- Funciones de soporte --

    /** Función que provee los intentos asociados a un grupo. */
    private final Function<Grupo, List<IntentoEvaluacion>> proveedorIntentos;

    /** Función que exporta un intento de evaluación a PDF. */
    private final BiFunction<IntentoEvaluacion, File, Boolean> exportadorPdf;

    // -- Componentes de interfaz --

    private final JComboBox<Grupo> cmbGrupo = new JComboBox<>();
    private final JTable tabla = new JTable();
    private final IntentosModel model = new IntentosModel();

    private final JButton btnRefrescar = new JButton("Refrescar");
    private final JButton btnVer = new JButton("Ver intento");
    private final JButton btnExportar = new JButton("Exportar PDF");

    // -- Constructor --

    /**
     * Crea un nuevo panel de gestión de intentos de evaluación del profesor.
     *
     * @param usuarioService servicio de usuarios
     * @param cursoService servicio de cursos
     * @param profesor profesor actual
     * @param proveedorIntentos función que devuelve los intentos de un grupo
     * @param exportadorPdf función que genera un PDF a partir de un intento
     */
    public ProfesorIntentosPanel(UsuarioServiceMem usuarioService,
                                 CursoService cursoService,
                                 Profesor profesor,
                                 Function<Grupo, List<IntentoEvaluacion>> proveedorIntentos,
                                 BiFunction<IntentoEvaluacion, File, Boolean> exportadorPdf) {
        this.usuarioService = usuarioService;
        this.cursoService = cursoService;
        this.profesor = profesor;
        this.proveedorIntentos = proveedorIntentos;
        this.exportadorPdf = exportadorPdf;

        setLayout(new BorderLayout(8, 8));

        // -- Panel superior: selección de grupo y acciones --
        JPanel north = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        north.add(new JLabel("Grupo:"));
        cargarGruposProfesor();
        north.add(cmbGrupo);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        actions.add(btnRefrescar);
        actions.add(btnVer);
        actions.add(btnExportar);

        JPanel top = new JPanel(new BorderLayout());
        top.add(north, BorderLayout.WEST);
        top.add(actions, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);

        // -- Tabla de intentos --
        tabla.setModel(model);
        tabla.setFillsViewportHeight(true);
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        // -- Eventos --
        btnRefrescar.addActionListener(e -> cargarIntentos());
        btnVer.addActionListener(e -> onVerIntento());
        btnExportar.addActionListener(e -> onExportarPdf());
        cmbGrupo.addActionListener(e -> cargarIntentos());

        if (cmbGrupo.getItemCount() > 0) cmbGrupo.setSelectedIndex(0);
        cargarIntentos();
    }

    // -- Carga de datos --

    /** Carga los grupos asignados al profesor en el combo de selección. */
    private void cargarGruposProfesor() {
        cmbGrupo.removeAllItems();
        List<Grupo> grupos = profesor.getGrupos();
        if (grupos != null) {
            for (Grupo g : grupos) cmbGrupo.addItem(g);
        }
    }

    /** Carga los intentos del grupo seleccionado en la tabla. */
    private void cargarIntentos() {
        Grupo g = (Grupo) cmbGrupo.getSelectedItem();
        List<IntentoEvaluacion> data = (g == null) ? new ArrayList<>() : proveedorIntentos.apply(g);
        model.setData(data);
    }

    /** Devuelve el intento actualmente seleccionado en la tabla. */
    private IntentoEvaluacion seleccionado() {
        int r = tabla.getSelectedRow();
        return (r < 0) ? null : model.getAt(r);
    }

    // -- Acciones --

    /** Muestra un intento de evaluación seleccionado en una ventana de revisión. */
    private void onVerIntento() {
        IntentoEvaluacion it = seleccionado();
        if (it == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un intento.");
            return;
        }

        RevisionIntentoDialog dlg = new RevisionIntentoDialog(
                SwingUtilities.getWindowAncestor(this), it
        );
        dlg.setVisible(true);
    }

    /** Exporta el intento seleccionado a un archivo PDF. */
    private void onExportarPdf() {
        IntentoEvaluacion it = seleccionado();
        if (it == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un intento.");
            return;
        }

        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Guardar intento como PDF");

        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File destino = fc.getSelectedFile();
            boolean ok = false;

            try {
                ok = exportadorPdf.apply(it, destino);
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            JOptionPane.showMessageDialog(this,
                    ok ? "PDF generado correctamente." : "No se pudo generar el PDF.");
        }
    }

    // -- Modelo interno --

    /**
     * Modelo de tabla que muestra los intentos de evaluación de los estudiantes.
     */
    static class IntentosModel extends AbstractTableModel {

        private final String[] cols = {
                "Estudiante", "Evaluación", "Grupo",
                "Inicio", "Fin", "Puntaje", "Calificación"
        };

        private List<IntentoEvaluacion> data = new ArrayList<>();

        /** Actualiza los datos mostrados en la tabla. */
        public void setData(List<IntentoEvaluacion> list) {
            data = (list == null) ? new ArrayList<>() : list;
            fireTableDataChanged();
        }

        /** Obtiene el intento en la fila indicada. */
        public IntentoEvaluacion getAt(int r) { return data.get(r); }

        @Override public int getRowCount() { return data.size(); }
        @Override public int getColumnCount() { return cols.length; }
        @Override public String getColumnName(int c) { return cols[c]; }

        @Override
        public Object getValueAt(int r, int c) {
            IntentoEvaluacion it = data.get(r);
            return switch (c) {
                case 0 -> (it.getEstudiante() == null) ? "-" : it.getEstudiante().getNombre();
                case 1 -> (it.getEvaluacion() == null) ? "-" : it.getEvaluacion().getNombre();
                case 2 -> (it.getGrupo() == null) ? "-" : it.getGrupo().getIdGrupo();
                case 3 -> it.getFechaHoraInicio();
                case 4 -> it.getFechaHoraFinal();
                case 5 -> it.getPuntajeObtenido();
                case 6 -> String.format("%.2f", it.getCalificacion());
                default -> "";
            };
        }
    }
}

