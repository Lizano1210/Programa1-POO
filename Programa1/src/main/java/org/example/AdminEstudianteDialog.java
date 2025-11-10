package org.example;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Diálogo modal para crear o editar un estudiante.
 * Permite ingresar y validar los datos personales del estudiante
 * antes de guardarlos o actualizarlos.
 */
public class AdminEstudianteDialog extends JDialog {

    // -- Dependencias --

    /** Servicio de autenticación para registrar credenciales. */
    private final Autenticacion auth;

    /** Servicio de usuarios para validar unicidad de correo. */
    private final UsuarioService usuarioService;

    /** Estudiante base (si es edición). */
    private final Estudiante baseRef;

    // -- Campos de formulario --

    /** Campo de texto para el nombre del estudiante. */
    private final JTextField txtNombre = new JTextField(20);

    /** Campo de texto para el primer apellido. */
    private final JTextField txtApe1 = new JTextField(20);

    /** Campo de texto para el segundo apellido. */
    private final JTextField txtApe2 = new JTextField(20);

    /** Campo de texto para el identificador de usuario. */
    private final JTextField txtId = new JTextField(12);

    /** Campo de texto para el número de teléfono. */
    private final JTextField txtTel = new JTextField(12);

    /** Campo de texto para el correo electrónico. */
    private final JTextField txtCorreo = new JTextField(24);

    /** Campo de texto para la dirección. */
    private final JTextField txtDir = new JTextField(30);

    /** Campo de texto para la organización de origen. */
    private final JTextField txtOrgDL = new JTextField(20);

    /** Campo de texto para los temas de interés (separados por comas). */
    private final JTextField txtTemIN = new JTextField(30);

    /** Resultado del diálogo: estudiante creado o editado (o null si se cancela). */
    private Estudiante resultado;

    // -- Constructor --

    /**
     * Crea un nuevo diálogo para registrar o editar un estudiante.
     *
     * @param owner ventana propietaria del diálogo
     * @param base  estudiante base a editar; si es {@code null}, se creará uno nuevo
     * @param auth  servicio de autenticación para registrar la contraseña del estudiante
     * @param usuarioService servicio de usuarios para validar unicidad de correo
     */
    public AdminEstudianteDialog(Window owner, Estudiante base, Autenticacion auth, UsuarioService usuarioService) {
        super(owner, "Estudiante", ModalityType.APPLICATION_MODAL);
        this.auth = auth;
        this.usuarioService = usuarioService;
        this.baseRef = base;

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(8, 8));

