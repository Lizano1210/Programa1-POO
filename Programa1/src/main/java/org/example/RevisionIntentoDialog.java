package org.example;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Ventana de diálogo que permite revisar el detalle completo de un intento de evaluación.
 * <p>
 * Muestra la información general del intento (estudiante, evaluación, grupo, fechas y puntaje)
 * junto con el detalle de las preguntas y respuestas registradas.
 * </p>
 * <p>
 * Esta vista es de solo lectura y se utiliza principalmente desde el panel
 * {@link ProfesorIntentosPanel} para permitir al profesor consultar el desempeño
 * de los estudiantes en evaluaciones específicas.
 * </p>
 */
public class RevisionIntentoDialog extends JDialog {

    // -- Atributos principales --

    /** Intento de evaluación a mostrar en el diálogo. */
    private final IntentoEvaluacion intento;

    // -- Constructor --

    /**
     * Crea el diálogo de revisión de intento con los datos del intento de evaluación indicado.
     *
     * @param owner ventana padre del diálogo
     * @param intento intento de evaluación que se revisará
     */
    public RevisionIntentoDialog(Window owner, IntentoEvaluacion intento) {
        super(owner, "Revisión del intento", ModalityType.APPLICATION_MODAL);
        this.intento = intento;

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(700, 500));
        setLocationRelativeTo(owner);

        construirUI();
        pack();
    }

    // -- Construcción de interfaz --

    /** Construye la estructura visual del diálogo con el encabezado y el detalle de preguntas. */
    private void construirUI() {
        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        setContentPane(root);

        // -- Encabezado con información general del intento --
        String alumno = (intento.getEstudiante() == null) ? "-" :
                intento.getEstudiante().getNombre() + " " + intento.getEstudiante().getApellido1();
        String eval = (intento.getEvaluacion() == null) ? "-" : intento.getEvaluacion().getNombre();
        String grupo = (intento.getGrupo() == null) ? "-" : String.valueOf(intento.getGrupo().getIdGrupo());

        JTextArea header = new JTextArea(
                "Estudiante: " + alumno + "\n" +
                        "Evaluación: " + eval + "\n" +
                        "Grupo: " + grupo + "\n" +
                        "Inicio: " + intento.getFechaHoraInicio() + "\n" +
                        "Fin: " + intento.getFechaHoraFinal() + "\n" +
                        "Puntaje: " + intento.getPuntajeObtenido() + " / " +
                        (intento.getEvaluacion() == null ? "-" : intento.getEvaluacion().getPuntajeTotal()) + "\n" +
                        "Calificación: " + String.format("%.2f", intento.getCalificacion())
        );
        header.setEditable(false);
        root.add(header, BorderLayout.NORTH);

        // -- Sección de detalle de preguntas --
        JTextArea detalle = new JTextArea();
        detalle.setEditable(false);

        List<RespuestaEstudiante> resps = intento.getRespuestasEstudiante();
        if (resps != null && intento.getEvaluacion() != null) {
            List<IPregunta> preguntas = intento.getEvaluacion().getPreguntas();
            for (int i = 0; i < preguntas.size(); i++) {
                IPregunta p = preguntas.get(i);
                RespuestaEstudiante re = (i < resps.size()) ? resps.get(i) : null;

                detalle.append("Pregunta " + (i + 1) + ": " + p.obtenerDescripcion() + "\n");
                if (re != null) {
                    detalle.append("  Puntos obtenidos: " + re.getPuntosObtenidos() + "\n");
                    List<Integer> ord = re.getOrdenesSeleccionados();
                    if (ord != null) detalle.append("  Selecciones: " + ord + "\n");
                } else {
                    detalle.append("  (Sin respuesta)\n");
                }
                detalle.append("\n");
            }
        } else {
            detalle.setText("(No hay detalle de respuestas para mostrar)");
        }

        root.add(new JScrollPane(detalle), BorderLayout.CENTER);

        // -- Botón inferior para cerrar el diálogo --
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton cerrar = new JButton("Cerrar");
        cerrar.addActionListener(e -> dispose());
        south.add(cerrar);
        root.add(south, BorderLayout.SOUTH);
    }
}
