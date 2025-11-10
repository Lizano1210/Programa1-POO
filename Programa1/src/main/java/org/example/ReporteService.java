package org.example;

import java.nio.file.Path;
import java.time.LocalDate;

/**
 * Interfaz que define el contrato para generar distintos tipos de reportes
 * administrativos dentro del sistema.
 * <p>
 * Esta interfaz puede ser implementada por clases como {@code ReporteServiceTxt}
 * o {@code ReporteServicePdf}, las cuales se encargan de crear reportes en
 * diferentes formatos (TXT, CSV, PDF, etc.).
 * </p>
 */
public interface ReporteService {

    // -- Tipos de alcance --

    /**
     * Define los tipos de alcance posibles para la generación de reportes:
     * <ul>
     *     <li>{@code TODOS}: incluye todos los cursos y grupos.</li>
     *     <li>{@code CURSO}: restringe el reporte a un curso específico.</li>
     *     <li>{@code GRUPO}: restringe el reporte a un grupo dentro de un curso.</li>
     * </ul>
     */
    enum ScopeType { TODOS, CURSO, GRUPO }

    /**
     * Representa el alcance de un reporte, indicando si es general, por curso
     * o por grupo.
     * <p>
     * Se utiliza para delimitar el conjunto de datos incluidos en cada tipo de
     * reporte generado.
     * </p>
     */
    final class Scope {

        /** Tipo de alcance definido (todos, curso o grupo). */
        public final ScopeType tipo;

        /** Identificador del curso (obligatorio si el tipo es {@code CURSO} o {@code GRUPO}). */
        public final String idCurso;

        /** Identificador del grupo (obligatorio si el tipo es {@code GRUPO}). */
        public final Integer idGrupo;

        // -- Constructor privado --

        private Scope(ScopeType tipo, String idCurso, Integer idGrupo) {
            this.tipo = tipo;
            this.idCurso = idCurso;
            this.idGrupo = idGrupo;
        }

        // -- Métodos de fábrica --

        /** Crea un alcance que incluye todos los cursos y grupos. */
        public static Scope todos() { return new Scope(ScopeType.TODOS, null, null); }

        /** Crea un alcance limitado a un curso específico. */
        public static Scope curso(String idCurso) { return new Scope(ScopeType.CURSO, idCurso, null); }

        /** Crea un alcance limitado a un grupo específico dentro de un curso. */
        public static Scope grupo(String idCurso, int idGrupo) { return new Scope(ScopeType.GRUPO, idCurso, idGrupo); }
    }

    // -- Métodos de generación de reportes --

    /**
     * Genera una lista alfabética de estudiantes según la fecha de vigencia
     * y el alcance indicado.
     * <p>
     * El resultado se almacena en un archivo de texto o CSV y se devuelve su ruta.
     * </p>
     *
     * @param fechaVigencia fecha de corte para considerar estudiantes activos
     * @param scope alcance del reporte (todos, curso o grupo)
     * @return ruta del archivo generado
     * @throws Exception si ocurre un error durante la generación o escritura del archivo
     */
    Path generarListaEstudiantes(LocalDate fechaVigencia, Scope scope) throws Exception;

    /**
     * Genera un reporte estadístico de matrícula según la fecha de vigencia
     * y el alcance indicado.
     * <p>
     * Este reporte resume la cantidad de estudiantes matriculados y otros
     * datos relevantes. El archivo generado puede ser TXT o CSV.
     * </p>
     *
     * @param fechaVigencia fecha de corte de los datos
     * @param scope alcance del reporte (todos, curso o grupo)
     * @return ruta del archivo generado
     * @throws Exception si ocurre un error al procesar la información
     */
    Path generarEstadisticaMatricula(LocalDate fechaVigencia, Scope scope) throws Exception;

    // -- Exportación de intentos --

    /**
     * Exporta un intento de evaluación a un archivo (por ejemplo, PDF).
     * <p>
     * Este método permite registrar o almacenar los resultados de un intento
     * individual de un estudiante, en el formato definido por la implementación.
     * </p>
     *
     * @param intento intento de evaluación a exportar
     * @param destino archivo de destino donde se guardará el reporte
     * @return {@code true} si la exportación fue exitosa, {@code false} en caso contrario
     */
    boolean exportarIntento(IntentoEvaluacion intento, java.io.File destino);
}