        // -- Formulario principal --
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 4, 4, 4);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;

        addRow(form, c, 0, "Nombre", txtNombre);
        addRow(form, c, 1, "Apellido 1", txtApe1);
        addRow(form, c, 2, "Apellido 2", txtApe2);
        addRow(form, c, 3, "ID Usuario", txtId);
        addRow(form, c, 4, "Teléfono", txtTel);
        addRow(form, c, 5, "Correo", txtCorreo);
        addRow(form, c, 6, "Dirección", txtDir);
        addRow(form, c, 7, "Org. de origen (orgDL)", txtOrgDL);
        addRow(form, c, 8, "Temas de interés (temIN, coma-sep.)", txtTemIN);
        add(form, BorderLayout.CENTER);

        // -- Botones de acción --
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnOk = new JButton("Guardar");
        JButton btnCancel = new JButton("Cancelar");
        actions.add(btnOk);
        actions.add(btnCancel);
        add(actions, BorderLayout.SOUTH);

        // -- Precargar datos si es edición --
        if (base != null) {
            txtNombre.setText(base.getNombre());
            txtApe1.setText(base.getApellido1());
            txtApe2.setText(base.getApellido2());
            txtId.setText(base.getIdUsuario());
            txtId.setEnabled(false); // ID no editable
            txtTel.setText(base.getTelefono());
            txtCorreo.setText(base.getCorreo());
            txtDir.setText(base.getDireccion());
            txtOrgDL.setText(base.getOrgDL());
            List<String> t = base.getTemIN();
            txtTemIN.setText(t == null ? "" : String.join(", ", t));
        }

        // -- Eventos --
        btnOk.addActionListener(e -> onGuardar(base != null));
        btnCancel.addActionListener(e -> {
            resultado = null;
            dispose();
        });

        pack();
        setLocationRelativeTo(owner);
    }

    // -- Construcción del formulario --

    /** Agrega una fila al formulario con etiqueta y componente. */
    private void addRow(JPanel panel, GridBagConstraints c, int row, String label, JComponent comp) {
        c.gridx = 0; c.gridy = row; c.weightx = 0;
        panel.add(new JLabel(label), c);
        c.gridx = 1; c.gridy = row; c.weightx = 1;
        panel.add(comp, c);
    }

    // -- Lógica de guardado --

    /**
     * Valida los datos ingresados y crea un nuevo objeto {@link Estudiante}.
     *
     * @param esEdicion indica si se está editando un registro existente
     */
    private void onGuardar(boolean esEdicion) {
        try {
            String nombre = req(txtNombre.getText(), "Nombre", 2);
            String a1 = req(txtApe1.getText(), "Apellido 1", 2);
            String a2 = req(txtApe2.getText(), "Apellido 2", 0); // puede estar vacío
            String id = esEdicion ? txtId.getText() : req(txtId.getText(), "ID Usuario", 9);
            String tel = req(txtTel.getText(), "Teléfono", 8);
            String correo = validarCorreo(txtCorreo.getText(), esEdicion);
            String dir = req(txtDir.getText(), "Dirección", 5);
            String org = req(txtOrgDL.getText(), "Org. de origen", 2);
            List<String> tem = parseTemIN(txtTemIN.getText());

            Estudiante e = new Estudiante(nombre, a1, a2, id, tel, correo, dir, org, tem);

            // -- Credenciales automáticas para alta de estudiante --
            if (!esEdicion && auth != null) {
                Password pw = new Password(id, "", false);
                String temp = pw.tempPassword();               // genera, encripta y marca temporal
                auth.upsertUsuario(id, correo, Roles.ESTUDIANTE, pw);
                System.out.println("[Credenciales] Estudiante " + id + " -> contraseña temporal: " + temp);
            }

            this.resultado = e;
            dispose();

        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validación", JOptionPane.WARNING_MESSAGE);
        }
    }

    // -- Validaciones y utilidades --

    /** Verifica que un campo tenga una longitud mínima. */
    private String req(String v, String campo, int min) {
        String s = v == null ? "" : v.trim();
        if (s.length() < min)
            throw new IllegalArgumentException("Campo '" + campo + "' inválido (mín. " + min + ").");
        return s;
    }

    /**
     * Valida el correo bajo las reglas:
     * - formato parte1@parte2
     * - sin espacios
     * - parte1 y parte2 con longitud >= 3
     * - único en el sistema (estudiantes y profesores)
     */
    private String validarCorreo(String v, boolean esEdicion) {
        String s = v == null ? "" : v.trim();
        if (s.isEmpty()) throw new IllegalArgumentException("Campo 'Correo' inválido (requerido).");
        if (s.contains(" ")) throw new IllegalArgumentException("El correo no debe contener espacios.");
        int at = s.indexOf('@');
        if (at <= 0 || at != s.lastIndexOf('@'))
            throw new IllegalArgumentException("Correo inválido: use formato parte1@parte2.");
        String p1 = s.substring(0, at);
        String p2 = s.substring(at + 1);
        if (p1.length() < 3 || p2.length() < 3)
            throw new IllegalArgumentException("Correo inválido: cada parte debe tener al menos 3 caracteres.");

        // Unicidad (permite el mismo correo si estás editando y no lo cambias)
        if (usuarioService != null) {
            String correoActual = (baseRef == null) ? null : (baseRef.getCorreo() == null ? null : baseRef.getCorreo().trim());
            boolean mismoQueAntes = esEdicion && correoActual != null && correoActual.equalsIgnoreCase(s);

            if (!mismoQueAntes) {
                boolean usadoPorEst = usuarioService.listarEstudiantes().stream()
                        .anyMatch(e -> e.getCorreo() != null && s.equalsIgnoreCase(e.getCorreo().trim()));
                boolean usadoPorProf = usuarioService.listarProfesores().stream()
                        .anyMatch(p -> p.getCorreo() != null && s.equalsIgnoreCase(p.getCorreo().trim()));

                if (usadoPorEst || usadoPorProf) {
                    throw new IllegalArgumentException("El correo ya existe en el sistema.");
                }
            }
        }
        return s;
    }

    /** Convierte el texto de temas de interés en una lista. */
    private List<String> parseTemIN(String v) {
        if (v == null || v.isBlank()) return new ArrayList<>();
        return new ArrayList<>(Arrays.asList(v.split("\\s*,\\s*")));
    }

    /** Devuelve el estudiante creado o editado. */
    public Estudiante getResultado() {
        return resultado;
    }
}


