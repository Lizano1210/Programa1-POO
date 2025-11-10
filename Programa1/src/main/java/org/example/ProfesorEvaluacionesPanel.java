package org.example;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel gráfico que permite al profesor gestionar sus evaluaciones.
 * <p>
 * Incluye funciones para crear, editar, eliminar y visualizar las evaluaciones
 * creadas por el profesor, además de mantener sincronizada la lista de
 * evaluaciones en memoria del propio objeto {@link Profesor}.
 * </p>
 */
public class ProfesorEvaluacionesPanel extends JPanel {

    // -- Atributos principales --

    /** Profesor actualmente autenticado. */
    private final Profesor profesor;

    /** Servicio de evaluaciones utilizado para operaciones CRUD. */
    private final EvaluacionService evaluacionService;

    // -- Componentes de interfaz --

    /** Modelo de datos para la tabla de evaluaciones. */
    private final TablaModel model = new TablaModel();

    /** Tabla principal donde se muestran las evaluaciones. */
    private final JTable tabla = new JTable(model);

    // -- Constructor --

    /**
     * Crea un nuevo panel para gestionar las evaluaciones de un profesor.
     *
     * @param profesor profesor que utilizará el panel
     * @param evaluacionService servicio de evaluaciones
     */
    public ProfesorEvaluacionesPanel(Profesor profesor, EvaluacionService evaluacionService) {
        this.profesor = profesor;
        this.evaluacionService = evaluacionService;

        setLayout(new BorderLayout(8, 8));

        // -- Configuración de tabla --
        tabla.setFillsViewportHeight(true);
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        // -- Botonera inferior --
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

        // -- Eventos --
        btnRefrescar.addActionListener(e -> cargar());
        btnCrear.addActionListener(e -> onCrear());
        btnEditar.addActionListener(e -> onEditar());
        btnEliminar.addActionListener(e -> onEliminar());

        cargar();
    }

    // -- Carga de datos --

    /**
     * Carga la lista de evaluaciones del profesor desde el servicio.
     * <p>
     * La tabla se actualiza con los datos más recientes del servicio.
     * </p>
     */
    private void cargar() {
        List<Evaluacion> lista = evaluacionService.listarPorProfesor(profesor.getIdUsuario());
        model.setData(lista);
    }

    /** Obtiene la evaluación actualmente seleccionada en la tabla. */
    private Evaluacion seleccionada() {
        int r = tabla.getSelectedRow();
        return r < 0 ? null : model.getAt(r);
    }

    // -- Creación, edición y eliminación --

    /** Abre el diálogo para crear una nueva evaluación. */
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

                // 1) Guardar en el servicio
                evaluacionService.crear(profesor.getIdUsuario(), nueva);

                // 2) Sincronizar con la lista del profesor
                agregarAlProfesor(nueva);

                JOptionPane.showMessageDialog(this, "Evaluación creada exitosamente.");
                cargar();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error al crear evaluación", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /** Abre el diálogo para editar una evaluación seleccionada. */
    private void onEditar() {
        Evaluacion ev = seleccionada();
        if (ev == null) {
            JOptionPane.showMessageDialog(this, "Seleccione una evaluación para editar.");
            return;
        }

        EditorEvaluacionDialog dlg = new EditorEvaluacionDialog(SwingUtilities.getWindowAncestor(this), ev);
        dlg.setVisible(true);

        if (dlg.isGuardado()) {
            try {
                evaluacionService.actualizar(profesor.getIdUsuario(), ev);
                reemplazarEnProfesor(ev);
                JOptionPane.showMessageDialog(this, "Evaluación actualizada.");
                cargar();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error al actualizar", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /** Elimina una evaluación seleccionada del sistema y del profesor. */
    private void onEliminar() {
        Evaluacion ev = seleccionada();
        if (ev == null) {
            JOptionPane.showMessageDialog(this, "Seleccione una evaluación para eliminar.");
            return;
        }

        int ok = JOptionPane.showConfirmDialog(this, "¿Eliminar la evaluación \"" + ev.getNombre() + "\"?",
                "Confirmar eliminación", JOptionPane.YES_NO_OPTION);

        if (ok == JOptionPane.YES_OPTION) {
            try {
                evaluacionService.eliminar(profesor.getIdUsuario(), ev.getId());
                eliminarDeProfesor(ev);
                JOptionPane.showMessageDialog(this, "Evaluación eliminada.");
                cargar();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error al eliminar", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // -- Sincronización con la lista interna del profesor --

    /** Asegura que la lista de evaluaciones del profesor sea mutable. */
    private void asegurarListaMutableProfesor() {
        List<Evaluacion> list = profesor.getEvaluaciones();
        if (list == null) {
            profesor.setEvaluaciones(new ArrayList<>());
        } else if (!(list instanceof ArrayList)) {
            profesor.setEvaluaciones(new ArrayList<>(list));
        }
    }

    /** Agrega una nueva evaluación al listado del profesor. */
    private void agregarAlProfesor(Evaluacion ev) {
        if (ev == null) return;
        asegurarListaMutableProfesor();
        List<Evaluacion> list = profesor.getEvaluaciones();
        for (Evaluacion e : list) {
            if (e != null && e.getId() == ev.getId()) return; // evita duplicados
        }
        list.add(ev);
    }

    /** Reemplaza una evaluación existente en el listado del profesor. */
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
        list.add(ev); // si no estaba, se agrega
    }

    /** Elimina una evaluación del listado del profesor. */
    private void eliminarDeProfesor(Evaluacion ev) {
        if (ev == null) return;
        List<Evaluacion> list = profesor.getEvaluaciones();
        if (list == null) return;
        list.removeIf(x -> x != null && x.getId() == ev.getId());
    }

    // -- Modelo de tabla --

    /**
     * Modelo de tabla para mostrar las evaluaciones del profesor.
     */
    static class TablaModel extends AbstractTableModel {
        private final String[] cols = {
                "ID", "Nombre", "Duración (min)", "# Preguntas",
                "Puntaje total", "Aleatoriedad"
        };

        private List<Evaluacion> data = new ArrayList<>();

        /** Actualiza los datos mostrados en la tabla. */
        public void setData(List<Evaluacion> list) {
            data = (list == null) ? new ArrayList<>() : new ArrayList<>(list);
            fireTableDataChanged();
        }

        /** Obtiene la evaluación en la fila indicada. */
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


