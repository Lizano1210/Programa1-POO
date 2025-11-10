package org.example;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.List;

/**
 * Panel para la gestión de cursos.
 * Permite listar, crear, editar, eliminar y gestionar los grupos
 * asociados a cada curso.
 */
public class AdminCursosPanel extends JPanel {

    /** Servicio encargado de la gestión de cursos. */
    private final CursoService cursoService;

    /** Servicio de usuarios, usado para elegir profesor en los grupos. */
    private final UsuarioService usuarioService;

    /** Tabla que muestra los cursos registrados. */
    private final JTable tblCursos = new JTable();

    /** Modelo de tabla con los datos de los cursos. */
    private final CursosModel cursosModel = new CursosModel();

    /**
     * Crea un nuevo panel de administración de cursos.
     *
     * @param cursoService servicio encargado de manejar los cursos
     * @param usuarioService servicio de usuarios, usado en la gestión de grupos
     */
    public AdminCursosPanel(CursoService cursoService, UsuarioService usuarioService) {
        this.cursoService = cursoService;
        this.usuarioService = usuarioService;

        setLayout(new BorderLayout());

        // --- Tabla de cursos ---
        tblCursos.setModel(cursosModel);
        add(new JScrollPane(tblCursos), BorderLayout.CENTER);

        // --- Panel de acciones ---
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnNuevo = new JButton("Nuevo curso");
        JButton btnEditar = new JButton("Editar curso");
        JButton btnEliminar = new JButton("Eliminar curso");
        JButton btnGrupos = new JButton("Gestionar grupos");
        JButton btnRefrescar = new JButton("Refrescar");
        actions.add(btnNuevo);
        actions.add(btnEditar);
        actions.add(btnEliminar);
        actions.add(btnGrupos);
        actions.add(btnRefrescar);
        add(actions, BorderLayout.SOUTH);

        // --- Eventos de los botones ---
        btnRefrescar.addActionListener(_evt -> refrescar());
        btnNuevo.addActionListener(_evt -> onNuevo());
        btnEditar.addActionListener(_evt -> onEditar());
        btnEliminar.addActionListener(_evt -> onEliminar());
        btnGrupos.addActionListener(_evt -> onGrupos());

        refrescar();
    }

    /**
     * Actualiza la tabla con los cursos más recientes.
     */
    private void refrescar() {
        cursosModel.setData(cursoService.listarCursos());
    }

    /**
     * Obtiene el curso actualmente seleccionado en la tabla.
     *
     * @return el curso seleccionado o {@code null} si no hay selección
     */
    private Curso getSeleccionado() {
        int r = tblCursos.getSelectedRow();
        return (r < 0) ? null : cursosModel.getAt(r);
    }

    /**
     * Acción para crear un nuevo curso.
     * Abre un diálogo y, si el usuario guarda, agrega el curso.
     */
    private void onNuevo() {
        AdminCursoDialog dlg = new AdminCursoDialog(SwingUtilities.getWindowAncestor(this), null);
        dlg.setVisible(true);
        Curso nuevo = dlg.getResultado();
        if (nuevo != null) {
            try {
                cursoService.agregarCurso(nuevo);
                refrescar();
                JOptionPane.showMessageDialog(this, "Curso agregado.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Acción para editar el curso seleccionado.
     * Si el usuario guarda los cambios, actualiza la información del curso.
     */
    private void onEditar() {
        Curso sel = getSeleccionado();
        if (sel == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un curso.");
            return;
        }
        AdminCursoDialog dlg = new AdminCursoDialog(SwingUtilities.getWindowAncestor(this), sel);
        dlg.setVisible(true);
        Curso editado = dlg.getResultado();
        if (editado != null) {
            try {
                cursoService.actualizarCurso(editado);
                refrescar();
                JOptionPane.showMessageDialog(this, "Curso actualizado.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Acción para eliminar el curso seleccionado.
     * Pide confirmación antes de eliminarlo.
     */
    private void onEliminar() {
        Curso sel = getSeleccionado();
        if (sel == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un curso.");
            return;
        }
        int r = JOptionPane.showConfirmDialog(this,
                "¿Eliminar el curso " + sel.getNombre() + "?",
                "Confirmar", JOptionPane.YES_NO_OPTION);
        if (r == JOptionPane.YES_OPTION) {
            try {
                cursoService.eliminarCurso(sel.getId());
                refrescar();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(),
                        "No se pudo eliminar", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Acción para abrir la ventana de gestión de grupos de un curso.
     */
    private void onGrupos() {
        Curso sel = getSeleccionado();
        if (sel == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un curso.");
            return;
        }
        AdminGruposDialog dlg = new AdminGruposDialog(
                SwingUtilities.getWindowAncestor(this),
                sel,
                cursoService,
                usuarioService
        );
        dlg.setVisible(true);
        // Al cerrar, se actualiza la tabla por si hubo cambios
        refrescar();
    }

    // --- Modelo de tabla interno ---

    /**
     * Modelo de tabla que muestra los cursos en la interfaz.
     * Define las columnas y los datos que se presentan.
     */
    private static class CursosModel extends AbstractTableModel {

        /** Nombres de las columnas mostradas en la tabla. */
        private final String[] cols = {
                "ID", "Nombre", "Modalidad", "Tipo",
                "Min", "Max", "Horas/día", "Aprobación"
        };

        /** Lista de cursos a mostrar. */
        private List<Curso> data = List.of();

        /**
         * Establece los datos de la tabla.
         *
         * @param list lista de cursos a mostrar
         */
        public void setData(List<Curso> list) {
            data = (list == null ? List.of() : list);
            fireTableDataChanged();
        }

        /**
         * Obtiene el curso correspondiente a una fila.
         *
         * @param row índice de la fila
         * @return curso asociado a la fila
         */
        public Curso getAt(int row) {
            return data.get(row);
        }

        @Override
        public int getRowCount() {
            return data.size();
        }

        @Override
        public int getColumnCount() {
            return cols.length;
        }

        @Override
        public String getColumnName(int c) {
            return cols[c];
        }

        @Override
        public Object getValueAt(int r, int c) {
            Curso x = data.get(r);
            return switch (c) {
                case 0 -> x.getId();
                case 1 -> x.getNombre();
                case 2 -> String.valueOf(x.getModalidad());
                case 3 -> String.valueOf(x.getTipo());
                case 4 -> x.getMinEstu();
                case 5 -> x.getMaxEstu();
                case 6 -> x.getHrsDia();
                case 7 -> x.getAprobCalificacion();
                default -> "";
            };
        }
    }
}
