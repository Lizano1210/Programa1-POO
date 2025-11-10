package org.example;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/** Panel "Mis Cursos y Grupos": vista de solo lectura de los grupos impartidos por el profesor. */
public class ProfesorCursosGruposPanel extends JPanel {

    private final UsuarioServiceMem usuarioService;
    private final CursoService cursoService;
    private final Profesor profesor;

    private final JTable tblGrupos = new JTable();
    private final ModeloGrupos model = new ModeloGrupos();

    private final JButton btnRefrescar = new JButton("Refrescar");

    public ProfesorCursosGruposPanel(UsuarioServiceMem usuarioService,
                                     CursoService cursoService,
                                     Profesor profesor) {
        this.usuarioService = usuarioService;
        this.cursoService = cursoService;
        this.profesor = profesor;

        setLayout(new BorderLayout(8,8));
        setBorder(BorderFactory.createEmptyBorder(12,12,12,12));

        JPanel north = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        north.add(btnRefrescar);

        tblGrupos.setModel(model);
        add(north, BorderLayout.NORTH);
        add(new JScrollPane(tblGrupos), BorderLayout.CENTER);

        btnRefrescar.addActionListener(e -> cargar());

        cargar();
    }

    private void cargar() {
        List<Grupo> grupos = profesor.getGrupos() == null ? new ArrayList<>() : profesor.getGrupos();
        model.setData(grupos);
    }

    static class ModeloGrupos extends AbstractTableModel {
        private final String[] cols = {"Grupo", "Curso", "Modalidad", "Tipo Curso", "Cupo Max", "Matriculados", "Asignaciones"};
        private List<Grupo> data = new ArrayList<>();
        public void setData(List<Grupo> list) { data = list == null ? new ArrayList<>() : list; fireTableDataChanged(); }
        @Override public int getRowCount() { return data.size(); }
        @Override public int getColumnCount() { return cols.length; }
        @Override public String getColumnName(int c) { return cols[c]; }
        @Override public Object getValueAt(int r, int c) {
            Grupo g = data.get(r);
            return switch (c) {
                case 0 -> g.getCurso().getDescripcion(); // o g.getId()
                case 1 -> (g.getCurso()==null?"-":g.getCurso().getNombre());
                case 2 -> (g.getCurso()==null?"-":String.valueOf(g.getCurso().getModalidad()));
                case 3 -> (g.getCurso()==null?"-":String.valueOf(g.getCurso().getTipo()));
                case 4 -> g.getCurso().getMaxEstu();
                case 5 -> (g.getMatriculas()==null?0:g.getMatriculas().size());
                case 6 -> (g.getEvaluacionesAsignadas()==null?0:g.getEvaluacionesAsignadas().size());
                default -> "";
            };
        }
    }
}
