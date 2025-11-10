package org.example;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel de seguimiento para profesor:
 * - Lista sus evaluaciones desde EvaluacionService
 * - Permite simular una evaluación (no persiste)
 */
public class ProfesorSeguimientoPanel extends JPanel {

    private final UsuarioServiceMem usuarioService;
    private final CursoService cursoService;
    private final EvaluacionService evaluacionService; // <-- NUEVO
    private final Profesor profesor;

    private final JTable tblEvaluaciones = new JTable();
    private final EvaluacionesModel evalModel = new EvaluacionesModel();

    private final JButton btnSimular = new JButton("Simular evaluación");
    private final JButton btnRefrescar = new JButton("Refrescar");

    public ProfesorSeguimientoPanel(UsuarioServiceMem usuarioService,
                                    CursoService cursoService,
                                    EvaluacionService evaluacionService, // <-- NUEVO
                                    Profesor profesor) {
        this.usuarioService = usuarioService;
        this.cursoService = cursoService;
        this.evaluacionService = evaluacionService; // <-- NUEVO
        this.profesor = profesor;

        setLayout(new BorderLayout(8,8));
        construirUI();
        cargarEvaluaciones();
    }

    private void construirUI() {
        JPanel north = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        north.add(btnRefrescar);
        north.add(btnSimular);

        tblEvaluaciones.setModel(evalModel);

        add(new JScrollPane(tblEvaluaciones), BorderLayout.CENTER);
        add(north, BorderLayout.NORTH);

        btnRefrescar.addActionListener(e -> cargarEvaluaciones());
        btnSimular.addActionListener(e -> onSimular());
    }

    private void cargarEvaluaciones() {
        List<Evaluacion> evals = evaluacionService.listarPorProfesor(profesor.getIdUsuario());
        evalModel.setData(evals == null ? new ArrayList<>() : evals);
    }

    private void onSimular() {
        int r = tblEvaluaciones.getSelectedRow();
        if (r < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione una evaluación.");
            return;
        }
        Evaluacion ev = evalModel.getAt(r);
        SimuladorEvaluacionDialog dlg = new SimuladorEvaluacionDialog(
                SwingUtilities.getWindowAncestor(this),
                ev
        );
        dlg.setVisible(true);
    }

    // ===== modelo de tabla =====
    static class EvaluacionesModel extends AbstractTableModel {
        private final String[] cols = {"Nombre", "Duración", "Puntaje total", "Preg. aleat", "Opc. aleat", "#Preg"};
        private List<Evaluacion> data = new ArrayList<>();
        public void setData(List<Evaluacion> list) { data = list == null ? new ArrayList<>() : list; fireTableDataChanged(); }
        public Evaluacion getAt(int r) { return data.get(r); }
        @Override public int getRowCount() { return data.size(); }
        @Override public int getColumnCount() { return cols.length; }
        @Override public String getColumnName(int c) { return cols[c]; }
        @Override public Object getValueAt(int r, int c) {
            Evaluacion e = data.get(r);
            return switch (c) {
                case 0 -> e.getNombre();
                case 1 -> e.getDuracionMinutos();
                case 2 -> e.getPuntajeTotal();
                case 3 -> e.isPreguntasAleatorias();
                case 4 -> e.isOpcionesAleatorias();
                case 5 -> (e.getPreguntas() == null ? 0 : e.getPreguntas().size());
                default -> "";
            };
        }
    }
}
