package org.example;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.List;

/**
 * Panel de interfaz gráfica que permite al profesor gestionar las asignaciones
 * de evaluaciones a sus grupos.
 * <p>
 * Funcionalidades principales:
 * <ul>
 *     <li>Seleccionar uno de sus grupos.</li>
 *     <li>Visualizar las evaluaciones asignadas al grupo.</li>
 *     <li>Asignar una nueva evaluación con fecha y hora de inicio.</li>
 *     <li>Eliminar una asignación existente.</li>
 * </ul>
 * <p>
 * Este panel trabaja completamente en memoria sobre la lista
 * {@code g.getEvaluacionesAsignadas()} de cada grupo.
 * </p>
 */
public class ProfesorAsignacionesPanel extends JPanel {

    // -- Servicios --

    /** Servicio de usuarios utilizado para acceder a la información de profesores y estudiantes. */
    private final UsuarioServiceMem usuarioService;

    /** Servicio de cursos utilizado para gestionar grupos. */
    private final CursoService cursoService;

    /** Servicio de evaluaciones utilizado para listar evaluaciones del profesor. */
    private final EvaluacionService evaluacionService;

    /** Profesor actual que usa el panel. */
    private final Profesor profesor;

    // -- Componentes de la interfaz --

    private final JComboBox<Grupo> cboGrupos = new JComboBox<>();
    private final JComboBox<Evaluacion> cboEvaluaciones = new JComboBox<>();
    private final JSpinner spFechaHoraInicio;
    private final JButton btnAsignar = new JButton("Asignar");
    private final JButton btnEliminar = new JButton("Eliminar asignación");
    private final JButton btnRefrescar = new JButton("Refrescar");

    private final JTable tabla = new JTable();
    private final AsignacionesModel model = new AsignacionesModel();

    // -- Constructor --

    /**
     * Crea un nuevo panel de asignaciones de evaluaciones para un profesor.
     *
     * @param usuarioService servicio de usuarios
     * @param cursoService servicio de cursos
     * @param evaluacionService servicio de evaluaciones
     * @param profesor profesor que utilizará el panel
     */
    public ProfesorAsignacionesPanel(UsuarioServiceMem usuarioService,
                                     CursoService cursoService,
                                     EvaluacionService evaluacionService,
                                     Profesor profesor) {
        this.usuarioService = usuarioService;
        this.cursoService = cursoService;
        this.evaluacionService = evaluacionService;
        this.profesor = profesor;

        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        // Configuración del spinner de fecha y hora
        SpinnerDateModel dm = new SpinnerDateModel(new Date(), null, null, Calendar.MINUTE);
        spFechaHoraInicio = new JSpinner(dm);
        JSpinner.DateEditor de = new JSpinner.DateEditor(spFechaHoraInicio, "yyyy-MM-dd HH:mm");
        spFechaHoraInicio.setEditor(de);

        construirUI();
        cargarCombos();
        cargarAsignaciones();
        wire();
    }

    // -- Construcción de la interfaz --

    /** Construye la estructura de la interfaz del panel. */
    private void construirUI() {
        JPanel north = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0;

        int r = 0;
        addRow(north, c, r++, "Grupo:", cboGrupos);
        addRow(north, c, r++, "Evaluación:", cboEvaluaciones);
        addRow(north, c, r++, "Inicio (yyyy-MM-dd HH:mm):", spFechaHoraInicio);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.add(btnRefrescar);
        actions.add(btnAsignar);
        actions.add(btnEliminar);

        add(north, BorderLayout.NORTH);
        add(actions, BorderLayout.SOUTH);

        tabla.setModel(model);
        tabla.setFillsViewportHeight(true);
        add(new JScrollPane(tabla), BorderLayout.CENTER);
    }

    /** Agrega una fila con etiqueta y componente al panel norte. */
    private void addRow(JPanel p, GridBagConstraints c, int row, String label, JComponent comp) {
        c.gridx = 0; c.gridy = row; c.weightx = 0; p.add(new JLabel(label), c);
        c.gridx = 1; c.gridy = row; c.weightx = 1; p.add(comp, c);
    }

    // -- Configuración de eventos --

    /** Configura los eventos de los botones y combos del panel. */
    private void wire() {
        btnRefrescar.addActionListener(e -> {
            cargarCombos();
            cargarAsignaciones();
        });

        cboGrupos.addActionListener(e -> cargarAsignaciones());

        btnAsignar.addActionListener(e -> onAsignar());
        btnEliminar.addActionListener(e -> onEliminar());
    }

    // -- Carga de datos --

    /** Carga los grupos y evaluaciones disponibles en los combos. */
    private void cargarCombos() {
        cboGrupos.removeAllItems();
        List<Grupo> grupos = profesor.getGrupos() == null ? new ArrayList<>() : profesor.getGrupos();
        for (Grupo g : grupos) cboGrupos.addItem(g);

        cboEvaluaciones.removeAllItems();
        List<Evaluacion> evals = evaluacionService.listarPorProfesor(profesor.getIdUsuario());
        if (evals != null) for (Evaluacion e : evals) cboEvaluaciones.addItem(e);

        if (cboGrupos.getItemCount() > 0) cboGrupos.setSelectedIndex(0);
        if (cboEvaluaciones.getItemCount() > 0) cboEvaluaciones.setSelectedIndex(0);
    }

