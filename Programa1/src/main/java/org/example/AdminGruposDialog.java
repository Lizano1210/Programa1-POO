package org.example;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

/**
 * Diálogo de administración de grupos de un curso.
 * Permite listar, crear, editar, eliminar y asignar profesores a los grupos.
 */
public class AdminGruposDialog extends JDialog {

    /** Curso al que pertenecen los grupos. */
    private final Curso curso;

    /** Servicio para gestionar cursos y grupos. */
    private final CursoService cursoService;

    /** Servicio para acceder a los usuarios (profesores). */
    private final UsuarioService usuarioService;

    /** Tabla que muestra los grupos. */
    private final JTable tbl = new JTable();

    /** Modelo de datos de la tabla de grupos. */
    private final GruposModel model = new GruposModel();

    /** Selector de fecha de inicio. */
    private final JSpinner spInicio = new JSpinner(new SpinnerDateModel());

    /** Selector de fecha final. */
    private final JSpinner spFinal = new JSpinner(new SpinnerDateModel());

    /**
     * Crea el diálogo para administrar los grupos de un curso.
     *
     * @param owner ventana propietaria del diálogo
     * @param curso curso al que pertenecen los grupos
     * @param cursoService servicio para manejar cursos y grupos
     * @param usuarioService servicio para acceder a los profesores
     */
    public AdminGruposDialog(Window owner, Curso curso, CursoService cursoService, UsuarioService usuarioService) {
        super(owner, "Grupos de " + curso.getNombre(), ModalityType.APPLICATION_MODAL);
        this.curso = curso;
        this.cursoService = cursoService;
        this.usuarioService = usuarioService;

        setLayout(new BorderLayout(8,8));

        // -- Tabla principal --
        tbl.setModel(model);
        add(new JScrollPane(tbl), BorderLayout.CENTER);

        // -- Panel de acciones --
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnNuevo = new JButton("Nuevo grupo");
        JButton btnEditar = new JButton("Editar fechas");
        JButton btnEliminar = new JButton("Eliminar");
        JButton btnAsignar = new JButton("Asignar profesor");
        JButton btnCerrar = new JButton("Cerrar");
        actions.add(btnNuevo);
        actions.add(btnEditar);
        actions.add(btnEliminar);
        actions.add(btnAsignar);
        actions.add(btnCerrar);
        add(actions, BorderLayout.SOUTH);

        // -- Eventos de los botones --
        btnNuevo.addActionListener(_evt -> onNuevo());
        btnEditar.addActionListener(_evt -> onEditar());
        btnEliminar.addActionListener(_evt -> onEliminar());
        btnAsignar.addActionListener(_evt -> onAsignar());
        btnCerrar.addActionListener(_evt -> dispose());

        refrescar();
        setSize(800, 400);
        setLocationRelativeTo(owner);
    }

    /**
     * Actualiza la tabla de grupos con los datos más recientes.
     */
    private void refrescar() {
        model.setData(cursoService.listarGrupos(curso));
    }

    /**
     * Obtiene el grupo actualmente seleccionado en la tabla.
     *
     * @return grupo seleccionado o {@code null} si no hay ninguno
     */
    private Grupo getSel() {
        int r = tbl.getSelectedRow();
        return (r < 0) ? null : model.getAt(r);
    }

    // -- Crear nuevo grupo --

    /**
     * Permite crear un nuevo grupo, solicitando las fechas de inicio y fin.
     */
    private void onNuevo() {
        LocalDate inicio = askFecha("Fecha de inicio (YYYY-MM-DD):");
        if (inicio == null) return;
        LocalDate fin = askFecha("Fecha final (YYYY-MM-DD):");
        if (fin == null) return;
        try {
            cursoService.crearGrupo(curso, inicio, fin);
            refrescar();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // -- Editar fechas de grupo --

    /**
     * Permite editar las fechas de inicio y fin del grupo seleccionado.
     */
    private void onEditar() {
        Grupo g = getSel();
        if (g == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un grupo.");
            return;
        }
        LocalDate inicio = askFecha("Nueva fecha de inicio (YYYY-MM-DD), actual: " + g.getFechaInicio());
        if (inicio == null) return;
        LocalDate fin = askFecha("Nueva fecha final (YYYY-MM-DD), actual: " + g.getFechaFinal());
        if (fin == null) return;
        try {
            cursoService.actualizarGrupoFechas(curso, g.getIdGrupo(), inicio, fin);
            refrescar();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // -- Eliminar grupo --

    /**
     * Elimina el grupo seleccionado después de una confirmación.
     */
    private void onEliminar() {
        Grupo g = getSel();
        if (g == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un grupo.");
            return;
        }
        int r = JOptionPane.showConfirmDialog(this,
                "¿Eliminar grupo #" + g.getIdGrupo() + "?",
                "Confirmar", JOptionPane.YES_NO_OPTION);
        if (r == JOptionPane.YES_OPTION) {
            try {
                cursoService.eliminarGrupo(curso, g.getIdGrupo());
                refrescar();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(),
                        "No se pudo eliminar", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // -- Asignar profesor --

    /**
     * Permite asignar un profesor al grupo seleccionado.
     */
    private void onAsignar() {
        Grupo g = getSel();
        if (g == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un grupo.");
            return;
        }
        List<Profesor> profs = usuarioService.listarProfesores();
        if (profs.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay profesores registrados.");
            return;
        }

        Profesor elegido = (Profesor) JOptionPane.showInputDialog(
                this,
                "Seleccione profesor:",
                "Asignar profesor",
                JOptionPane.QUESTION_MESSAGE,
                null,
                profs.toArray(),
                g.getProfesor()
        );

        if (elegido != null) {
            try {
                cursoService.asignarProfesor(curso, g.getIdGrupo(), elegido);
                elegido.getGrupos().add(g);
                refrescar();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // -- Solicitud de fecha --

    /**
     * Muestra un cuadro de diálogo para ingresar una fecha en formato YYYY-MM-DD.
     *
     * @param msg mensaje a mostrar en el cuadro de entrada
     * @return la fecha ingresada o {@code null} si se cancela
     */
    private LocalDate askFecha(String msg) {
        String s = JOptionPane.showInputDialog(this, msg);
        if (s == null) return null;
        try {
            return LocalDate.parse(s.trim());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Formato inválido. Use YYYY-MM-DD.");
            return null;
        }
    }

    // -- Modelo de tabla para grupos --

    /**
     * Modelo de tabla que muestra los grupos del curso.
     */
    private static class GruposModel extends AbstractTableModel {

        /** Nombres de las columnas de la tabla. */
        private final String[] cols = {"#", "Inicio", "Final", "Profesor"};

        /** Lista de grupos a mostrar. */
        private List<Grupo> data = List.of();

        /**
         * Establece los datos del modelo.
         *
         * @param list lista de grupos
         */
        public void setData(List<Grupo> list) {
            data = (list == null ? List.of() : list);
            fireTableDataChanged();
        }

        /**
         * Obtiene el grupo correspondiente a una fila.
         *
         * @param row índice de la fila
         * @return grupo en esa posición
         */
        public Grupo getAt(int row) {
            return data.get(row);
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
                case 0 -> g.getIdGrupo();
                case 1 -> String.valueOf(g.getFechaInicio());
                case 2 -> String.valueOf(g.getFechaFinal());
                case 3 -> g.getProfesor() == null
                        ? "(sin asignar)"
                        : g.getProfesor().getNombre() + " " + g.getProfesor().getApellido1();
                default -> "";
            };
        }
    }
}

