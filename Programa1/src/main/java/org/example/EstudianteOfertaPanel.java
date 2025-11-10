package org.example;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

public class EstudianteOfertaPanel extends JPanel {

    private final Estudiante estudiante;
    private final CursoService cursoService;
    private final MatriculaService matriculaService;

    private final JTable tblCursos = new JTable();
    private final JTable tblGrupos = new JTable();
    private final CursosModel cursosModel = new CursosModel();
    private final GruposModel gruposModel = new GruposModel();

    public EstudianteOfertaPanel(Estudiante estudiante,
                                 CursoService cursoService,
                                 MatriculaService matriculaService) {
        this.estudiante = estudiante;
        this.cursoService = cursoService;
        this.matriculaService = matriculaService;

        setLayout(new BorderLayout(8,8));

        // Arriba: cursos
        tblCursos.setModel(cursosModel);
        tblCursos.getSelectionModel().addListSelectionListener(_evt -> cargarGrupos());
        add(new JScrollPane(tblCursos), BorderLayout.NORTH);

        // Centro: grupos del curso seleccionado
        tblGrupos.setModel(gruposModel);
        add(new JScrollPane(tblGrupos), BorderLayout.CENTER);

        // Abajo: Acciones
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnMatricular = new JButton("Matricularme en el grupo seleccionado");
        south.add(btnMatricular);
        add(south, BorderLayout.SOUTH);

        btnMatricular.addActionListener(e -> onMatricular());

        refrescarCursos();
    }

    private void refrescarCursos() {
        cursosModel.setData(cursoService.listarCursos());
        if (cursosModel.getRowCount() > 0) {
            tblCursos.setRowSelectionInterval(0, 0);
        } else {
            gruposModel.setData(List.of());
        }
    }

    private Curso cursoSeleccionado() {
        int r = tblCursos.getSelectedRow();
        return r < 0 ? null : cursosModel.getAt(r);
    }

    private Grupo grupoSeleccionado() {
        int r = tblGrupos.getSelectedRow();
        return r < 0 ? null : gruposModel.getAt(r);
    }

    private void cargarGrupos() {
        Curso c = cursoSeleccionado();
        if (c == null) { gruposModel.setData(List.of()); return; }
        // Aquí puedes filtrar por vigencia si quieres (por ahora, todos los grupos)
        gruposModel.setData(c.grupos);
        if (gruposModel.getRowCount() > 0) tblGrupos.setRowSelectionInterval(0, 0);
    }

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

    // ===== modelos de tabla =====
    static class CursosModel extends AbstractTableModel {
        private final String[] cols = {"ID", "Nombre", "Modalidad", "Tipo", "Min", "Max", "Horas/día", "Aprobación"};
        private List<Curso> data = List.of();
        public void setData(List<Curso> list) { data = list==null?List.of():list; fireTableDataChanged(); }
        public Curso getAt(int row) { return data.get(row); }
        @Override public int getRowCount() { return data.size(); }
        @Override public int getColumnCount() { return cols.length; }
        @Override public String getColumnName(int c) { return cols[c]; }
        @Override public Object getValueAt(int r, int c) {
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

    static class GruposModel extends AbstractTableModel {
        private final String[] cols = {"#", "Inicio", "Final", "Profesor", "Cupo"};
        private List<Grupo> data = List.of();
        public void setData(List<Grupo> list) { data = list==null?List.of():list; fireTableDataChanged(); }
        public Grupo getAt(int row) { return data.get(row); }
        @Override public int getRowCount() { return data.size(); }
        @Override public int getColumnCount() { return cols.length; }
        @Override public String getColumnName(int c) { return cols[c]; }
        @Override public Object getValueAt(int r, int c) {
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
