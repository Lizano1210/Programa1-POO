package org.example;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ProfesorEvaluacionesPanel extends JPanel {

    private final Profesor profesor;
    private final EvaluacionService evaluacionService;

    private final TablaModel model = new TablaModel();
    private final JTable tabla = new JTable(model);

    public ProfesorEvaluacionesPanel(Profesor profesor, EvaluacionService evaluacionService) {
        this.profesor = profesor;
        this.evaluacionService = evaluacionService;

        setLayout(new BorderLayout(8,8));

        // Tabla
        tabla.setFillsViewportHeight(true);
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        // Botonera
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnCrear = new JButton("Crear");
        JButton btnEditar = new JButton("Editar");
        JButton btnEliminar = new JButton("Eliminar");
        JButton btnRefrescar = new JButton("Refrescar");

        actions.add(btnCrear);
        actions.add(btnEditar);
        actions.add(btnEliminar);
        actions.add(btnRefrescar);
        add(actions, BorderLayout.SOUTH);

        // Eventos
        btnRefrescar.addActionListener(e -> cargar());
        btnCrear.addActionListener(e -> onCrear());
        btnEditar.addActionListener(e -> onEditar());
        btnEliminar.addActionListener(e -> onEliminar());

        cargar();
    }

    /** Carga para esta tabla (desde el service). Las otras pestañas leen de profesor.getEvaluaciones(). */
    private void cargar() {
        List<Evaluacion> lista = evaluacionService.listarPorProfesor(profesor.getIdUsuario());
        model.setData(lista);
    }

    private Evaluacion seleccionada() {
        int r = tabla.getSelectedRow();
        return r < 0 ? null : model.getAt(r);
    }

    private void onCrear() {
        EditorEvaluacionDialog dlg = new EditorEvaluacionDialog(
                SwingUtilities.getWindowAncestor(this), null
        );
        dlg.setVisible(true);

        if (dlg.isGuardado()) {
            try {
                Evaluacion nueva = dlg.getNuevaEvaluacion();
                if (nueva == null) {
                    JOptionPane.showMessageDialog(this, "No se creó la evaluación.", "Aviso", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // 1) Service
                evaluacionService.crear(profesor.getIdUsuario(), nueva);

                // 2) Sincronizar con la lista del profesor
                agregarAlProfesor(nueva);

                JOptionPane.showMessageDialog(this, "Evaluación creada.");
                cargar(); // refresca la tabla local
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "No se pudo crear", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void onEditar() {
        Evaluacion ev = seleccionada();
        if (ev == null) {
            JOptionPane.showMessageDialog(this, "Seleccione una evaluación.");
            return;
        }

        EditorEvaluacionDialog dlg = new EditorEvaluacionDialog(SwingUtilities.getWindowAncestor(this), ev);
        dlg.setVisible(true);

        if (dlg.isGuardado()) {
            try {
                // 1) Service
                evaluacionService.actualizar(profesor.getIdUsuario(), ev);

                // 2) Sincronizar con la lista del profesor (reemplazo por id)
                reemplazarEnProfesor(ev);

                JOptionPane.showMessageDialog(this, "Evaluación actualizada.");
                cargar();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "No se pudo actualizar", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void onEliminar() {
        Evaluacion ev = seleccionada();
        if (ev == null) {
            JOptionPane.showMessageDialog(this, "Seleccione una evaluación.");
            return;
        }
        int ok = JOptionPane.showConfirmDialog(this, "¿Eliminar la evaluación \"" + ev.getNombre() + "\"?",
                "Confirmar", JOptionPane.YES_NO_OPTION);
        if (ok == JOptionPane.YES_OPTION) {
            try {
                // 1) Service
                evaluacionService.eliminar(profesor.getIdUsuario(), ev.getId());

                // 2) Sincronizar lista del profesor
                eliminarDeProfesor(ev);

                JOptionPane.showMessageDialog(this, "Evaluación eliminada.");
                cargar();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "No se pudo eliminar", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ---------- Sincronización con profesor.getEvaluaciones() ----------
    private void asegurarListaMutableProfesor() {
        List<Evaluacion> list = profesor.getEvaluaciones();
        if (list == null) {
            profesor.setEvaluaciones(new ArrayList<>());
        } else if (!(list instanceof ArrayList)) {
            profesor.setEvaluaciones(new ArrayList<>(list));
        }
    }

    private void agregarAlProfesor(Evaluacion ev) {
        if (ev == null) return;
        asegurarListaMutableProfesor();
        List<Evaluacion> list = profesor.getEvaluaciones();
        for (Evaluacion e : list) {
            if (e != null && e.getId() == ev.getId()) return; // evitar duplicado por id
        }
        list.add(ev);
    }

    private void reemplazarEnProfesor(Evaluacion ev) {
        if (ev == null) return;
        asegurarListaMutableProfesor();
        List<Evaluacion> list = profesor.getEvaluaciones();
        for (int i = 0; i < list.size(); i++) {
            Evaluacion cur = list.get(i);
            if (cur != null && cur.getId() == ev.getId()) {
                list.set(i, ev);
                return;
            }
        }
        // si no estaba (por alguna razón), agrégala
        list.add(ev);
    }

    private void eliminarDeProfesor(Evaluacion ev) {
        if (ev == null) return;
        List<Evaluacion> list = profesor.getEvaluaciones();
        if (list == null) return;
        list.removeIf(x -> x != null && x.getId() == ev.getId());
    }

    // ---------- Tabla ----------
    static class TablaModel extends AbstractTableModel {
        private final String[] cols = {"ID", "Nombre", "Duración (min)", "# Preguntas", "Puntaje total", "Aleatoriedad"};
        private List<Evaluacion> data = new ArrayList<>();

        public void setData(List<Evaluacion> list) {
            data = (list == null) ? new ArrayList<>() : new ArrayList<>(list);
            fireTableDataChanged();
        }

        public Evaluacion getAt(int row) { return data.get(row); }

        @Override public int getRowCount() { return data.size(); }
        @Override public int getColumnCount() { return cols.length; }
        @Override public String getColumnName(int c) { return cols[c]; }

        @Override
        public Object getValueAt(int r, int c) {
            Evaluacion e = data.get(r);
            return switch (c) {
                case 0 -> e.getId();
                case 1 -> e.getNombre();
                case 2 -> e.getDuracionMinutos();
                case 3 -> (e.getPreguntas() == null ? 0 : e.getPreguntas().size());
                case 4 -> e.getPuntajeTotal();
                case 5 -> (e.isPreguntasAleatorias() ? "Preguntas" : "—")
                        + (e.isOpcionesAleatorias() ? " / Opciones" : "");
                default -> "";
            };
        }
    }
}

