package org.example;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel de seguimiento de evaluaciones para el profesor.
 * <p>
 * Permite listar todas las evaluaciones creadas por el profesor, obtenidas desde
 * el {@link EvaluacionService}, y ofrece la opción de simular una evaluación
 * sin afectar los datos almacenados (la simulación no se persiste).
 * </p>
 */
public class ProfesorSeguimientoPanel extends JPanel {

    // -- Servicios --

    /** Servicio de usuarios del sistema. */
    private final UsuarioServiceMem usuarioService;

    /** Servicio de cursos utilizado para obtener grupos. */
    private final CursoService cursoService;

    /** Servicio de evaluaciones usado para listar las evaluaciones del profesor. */
    private final EvaluacionService evaluacionService;

    /** Profesor autenticado que utiliza el panel. */
    private final Profesor profesor;

    // -- Componentes visuales --

    /** Tabla que muestra las evaluaciones del profesor. */
    private final JTable tblEvaluaciones = new JTable();

    /** Modelo de datos para la tabla de evaluaciones. */
    private final EvaluacionesModel evalModel = new EvaluacionesModel();

    /** Botón para simular una evaluación seleccionada. */
    private final JButton btnSimular = new JButton("Simular evaluación");

    /** Botón para refrescar la lista de evaluaciones. */
    private final JButton btnRefrescar = new JButton("Refrescar");

    // -- Constructor --

    /**
     * Crea un panel de seguimiento de evaluaciones para un profesor.
     *
     * @param usuarioService servicio de usuarios
     * @param cursoService servicio de cursos
     * @param evaluacionService servicio de evaluaciones
     * @param profesor profesor autenticado
     */
    public ProfesorSeguimientoPanel(UsuarioServiceMem usuarioService,
                                    CursoService cursoService,
                                    EvaluacionService evaluacionService,
                                    Profesor profesor) {
        this.usuarioService = usuarioService;
        this.cursoService = cursoService;
        this.evaluacionService = evaluacionService;
        this.profesor = profesor;

        setLayout(new BorderLayout(8, 8));
        construirUI();
        cargarEvaluaciones();
    }

    // -- Construcción de interfaz --

    /** Construye la estructura visual del panel. */
    private void construirUI() {
        JPanel north = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        north.add(btnRefrescar);
        north.add(btnSimular);

        tblEvaluaciones.setModel(evalModel);
        tblEvaluaciones.setFillsViewportHeight(true);

        add(new JScrollPane(tblEvaluaciones), BorderLayout.CENTER);
        add(north, BorderLayout.NORTH);

        btnRefrescar.addActionListener(e -> cargarEvaluaciones());
        btnSimular.addActionListener(e -> onSimular());
    }

    // -- Carga de datos --

    /** Carga las evaluaciones creadas por el profesor desde el servicio. */
    private void cargarEvaluaciones() {
        List<Evaluacion> evals = evaluacionService.listarPorProfesor(profesor.getIdUsuario());
        evalModel.setData(evals == null ? new ArrayList<>() : evals);
    }

    // -- Acciones --

    /**
     * Abre un simulador para la evaluación seleccionada en la tabla.
     * <p>
     * Esta simulación permite al profesor revisar el flujo de preguntas y respuestas
     * de una evaluación sin registrar resultados reales ni afectar los datos.
     * </p>
     */
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

    // -- Modelo interno de tabla --

    /**
     * Modelo de tabla que muestra las evaluaciones del profesor en formato legible.
     */
    static class EvaluacionesModel extends AbstractTableModel {

        private final String[] cols = {
                "Nombre", "Duración", "Puntaje total",
                "Preg. aleat", "Opc. aleat", "# Preguntas"
        };

        private List<Evaluacion> data = new ArrayList<>();

        /** Actualiza los datos de la tabla. */
        public void setData(List<Evaluacion> list) {
            data = list == null ? new ArrayList<>() : list;
            fireTableDataChanged();
        }

        /** Obtiene la evaluación correspondiente a una fila. */
        public Evaluacion getAt(int r) { return data.get(r); }

        @Override public int getRowCount() { return data.size(); }
        @Override public int getColumnCount() { return cols.length; }
        @Override public String getColumnName(int c) { return cols[c]; }

        @Override
        public Object getValueAt(int r, int c) {
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

