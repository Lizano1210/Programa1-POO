package org.example;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel de interfaz gráfica que muestra una vista de solo lectura
 * con los grupos y cursos asignados al profesor.
 * <p>
 * Permite visualizar información básica de cada grupo, incluyendo el curso,
 * modalidad, tipo, cantidad de estudiantes matriculados y evaluaciones asignadas.
 * </p>
 */
public class ProfesorCursosGruposPanel extends JPanel {

    // -- Servicios --

    /** Servicio de usuarios (referencia para futuras operaciones). */
    private final UsuarioServiceMem usuarioService;

    /** Servicio de cursos utilizado para acceder a los grupos. */
    private final CursoService cursoService;

    /** Profesor actualmente autenticado o en sesión. */
    private final Profesor profesor;

    // -- Componentes de interfaz --

    /** Tabla que muestra los grupos asignados al profesor. */
    private final JTable tblGrupos = new JTable();

    /** Modelo de datos utilizado por la tabla. */
    private final ModeloGrupos model = new ModeloGrupos();

    /** Botón para recargar los datos. */
    private final JButton btnRefrescar = new JButton("Refrescar");

    // -- Constructor --

    /**
     * Crea el panel de “Mis Cursos y Grupos” del profesor.
     *
     * @param usuarioService servicio de usuarios
     * @param cursoService servicio de cursos
     * @param profesor profesor que utilizará el panel
     */
    public ProfesorCursosGruposPanel(UsuarioServiceMem usuarioService,
                                     CursoService cursoService,
                                     Profesor profesor) {
        this.usuarioService = usuarioService;
        this.cursoService = cursoService;
        this.profesor = profesor;

        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel north = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        north.add(btnRefrescar);

        tblGrupos.setModel(model);

        add(north, BorderLayout.NORTH);
        add(new JScrollPane(tblGrupos), BorderLayout.CENTER);

        btnRefrescar.addActionListener(e -> cargar());

        cargar();
    }

    // -- Carga de datos --

    /**
     * Carga los grupos asociados al profesor actual en la tabla.
     */
    private void cargar() {
        List<Grupo> grupos = profesor.getGrupos() == null ? new ArrayList<>() : profesor.getGrupos();
        model.setData(grupos);
    }

    // -- Modelo interno de tabla --

    /**
     * Modelo de tabla para mostrar los grupos del profesor.
     */
    static class ModeloGrupos extends AbstractTableModel {

        private final String[] cols = {
                "Grupo", "Curso", "Modalidad", "Tipo Curso", "Cupo Máx.",
                "Matriculados", "Asignaciones"
        };

        private List<Grupo> data = new ArrayList<>();

        /**
         * Actualiza los datos mostrados en la tabla.
         *
         * @param list lista de grupos a mostrar
         */
        public void setData(List<Grupo> list) {
            data = list == null ? new ArrayList<>() : list;
            fireTableDataChanged();
        }

        @Override
        public int getRowCount() { return data.size(); }

        @Override
        public int getColumnCount() { return cols.length; }

        @Override
        public String getColumnName(int c) { return cols[c]; }

        @Override
        public Object getValueAt(int r, int c) {
            Grupo g = data.get(r);
            return switch (c) {
                case 0 -> g.getCurso().getDescripcion(); // o g.getId()
                case 1 -> (g.getCurso() == null ? "-" : g.getCurso().getNombre());
                case 2 -> (g.getCurso() == null ? "-" : String.valueOf(g.getCurso().getModalidad()));
                case 3 -> (g.getCurso() == null ? "-" : String.valueOf(g.getCurso().getTipo()));
                case 4 -> g.getCurso().getMaxEstu();
                case 5 -> (g.getMatriculas() == null ? 0 : g.getMatriculas().size());
                case 6 -> (g.getEvaluacionesAsignadas() == null ? 0 : g.getEvaluacionesAsignadas().size());
                default -> "";
            };
        }
    }
}
