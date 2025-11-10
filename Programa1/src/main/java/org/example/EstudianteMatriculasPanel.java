package org.example;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.List;

/**
 * Panel que muestra las matrículas del estudiante.
 * <p>
 * Permite visualizar los grupos en los que el estudiante está matriculado
 * y realizar acciones como actualizar la lista o desmatricularse de un grupo.
 * </p>
 */
public class EstudianteMatriculasPanel extends JPanel {

    // -- Atributos principales --

    /** Estudiante actual. */
    private final Estudiante estudiante;

    /** Servicio de cursos (usado para localizar el curso asociado a un grupo). */
    private final CursoService cursoService;

    /** Servicio de matrículas (para obtener y eliminar inscripciones). */
    private final MatriculaService matriculaService;

    // -- Componentes de interfaz --

    private final JTable tbl = new JTable();
    private final Model model = new Model();

    // -- Constructor --

    /**
     * Crea el panel de matrículas del estudiante.
     *
     * @param estudiante estudiante actual
     * @param cursoService servicio de cursos
     * @param matriculaService servicio de matrículas
     */
    public EstudianteMatriculasPanel(Estudiante estudiante, CursoService cursoService,
                                     MatriculaService matriculaService) {
        this.estudiante = estudiante;
        this.cursoService = cursoService;
        this.matriculaService = matriculaService;

        setLayout(new BorderLayout(8,8));
        tbl.setModel(model);
        add(new JScrollPane(tbl), BorderLayout.CENTER);

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnDesmatricular = new JButton("Desmatricular");
        JButton btnRefrescar = new JButton("Refrescar");
        south.add(btnDesmatricular);
        south.add(btnRefrescar);
        add(south, BorderLayout.SOUTH);

        btnRefrescar.addActionListener(e -> refrescar());
        btnDesmatricular.addActionListener(e -> onDesmatricular());

        refrescar();
    }

    // -- Carga de datos --

    /**
     * Carga o actualiza la lista de grupos en los que el estudiante está matriculado.
     */
    private void refrescar() {
        model.setData(matriculaService.gruposDelEstudiante(estudiante.getIdUsuario()));
    }

    // -- Selección --

    /**
     * Devuelve el grupo actualmente seleccionado en la tabla.
     *
     * @return grupo seleccionado o {@code null} si no hay selección
     */
    private Grupo seleccionado() {
        int r = tbl.getSelectedRow();
        return r < 0 ? null : model.getAt(r);
    }

    // -- Desmatricular --

    /**
     * Permite al estudiante desmatricularse del grupo seleccionado.
     * <p>
     * Si el grupo o el curso asociado no se encuentran, muestra un mensaje de error.
     * </p>
     */
    private void onDesmatricular() {
        Grupo g = seleccionado();
        if (g == null) { JOptionPane.showMessageDialog(this, "Seleccione un grupo."); return; }
        Curso curso = cursoPorGrupo(g);
        if (curso == null) { JOptionPane.showMessageDialog(this, "No se encontró el curso del grupo."); return; }
        try {
            matriculaService.desmatricular(estudiante.getIdUsuario(), curso, g.getIdGrupo());
            JOptionPane.showMessageDialog(this, "Desmatriculado del grupo #" + g.getIdGrupo());
            refrescar();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "No se pudo desmatricular", JOptionPane.ERROR_MESSAGE);
        }
    }

    // -- Auxiliares --

    /**
     * Busca el curso al que pertenece un grupo determinado.
     *
     * @param g grupo del cual se desea conocer el curso
     * @return curso correspondiente o {@code null} si no se encuentra
     */
    private Curso cursoPorGrupo(Grupo g) {
        for (Curso c : cursoService.listarCursos()) {
            for (Grupo x : c.grupos) {
                if (x == g || x.getIdGrupo() == g.getIdGrupo()) return c;
            }
        }
        return null;
    }

    // -- Modelo de tabla --

    /**
     * Modelo de tabla que representa los grupos matriculados del estudiante.
     */
    static class Model extends AbstractTableModel {

        private final String[] cols = {"Curso", "Grupo", "Inicio", "Final", "Profesor"};
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
                case 0 -> (g.getCurso() != null ? g.getCurso().getNombre() : "(curso)");
                case 1 -> g.getIdGrupo();
                case 2 -> g.getFechaInicio();
                case 3 -> g.getFechaFinal();
                case 4 -> g.getProfesor() == null ? "(sin asignar)" :
                        g.getProfesor().getNombre() + " " + g.getProfesor().getApellido1();
                default -> "";
            };
        }
    }
}