    /** Devuelve el grupo actualmente seleccionado. */
    private Grupo grupoSel() {
        Object o = cboGrupos.getSelectedItem();
        return (o instanceof Grupo g) ? g : null;
    }

    /** Devuelve la evaluación actualmente seleccionada. */
    private Evaluacion evaluacionSel() {
        Object o = cboEvaluaciones.getSelectedItem();
        return (o instanceof Evaluacion e) ? e : null;
    }

    /** Carga las evaluaciones asignadas al grupo seleccionado. */
    private void cargarAsignaciones() {
        Grupo g = grupoSel();
        if (g == null) {
            model.setData(new ArrayList<>());
            return;
        }
        List<EvaluacionAsignada> asigs = g.getEvaluacionesAsignadas() == null
                ? new ArrayList<>()
                : g.getEvaluacionesAsignadas();
        model.setData(asigs);
    }

    // -- Acciones principales --

    /** Asigna una evaluación al grupo seleccionado en la fecha y hora indicadas. */
    private void onAsignar() {
        try {
            Grupo g = grupoSel();
            Evaluacion ev = evaluacionSel();
            if (g == null) throw new IllegalStateException("Seleccione un grupo.");
            if (ev == null) throw new IllegalStateException("Seleccione una evaluación.");

            java.util.Date fecha = (java.util.Date) spFechaHoraInicio.getValue();
            LocalDateTime inicio = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(fecha.getTime()),
                    ZoneId.systemDefault()
            );

            if (g.getEvaluacionesAsignadas() != null) {
                for (EvaluacionAsignada ea : g.getEvaluacionesAsignadas()) {
                    if (ea != null && ea.getEvaluacion() != null
                            && ea.getEvaluacion().equals(ev)
                            && ea.getFechaHoraInicio() != null
                            && ea.getFechaHoraInicio().equals(inicio)) {
                        throw new IllegalStateException("Ya existe una asignación de esa evaluación en esa fecha/hora.");
                    }
                }
            }

            EvaluacionAsignada nueva = new EvaluacionAsignada(ev, g, inicio);
            nueva.calcularFechaHoraFinal();

            if (g.getEvaluacionesAsignadas() == null)
                g.setEvaluacionesAsignadas(new ArrayList<>());

            g.getEvaluacionesAsignadas().add(nueva);
            cargarAsignaciones();

            JOptionPane.showMessageDialog(this, "Evaluación asignada al grupo.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error al asignar", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Elimina la asignación seleccionada del grupo. */
    private void onEliminar() {
        int r = tabla.getSelectedRow();
        Grupo g = grupoSel();
        if (g == null || r < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione un grupo y una asignación.");
            return;
        }

        EvaluacionAsignada ea = model.getAt(r);
        int ok = JOptionPane.showConfirmDialog(this,
                "¿Eliminar la asignación seleccionada?", "Confirmar",
                JOptionPane.YES_NO_OPTION);

        if (ok != JOptionPane.YES_OPTION) return;

        try {
            if (g.getEvaluacionesAsignadas() != null) {
                g.getEvaluacionesAsignadas().remove(ea);
            }
            cargarAsignaciones();
            JOptionPane.showMessageDialog(this, "Asignación eliminada.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error al eliminar", JOptionPane.ERROR_MESSAGE);
        }
    }

    // -- Modelo interno de tabla --

    /**
     * Modelo de tabla para mostrar las evaluaciones asignadas de un grupo.
     */
    static class AsignacionesModel extends AbstractTableModel {

        private final String[] cols = {"Evaluación", "Inicio", "Fin", "Duración (min)"};
        private List<EvaluacionAsignada> data = new ArrayList<>();

        /** Actualiza los datos mostrados en la tabla. */
        public void setData(List<EvaluacionAsignada> list) {
            data = list == null ? new ArrayList<>() : new ArrayList<>(list);
            fireTableDataChanged();
        }

        /** Devuelve la asignación en la fila indicada. */
        public EvaluacionAsignada getAt(int r) { return data.get(r); }

        @Override public int getRowCount() { return data.size(); }
        @Override public int getColumnCount() { return cols.length; }
        @Override public String getColumnName(int c) { return cols[c]; }

        @Override
        public Object getValueAt(int r, int c) {
            EvaluacionAsignada a = data.get(r);
            return switch (c) {
                case 0 -> (a.getEvaluacion() == null ? "-" : a.getEvaluacion().getNombre());
                case 1 -> a.getFechaHoraInicio();
                case 2 -> a.getFechaHoraFinal();
                case 3 -> (a.getEvaluacion() == null ? 0 : a.getEvaluacion().getDuracionMinutos());
                default -> "";
            };
        }
    }
}

