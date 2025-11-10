package org.example;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class EditorPreguntasPanel extends JPanel {

    private final Evaluacion evaluacion;
    private final JTable tabla = new JTable();
    private final ModeloTabla model = new ModeloTabla();

    public EditorPreguntasPanel(Evaluacion evaluacion) {
        this.evaluacion = evaluacion;
        setLayout(new BorderLayout(8,8));

        tabla.setModel(model);
        tabla.setFillsViewportHeight(true);
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnAgregar = new JButton("Agregar");
        JButton btnEditar = new JButton("Editar");
        JButton btnEliminar = new JButton("Eliminar");
        JButton btnSubir = new JButton("Subir");
        JButton btnBajar = new JButton("Bajar");

        actions.add(btnAgregar);
        actions.add(btnEditar);
        actions.add(btnEliminar);
        actions.add(btnSubir);
        actions.add(btnBajar);
        add(actions, BorderLayout.SOUTH);

        btnAgregar.addActionListener(e -> onAgregar());
        btnEditar.addActionListener(e -> onEditar());
        btnEliminar.addActionListener(e -> onEliminar());
        btnSubir.addActionListener(e -> onMover(-1));
        btnBajar.addActionListener(e -> onMover(+1));

        recargar();
    }

    private void recargar() {
        List<IPregunta> ps = evaluacion.getPreguntas();
        model.setData(ps == null ? new ArrayList<>() : new ArrayList<>(ps));
    }

    private int filaSel() { return tabla.getSelectedRow(); }

    private IPregunta seleccionada() {
        int r = filaSel();
        return (r < 0) ? null : model.getAt(r);
    }

    /** Calcula el siguiente id entero para Pregunta basado en los existentes (para cualquier tipo con id int). */
    private int siguienteIdPregunta() {
        int max = 0;
        if (evaluacion.getPreguntas() != null) {
            for (IPregunta p : evaluacion.getPreguntas()) {
                if (p instanceof Pregunta q) {
                    max = Math.max(max, q.getId());
                } else if (p instanceof Pareo pr) {
                    max = Math.max(max, pr.getId());
                } else if (p instanceof SopaDeLetras sl) {
                    max = Math.max(max, sl.getId());
                }
            }
        }
        return max + 1;
    }

    private void onAgregar() {
        // Elegir tipo
        TipoPregunta tipo = (TipoPregunta) JOptionPane.showInputDialog(
                this, "Tipo de pregunta", "Agregar pregunta",
                JOptionPane.QUESTION_MESSAGE, null,
                new Object[]{
                        TipoPregunta.SELECCION_UNICA,
                        TipoPregunta.SELECCION_MULTIPLE,
                        TipoPregunta.VERDADERO_FALSO,
                        TipoPregunta.PAREO,
                        TipoPregunta.SOPA_LETRAS
                },
                TipoPregunta.SELECCION_UNICA
        );
        if (tipo == null) return;

        int nuevoId = siguienteIdPregunta();

        IPregunta nueva = null;

        switch (tipo) {
            case SELECCION_UNICA, SELECCION_MULTIPLE, VERDADERO_FALSO -> {
                EditorPreguntaSeleccionesDialog dlg =
                        new EditorPreguntaSeleccionesDialog(
                                SwingUtilities.getWindowAncestor(this),
                                null,
                                tipo,
                                nuevoId
                        );
                dlg.setVisible(true);
                if (dlg.isGuardado()) nueva = dlg.getPreguntaFinal();
            }
            case PAREO -> {
                EditorPreguntaPareoDialog dlg =
                        new EditorPreguntaPareoDialog(
                                SwingUtilities.getWindowAncestor(this),
                                null,
                                nuevoId
                        );
                dlg.setVisible(true);
                if (dlg.isGuardado()) nueva = dlg.getPreguntaFinal();
            }
            case SOPA_LETRAS -> {
                EditorPreguntaSopaDialog dlg =
                        new EditorPreguntaSopaDialog(
                                SwingUtilities.getWindowAncestor(this),
                                null,
                                nuevoId
                        );
                dlg.setVisible(true);
                if (dlg.isGuardado()) nueva = dlg.getPreguntaFinal();
            }
            default -> {}
        }

        if (nueva != null) {
            evaluacion.agregarPregunta(nueva);
            evaluacion.calcularPuntajeTotal();
            recargar();
        }
    }

    private void onEditar() {
        IPregunta p = seleccionada();
        if (p == null) { JOptionPane.showMessageDialog(this, "Seleccione una pregunta."); return; }

        int row = filaSel();
        IPregunta reemplazo = null;

        switch (p.getTipo()) {
            case SELECCION_UNICA, SELECCION_MULTIPLE, VERDADERO_FALSO -> {
                if (!(p instanceof Pregunta original)) {
                    JOptionPane.showMessageDialog(this, "Tipo inesperado para selección.");
                    return;
                }
                EditorPreguntaSeleccionesDialog dlg =
                        new EditorPreguntaSeleccionesDialog(
                                SwingUtilities.getWindowAncestor(this),
                                original,
                                original.getTipo(),
                                original.getId()
                        );
                dlg.setVisible(true);
                if (dlg.isGuardado()) reemplazo = dlg.getPreguntaFinal();
            }
            case PAREO -> {
                if (!(p instanceof Pareo pr)) {
                    JOptionPane.showMessageDialog(this, "Tipo inesperado para pareo.");
                    return;
                }
                EditorPreguntaPareoDialog dlg =
                        new EditorPreguntaPareoDialog(
                                SwingUtilities.getWindowAncestor(this),
                                pr,
                                pr.getId()
                        );
                dlg.setVisible(true);
                if (dlg.isGuardado()) reemplazo = dlg.getPreguntaFinal();
            }
            case SOPA_LETRAS -> {
                if (!(p instanceof SopaDeLetras sl)) {
                    JOptionPane.showMessageDialog(this, "Tipo inesperado para sopa de letras.");
                    return;
                }
                EditorPreguntaSopaDialog dlg =
                        new EditorPreguntaSopaDialog(
                                SwingUtilities.getWindowAncestor(this),
                                sl,
                                sl.getId()
                        );
                dlg.setVisible(true);
                if (dlg.isGuardado()) reemplazo = dlg.getPreguntaFinal();
            }
            default -> {}
        }

        if (reemplazo != null) {
            // Reemplazar manteniendo la posición
            List<IPregunta> ps = evaluacion.getPreguntas();
            ps.remove(row);
            ps.add(row, reemplazo);

            evaluacion.calcularPuntajeTotal();
            recargar();
            tabla.getSelectionModel().setSelectionInterval(row, row);
        }
    }

    private void onEliminar() {
        int r = filaSel();
        if (r < 0) { JOptionPane.showMessageDialog(this, "Seleccione una pregunta."); return; }
        int ok = JOptionPane.showConfirmDialog(this, "¿Eliminar la pregunta seleccionada?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (ok == JOptionPane.YES_OPTION) {
            evaluacion.eliminarPregunta(r);
            recargar();
        }
    }


    private void onMover(int delta) {
        int r = filaSel();
        if (r < 0) { JOptionPane.showMessageDialog(this, "Seleccione una pregunta."); return; }
        int n = model.getRowCount();
        int to = r + delta;
        if (to < 0 || to >= n) return;

        evaluacion.moverPregunta(r, to);
        recargar();
        tabla.getSelectionModel().setSelectionInterval(to, to);
    }


    // Modelo de tabla
    static class ModeloTabla extends AbstractTableModel {
        private final String[] cols = {"#", "ID", "Tipo", "Descripción", "Puntos", "Detalle"};
        private List<IPregunta> data = new ArrayList<>();

        public void setData(List<IPregunta> list) {
            data = list == null ? new ArrayList<>() : list;
            fireTableDataChanged();
        }

        public IPregunta getAt(int row) { return data.get(row); }

        @Override public int getRowCount() { return data.size(); }
        @Override public int getColumnCount() { return cols.length; }
        @Override public String getColumnName(int c) { return cols[c]; }

        @Override public Object getValueAt(int r, int c) {
            IPregunta p = data.get(r);
            return switch (c) {
                case 0 -> r + 1;
                case 1 -> {
                    if (p instanceof Pregunta q) yield q.getId();
                    if (p instanceof Pareo pr) yield pr.getId();
                    if (p instanceof SopaDeLetras sl) yield sl.getId();
                    yield "-";
                }
                case 2 -> p.getTipo();
                case 3 -> p.obtenerDescripcion();
                case 4 -> p.obtenerPuntos();
                case 5 -> detalle(p);
                default -> "";
            };
        }

        private String detalle(IPregunta p) {
            if (p instanceof Pregunta q) {
                return "Opciones: " + (q.getRespuestas() == null ? 0 : q.getRespuestas().size());
            } else if (p instanceof Pareo pr) {
                int e = pr.getEnunciados() == null ? 0 : pr.getEnunciados().size();
                int r = pr.getRespuestas() == null ? 0 : pr.getRespuestas().size();
                int a = pr.getAsociaciones() == null ? 0 : pr.getAsociaciones().size();
                return "Pareo: " + e + "/" + r + " (" + a + " asociaciones)";
            } else if (p instanceof SopaDeLetras sl) {
                int n = sl.getEnunciados() == null ? 0 : sl.getEnunciados().size();
                return "Sopa: " + n + " palabras";
            }
            return "";
        }
    }
}


