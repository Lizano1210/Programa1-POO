package org.example;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

/**
 * Panel que muestra la oferta de cursos y grupos disponibles para el estudiante.
 * <p>
 * Permite visualizar los cursos activos, explorar los grupos asociados y matricularse
 * directamente en uno de ellos desde la interfaz.
 * </p>
 */
public class EstudianteOfertaPanel extends JPanel {

    // -- Atributos principales --

    /** Estudiante actual que visualiza la oferta. */
    private final Estudiante estudiante;

    /** Servicio de cursos (provee la información de los cursos y grupos). */
    private final CursoService cursoService;

    /** Servicio de matrículas (gestiona las inscripciones del estudiante). */
    private final MatriculaService matriculaService;

    // -- Componentes de interfaz --

    private final JTable tblCursos = new JTable();
    private final JTable tblGrupos = new JTable();
    private final CursosModel cursosModel = new CursosModel();
    private final GruposModel gruposModel = new GruposModel();

    // -- Constructor --

    /**
     * Crea el panel de oferta de cursos y grupos.
     *
     * @param estudiante estudiante actual
     * @param cursoService servicio de cursos
     * @param matriculaService servicio de matrículas
     */
    public EstudianteOfertaPanel(Estudiante estudiante,
                                 CursoService cursoService,
                                 MatriculaService matriculaService) {
        this.estudiante = estudiante;
        this.cursoService = cursoService;
        this.matriculaService = matriculaService;

        setLayout(new BorderLayout(8,8));

        // -- Tabla de cursos --
        tblCursos.setModel(cursosModel);
        tblCursos.getSelectionModel().addListSelectionListener(_evt -> cargarGrupos());
        add(new JScrollPane(tblCursos), BorderLayout.NORTH);

        // -- Tabla de grupos asociados al curso seleccionado --
        tblGrupos.setModel(gruposModel);
        add(new JScrollPane(tblGrupos), BorderLayout.CENTER);

        // -- Panel de acciones --
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnMatricular = new JButton("Matricularme en el grupo seleccionado");
        south.add(btnMatricular);
        add(south, BorderLayout.SOUTH);

        btnMatricular.addActionListener(e -> onMatricular());

        refrescarCursos();
    }

    // -- Carga de datos --

    /**
     * Carga la lista de cursos disponibles desde el servicio.
     * Si existen cursos, selecciona automáticamente el primero.
     */
    private void refrescarCursos() {
        cursosModel.setData(cursoService.listarCursos());
        if (cursosModel.getRowCount() > 0) {
            tblCursos.setRowSelectionInterval(0, 0);
        } else {
            gruposModel.setData(List.of());
        }
    }

    // -- Selección --

    /** @return curso actualmente seleccionado o {@code null} si no hay selección */
    private Curso cursoSeleccionado() {
        int r = tblCursos.getSelectedRow();
        return r < 0 ? null : cursosModel.getAt(r);
    }

    /** @return grupo actualmente seleccionado o {@code null} si no hay selección */
    private Grupo grupoSeleccionado() {
        int r = tblGrupos.getSelectedRow();
        return r < 0 ? null : gruposModel.getAt(r);
    }

    // -- Carga de grupos --

    /**
     * Carga los grupos correspondientes al curso seleccionado.
     */
    private void cargarGrupos() {
        Curso c = cursoSeleccionado();
        if (c == null) {
            gruposModel.setData(List.of());
            return;
        }
        gruposModel.setData(c.grupos);
        if (gruposModel.getRowCount() > 0) tblGrupos.setRowSelectionInterval(0, 0);
    }

    // -- Matrícula --

    /**
     * Permite matricular al estudiante en el grupo actualmente seleccionado.
     */
    private void onMatricular() {
        Curso c = cursoSeleccionado();
        Grupo g = grupoSeleccionado();
        if (c == null || g == null) {
            JOptionPane.showMessageDialog(this, "Selecciona un curso y un grupo.");
            return;
        }
        try {
            matriculaService.matricular(estudiante.getIdUsuario(), c, g.getIdGrupo());
            JOptionPane.showMessageDialog(this, "Matrícula realizada.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "No se pudo matricular", JOptionPane.ERROR_MESSAGE);
        }
    }

    // -- Modelos de tabla --

    /**
     * Modelo de tabla que representa los cursos disponibles en la oferta.
     */
    static class CursosModel extends AbstractTableModel {
        private final String[] cols = {"ID", "Nombre", "Modalidad", "Tipo", "Min", "Max", "Horas/día", "Aprobación"};
        private List<Curso> data = List.of();

        /** Actualiza los datos del modelo. */
        public void setData(List<Curso> list) { data = list==null?List.of():list; fireTableDataChanged(); }

        /** Devuelve el curso correspondiente a una fila. */
        public Curso getAt(int row) { return data.get(row); }

        @Override public int getRowCount() { return data.size(); }
        @Override public int getColumnCount() { return cols.length; }
        @Override public String getColumnName(int c) { return cols[c]; }

        @Override
        public Object getValueAt(int r, int c) {
            Curso x = data.get(r);
            return switch (c) {
                case 0 -> x.getId();
                case 1 -> x.getNombre();
                case 2 -> x.getModalidad();
                case 3 -> x.getTipo();
                case 4 -> x.getMinEstu();
                case 5 -> x.getMaxEstu();
                case 6 -> x.getHrsDia();
                case 7 -> x.getAprobCalificacion();
                default -> "";
            };
        }
    }

    /**
     * Modelo de tabla que representa los grupos disponibles de un curso.
     */
    static class GruposModel extends AbstractTableModel {
        private final String[] cols = {"#", "Inicio", "Final", "Profesor", "Cupo"};
        private List<Grupo> data = List.of();

        /** Actualiza los datos del modelo. */
        public void setData(List<Grupo> list) { data = list==null?List.of():list; fireTableDataChanged(); }

        /** Devuelve el grupo correspondiente a una fila. */
        public Grupo getAt(int row) { return data.get(row); }

        @Override public int getRowCount() { return data.size(); }
        @Override public int getColumnCount() { return cols.length; }
        @Override public String getColumnName(int c) { return cols[c]; }

        @Override
        public Object getValueAt(int r, int c) {
            Grupo g = data.get(r);
            return switch (c) {
                case 0 -> g.getIdGrupo();
                case 1 -> g.getFechaInicio();
                case 2 -> g.getFechaFinal();
                case 3 -> g.getProfesor() == null ? "(sin asignar)" :
                        g.getProfesor().getNombre() + " " + g.getProfesor().getApellido1();
                case 4 -> (g.getMatriculas() == null ? 0 : g.getMatriculas().size());
                default -> "";
            };
        }
    }
}
