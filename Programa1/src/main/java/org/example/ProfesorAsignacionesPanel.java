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
 * Permite al profesor:
 *  - Seleccionar uno de sus Grupos
 *  - Ver las EvaluacionesAsignadas del grupo
 *  - Asignar una Evaluacion (propia) con fecha/hora de inicio
 *  - Eliminar una asignación (si procede)
 *
 * Persistencia: trabaja en memoria sobre g.getEvaluacionesAsignadas().
 */
public class ProfesorAsignacionesPanel extends JPanel {

    private final UsuarioServiceMem usuarioService;
    private final CursoService cursoService;
    private final EvaluacionService evaluacionService; // <-- NUEVO
    private final Profesor profesor;

    // UI
    private final JComboBox<Grupo> cboGrupos = new JComboBox<>();
    private final JComboBox<Evaluacion> cboEvaluaciones = new JComboBox<>();
    private final JSpinner spFechaHoraInicio; // Date + time
    private final JButton btnAsignar = new JButton("Asignar");
    private final JButton btnEliminar = new JButton("Eliminar asignación");
    private final JButton btnRefrescar = new JButton("Refrescar");

    private final JTable tabla = new JTable();
    private final AsignacionesModel model = new AsignacionesModel();

    public ProfesorAsignacionesPanel(UsuarioServiceMem usuarioService,
                                     CursoService cursoService,
                                     EvaluacionService evaluacionService, // <-- NUEVO
                                     Profesor profesor) {
        this.usuarioService = usuarioService;
        this.cursoService = cursoService;
        this.evaluacionService = evaluacionService; // <-- NUEVO
        this.profesor = profesor;

        setLayout(new BorderLayout(8,8));
        setBorder(BorderFactory.createEmptyBorder(12,12,12,12));

        // Spinner de fecha/hora (usa java.util.Date; convertimos a LocalDateTime)
        SpinnerDateModel dm = new SpinnerDateModel(new Date(), null, null, java.util.Calendar.MINUTE);
        spFechaHoraInicio = new JSpinner(dm);
        JSpinner.DateEditor de = new JSpinner.DateEditor(spFechaHoraInicio, "yyyy-MM-dd HH:mm");
        spFechaHoraInicio.setEditor(de);

        construirUI();
        cargarCombos();
        cargarAsignaciones();
        wire();
    }

    private void construirUI() {
        // Norte: filtros/inputs
        JPanel north = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6,6,6,6);
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

        // Centro: tabla de asignaciones
        tabla.setModel(model);
        tabla.setFillsViewportHeight(true);
        add(new JScrollPane(tabla), BorderLayout.CENTER);
    }

    private void wire() {
        btnRefrescar.addActionListener(e -> {
            cargarCombos();
            cargarAsignaciones();
        });

        cboGrupos.addActionListener(e -> cargarAsignaciones());

        btnAsignar.addActionListener(e -> onAsignar());
        btnEliminar.addActionListener(e -> onEliminar());
    }

    private void addRow(JPanel p, GridBagConstraints c, int row, String label, JComponent comp) {
        c.gridx = 0; c.gridy = row; c.weightx = 0; p.add(new JLabel(label), c);
        c.gridx = 1; c.gridy = row; c.weightx = 1; p.add(comp, c);
    }

    private void cargarCombos() {
        // Grupos del profesor
        cboGrupos.removeAllItems();
        List<Grupo> grupos = (profesor.getGrupos() == null) ? new ArrayList<>() : profesor.getGrupos();
        for (Grupo g : grupos) cboGrupos.addItem(g);

        // Evaluaciones del profesor: AHORA desde el SERVICE (no desde profesor.getEvaluaciones())
        cboEvaluaciones.removeAllItems();
        List<Evaluacion> evals = evaluacionService.listarPorProfesor(profesor.getIdUsuario());
        if (evals != null) {
            for (Evaluacion e : evals) cboEvaluaciones.addItem(e);
        }

        if (cboGrupos.getItemCount() > 0) cboGrupos.setSelectedIndex(0);
        if (cboEvaluaciones.getItemCount() > 0) cboEvaluaciones.setSelectedIndex(0);
    }

    private Grupo grupoSel() {
        Object o = cboGrupos.getSelectedItem();
        return (o instanceof Grupo g) ? g : null;
    }

    private Evaluacion evaluacionSel() {
        Object o = cboEvaluaciones.getSelectedItem();
        return (o instanceof Evaluacion e) ? e : null;
    }

    private void cargarAsignaciones() {
        Grupo g = grupoSel();
        if (g == null) { model.setData(new ArrayList<>()); return; }
        List<EvaluacionAsignada> asigs = (g.getEvaluacionesAsignadas() == null)
                ? new ArrayList<>()
                : g.getEvaluacionesAsignadas();
        model.setData(asigs);
    }

    private void onAsignar() {
        try {
            Grupo g = grupoSel();
            Evaluacion ev = evaluacionSel();
            if (g == null) throw new IllegalStateException("Seleccione un grupo.");
            if (ev == null) throw new IllegalStateException("Seleccione una evaluación.");

            // Obtener LocalDateTime desde el spinner
            java.util.Date fecha = (java.util.Date) spFechaHoraInicio.getValue();
            LocalDateTime inicio = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(fecha.getTime()),
                    ZoneId.systemDefault()
            );

            // Evitar duplicados de la misma evaluación en el mismo grupo y misma fecha
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
            nueva.calcularFechaHoraFinal(); // según duración de la evaluación

            if (g.getEvaluacionesAsignadas() == null) {
                g.setEvaluacionesAsignadas(new ArrayList<>());
            }
            g.getEvaluacionesAsignadas().add(nueva);

            cargarAsignaciones();
            JOptionPane.showMessageDialog(this, "Evaluación asignada al grupo.");

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error al asignar", JOptionPane.ERROR_MESSAGE);
        }
    }

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

    // ===== Modelo de tabla =====
    static class AsignacionesModel extends AbstractTableModel {
        private final String[] cols = {"Evaluación", "Inicio", "Fin", "Duración (min)"};
        private List<EvaluacionAsignada> data = new ArrayList<>();

        public void setData(List<EvaluacionAsignada> list) {
            data = (list == null) ? new ArrayList<>() : new ArrayList<>(list);
            fireTableDataChanged();
        }

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
