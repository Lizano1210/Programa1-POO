# PLANIFICACIÓN DE CLASES
## Sistema de Matrícula y Calificaciones - Programa 1 POO

**Nota:** En los métodos se omitirán getters, setters y toString para brevedad.

---

## MÓDULO 1: GESTIÓN DE USUARIOS Y AUTENTICACIÓN

### Clase: `Usuario` (Clase Abstracta o Padre)
**Descripción:** Clase base que contiene información común a estudiantes y profesores.

**Atributos:**
- `nombre`: String (2-20 caracteres)
- `apellido1`: String (2-20 caracteres)
- `apellido2`: String (2-20 caracteres)
- `id`: String (9+ caracteres, único en el sistema)
- `telefono`: String (8+ caracteres)
- `correo`: String (formato parte1@parte2, único, sin espacios)
- `direccion`: String (5-60 caracteres)
- `fechaRegistro`: LocalDate (automática, fecha del sistema)

**Métodos:**
- `cambiarContraseña()`: Genera nueva contraseña temporal, la envía al correo
- `validarDatos()`: Valida que los atributos cumplan con los requisitos

**⚠️ INVESTIGAR:**
- `LocalDate` de `java.time` para manejar fechas

---

### Clase: `Contraseña`
**Descripción:** Gestiona las contraseñas encriptadas de los usuarios.

**Atributos:**
- `idUsuario`: String (referencia al usuario)
- `contraseña`: String (encriptada, nunca visible en texto plano)
- `esTemp`: boolean (indica si es contraseña temporal)

**Métodos:**
- `verificarContraseña(String ingresada)`: Valida si la contraseña es correcta
- `encriptarContraseña(String)`: Encripta la contraseña
- `generarContraseñaTemporal()`: Genera una contraseña aleatoria de un solo uso

**⚠️ INVESTIGAR:**
- Algoritmo BCrypt o PBKDF2 para encriptación segura
- Librerías como `org.mindrot.jbcrypt` en Maven

---

### Clase: `Estudiante` (Hereda de `Usuario`)
**Descripción:** Información específica de estudiantes.

**Atributos:**
- `orgDL`: String (Organización donde labora, máx 40 caracteres)
- `temIN`: List\<String\> (Temas de interés, 5-30 caracteres cada uno)
- `matriculas`: List\<Matrícula\> (Cursos en los que está inscrito)

**Métodos:**
- `matricularCurso(Grupo)`: Añade una matrícula a su lista
- `obtenerMatriculas()`: Retorna todas sus matrículas

---

### Clase: `Profesor` (Hereda de `Usuario`)
**Descripción:** Información específica de profesores.

**Atributos:**
- `tituOb`: List\<String\> (Títulos obtenidos, 5-40 caracteres cada uno)
- `certEs`: List\<String\> (Certificaciones, 5-40 caracteres cada uno)
- `grupos`: List\<Grupo\> (Grupos que imparte)
- `evaluaciones`: List\<Evaluación\> (Evaluaciones que ha creado)

**Métodos:**
- `asignarGrupo(Grupo)`: Añade un grupo a su lista
- `desasignarGrupo(Grupo)`: Remueve un grupo de su lista
- `crearEvaluación()`: Crea una nueva evaluación
- `obtenerEvaluacionesPorGrupo(Grupo)`: Retorna evaluaciones asociadas a un grupo

---

### Clase: `Administrador` (Hereda de `Usuario`)
**Descripción:** Usuario con permisos para gestionar estudiantes, profesores y cursos.

**Atributos:**
- `nivelAcceso`: int (nivel de permisos administrativos)

**Métodos:**
- `registrarEstudiante(Estudiante)`: Crea un nuevo estudiante
- `modificarEstudiante(Estudiante)`: Modifica datos de estudiante
- `eliminarEstudiante(String idEstudiante)`: Elimina un estudiante
- `registrarProfesor(Profesor)`: Crea un nuevo profesor
- `modificarProfesor(Profesor)`: Modifica datos de profesor
- `eliminarProfesor(String idProfesor)`: Elimina un profesor
- `crearCurso(Curso)`: Crea un nuevo curso
- `modificarCurso(Curso)`: Modifica un curso
- `eliminarCurso(String idCurso)`: Elimina un curso
- `generarReporte()`: Genera reportes del sistema

---

## MÓDULO 2: GESTIÓN DE CURSOS Y GRUPOS

### Clase: `Curso`
**Descripción:** Información de un curso académico.

**Atributos:**
- `id`: String (6 caracteres exactos, único)
- `nombre`: String (5-40 caracteres)
- `descripcion`: String (5-400 caracteres)
- `horas_dia`: int (1-8)
- `modalidad`: Enum (PRESENCIAL, VIRTUAL_SINCRONICO, VIRTUAL_ASINCRONICO, VIRTUAL_HIBRIDO, SEMIPRESENCIAL)
- `min_estudiantes`: int (1-5)
- `max_estudiantes`: int (min_estudiantes a 20)
- `tipo`: Enum (TEORICO, PRACTICO, TALLER, SEMINARIO)
- `calif_minima_aprobar`: int (0-100)
- `grupos`: List\<Grupo\> (Grupos asociados al curso)

**Métodos:**
- `crearGrupo(LocalDate inicio, LocalDate fin)`: Crea un nuevo grupo para este curso
- `obtenerGrupos()`: Retorna todos los grupos
- `validarDatos()`: Valida que los atributos cumplan requisitos

---

### Clase: `Grupo`
**Descripción:** Representa un grupo específico de un curso con fechas definidas.

**Atributos:**
- `curso`: Curso (referencia al curso)
- `idGrupo`: int (>= 1, único por curso)
- `fechaInicio`: LocalDate
- `fechaFinal`: LocalDate
- `profesor`: Profesor (profesor que imparte el grupo)
- `matriculas`: List\<Matrícula\> (Estudiantes matriculados)
- `evaluacionesAsignadas`: List\<EvaluaciónAsignada\> (Evaluaciones del grupo)

**Métodos:**
- `agregarMatricula(Matrícula)`: Añade un estudiante al grupo
- `validarCapacidad()`: Verifica si el grupo está lleno
- `esVigente(LocalDate fecha)`: Valida si el grupo está vigente en esa fecha
- `asignarEvaluación(Evaluación, LocalDateTime inicio)`: Asocia evaluación al grupo

**⚠️ INVESTIGAR:**
- Métodos de comparación de fechas con `LocalDate`

---

## MÓDULO 3: GESTIÓN DE MATRÍCULAS

### Clase: `Matrícula`
**Descripción:** Registra la inscripción de un estudiante en un grupo específico.

**Atributos:**
- `estudiante`: Estudiante (referencia)
- `grupo`: Grupo (referencia)
- `fechaMatricula`: LocalDate (fecha en que se matriculó)
- `calificacionFinal`: double (0-100, calculada al finalizar evaluaciones)
- `intentosEvaluacion`: List\<IntentoEvaluación\> (Evaluaciones realizadas)

**Métodos:**
- `calcularCalificacionFinal()`: Promedia todas las evaluaciones realizadas
- `agregarIntento(IntentoEvaluación)`: Registra un nuevo intento de evaluación
- `obtenerIntentosEvaluacion()`: Retorna todos los intentos

---

## MÓDULO 4: GESTIÓN DE EVALUACIONES

### Clase: `Evaluación`
**Descripción:** Define una evaluación con preguntas de diversos tipos.

**Atributos:**
- `id`: int (generado automáticamente, único en todo el sistema)
- `nombre`: String (5-20 caracteres)
- `instrucciones`: String (5-400 caracteres)
- `objetivos`: List\<String\> (10-40 caracteres cada uno)
- `duracion_minutos`: int (>= 1)
- `preguntasAleatorias`: boolean (mostrar preguntas en orden aleatorio)
- `opcionesAleatorias`: boolean (mostrar opciones en orden aleatorio)
- `preguntas`: List\<IPregunta\> (Todas las preguntas de la evaluación)
- `puntajeTotal`: int (suma de puntos de todas las preguntas)
- `grupos_asociados`: List\<EvaluaciónAsignada\> (Grupos donde se usa)

**Métodos:**
- `agregarPregunta(IPregunta)`: Añade una pregunta de cualquier tipo
- `calcularPuntajeTotal()`: Suma los puntos de todas las preguntas
- `generarOrdenPreguntas()`: Si está activado, aleatoriza el orden
- `validarDatos()`: Verifica que la evaluación sea válida
- `canModificar()`: Retorna false si está asociada a algún grupo vigente
- `canDesasociar()`: Verifica si puede desasociarse (fecha de inicio > fecha/hora actual)

**⚠️ INVESTIGAR:**
- Cómo implementar `List\<IPregunta\>` con polimorfismo
- Manejo de `LocalDateTime` para hora exacta

---

### Interfaz/Clase Abstracta: `IPregunta`
**Descripción:** Contrato base para todos los tipos de preguntas.

**Métodos abstractos:**
- `obtenerPuntos()`: int
- `obtenerDescripcion()`: String
- `getTipo()`: Enum
- `validarDatos()`: boolean
- `calificar(RespuestaEstudiante)`: int (retorna puntos obtenidos)

---

### Clase: `Pregunta` (Implementa `IPregunta`)
**Descripción:** Pregunta de selección única, múltiple o verdadero/falso.

**Atributos:**
- `id`: int (único dentro de la evaluación)
- `tipo`: Enum (SELECCION_UNICA, SELECCION_MULTIPLE, VERDADERO_FALSO)
- `descripcion`: String (la pregunta en sí)
- `puntos`: int (>= 1)
- `respuestas`: List\<Respuesta\> (opciones de respuesta)

**Métodos:**
- `agregarRespuesta(Respuesta)`: Añade una opción de respuesta
- `obtenerRespuestasCorrectas()`: Retorna todas las respuestas marcadas como correctas
- `calificar(RespuestaEstudiante)`: Valida la respuesta del estudiante
  - Si es SELECCION_UNICA: solo 1 debe ser correcta
  - Si es SELECCION_MULTIPLE: puede haber varias correctas
  - Si es VERDADERO_FALSO: solo tiene 2 opciones
- `validarDatos()`: Verifica requisitos según el tipo

---

### Clase: `Respuesta`
**Descripción:** Opción de respuesta para una pregunta.

**Atributos:**
- `texto`: String (lo que ve el estudiante)
- `esCorrecta`: boolean (si es la respuesta correcta)
- `orden`: int (posición en la lista original)

**Métodos:**
- (Solo getters/setters necesarios)

---

### Clase: `Pareo` (Implementa `IPregunta`)
**Descripción:** Pregunta de tipo emparejamiento entre conceptos y definiciones.

**Atributos:**
- `id`: int (único dentro de la evaluación)
- `descripcion`: String (enunciado general de la pregunta)
- `puntos`: int (>= 1)
- `enunciados`: List\<String\> (columna izquierda)
- `respuestas`: List\<String\> (columna derecha: correctas + distractores)
- `asociaciones`: Map\<Integer, Integer\> (índice enunciado → índice respuesta correcta)

**Métodos:**
- `agregarEnunciado(String)`: Añade un elemento a emparejar
- `agregarRespuesta(String)`: Añade una opción en la columna derecha
- `definirAsociación(int enunciadoIdx, int respuestaIdx)`: Define qué va con qué
- `calificar(RespuestaEstudiante)`: Valida cada emparejamiento
- `validarDatos()`: Verifica que haya enunciados, respuestas y asociaciones
- `generarOrdenRespuestas()`: Aleatoriza el orden de la columna derecha si aplica

**⚠️ INVESTIGAR:**
- Uso de `Map` en Java y cómo usarlo para mapeos

---

### Clase: `SopaDeLetras` (Implementa `IPregunta`)
**Descripción:** Pregunta de sopa de letras. **(IMPLEMENTAR DESPUÉS - PRIORIDAD BAJA)**

**Atributos:**
- `id`: int (único dentro de la evaluación)
- `descripcion`: String (enunciado general)
- `puntos`: int (>= 1)
- `enunciados`: List\<Enunciado\> (palabras a buscar con pistas)
- `cuadricula`: char[][] (la matriz de letras)
- `palabrasEncontradas`: List\<PalabraEncontrada\> (registro de dónde están las palabras)

**Métodos:**
- `agregarEnunciado(String palabra, String pista)`: Añade palabra a buscar
- `generarCuadricula()`: Crea la matriz de letras
- `calificar(RespuestaEstudiante)`: Valida palabras encontradas
- `validarDatos()`: Mínimo 10 enunciados, cubre todos los 8 sentidos

**⚠️ INVESTIGAR:**
- Algoritmo para generar sopa de letras
- Cómo validar que una palabra existe en 8 sentidos (horizontal, vertical, diagonales)
- Estructura de datos para almacenar ubicación de palabras

---

## MÓDULO 5: GESTIÓN DE RESPUESTAS Y CALIFICACIONES

### Clase: `EvaluaciónAsignada`
**Descripción:** Asocia una evaluación a un grupo con fecha/hora de inicio.

**Atributos:**
- `evaluacion`: Evaluación (referencia)
- `grupo`: Grupo (referencia)
- `fechaHoraInicio`: LocalDateTime
- `fechaHoraFinal`: LocalDateTime (se calcula según duración)

**Métodos:**
- `calcularFechaHoraFinal()`: Suma la duración a la fecha de inicio
- `esActiva()`: Verifica si el estudiante puede hacer la evaluación ahora
- `canDesasociar()`: Retorna true si start > fecha/hora actual

---

### Clase: `IntentoEvaluación`
**Descripción:** Registra un intento específico de un estudiante resolviendo una evaluación.

**Atributos:**
- `estudiante`: Estudiante (referencia)
- `evaluacion`: Evaluación (referencia)
- `grupo`: Grupo (referencia)
- `fechaHoraInicio`: LocalDateTime (cuándo empezó)
- `fechaHoraFinal`: LocalDateTime (cuándo terminó)
- `respuestasEstudiante`: List\<RespuestaEstudiante\> (todas sus respuestas)
- `puntajeObtenido`: int (puntos totales obtenidos)
- `calificacion`: double (puntajeObtenido / puntajeTotal * 100)
- `ordenPreguntasUsado`: List\<Integer\> (orden en que se presentaron, si fue aleatorio)

**Métodos:**
- `agregarRespuesta(RespuestaEstudiante)`: Registra una respuesta
- `calcularCalificacion()`: Suma puntos y calcula porcentaje
- `obtenerTiempoUsado()`: LocalDateTime (diferencia entre inicio y fin)
- `obtenerDetalleRespuesta(int numPregunta)`: Retorna detalle de esa respuesta

---

### Clase: `RespuestaEstudiante`
**Descripción:** Almacena la respuesta específica de un estudiante a una pregunta.

**Atributos:**
- `pregunta`: IPregunta (referencia a la pregunta)
- `respuestaTexto`: String o List\<String\> (según el tipo de pregunta)
- `puntosObtenidos`: int
- `esCorrecta`: boolean

**Métodos:**
- `obtenerRespuestaCorrecta()`: String o List\<String\> (para mostrar después)
- (Principalmente getters)

---

## MÓDULO 6: PERSISTENCIA DE DATOS

### Clase: `GestorArchivos`
**Descripción:** Maneja la serialización/deserialización de todos los datos del sistema.

**Atributos:**
- `rutaBase`: String (carpeta donde se guardan los archivos)

**Métodos:**
- `guardarEstudiantes(List\<Estudiante\>)`: Persiste estudiantes
- `cargarEstudiantes()`: Lee estudiantes desde archivo
- `guardarProfesores(List\<Profesor\>)`: Persiste profesores
- `cargarProfesores()`: Lee profesores desde archivo
- `guardarCursos(List\<Curso\>)`: Persiste cursos
- `cargarCursos()`: Lee cursos desde archivo
- `guardarEvaluaciones(List\<Evaluación\>)`: Persiste evaluaciones
- `cargarEvaluaciones()`: Lee evaluaciones desde archivo
- `guardarIntentos(List\<IntentoEvaluación\>)`: Persiste intentos
- `cargarIntentos()`: Lee intentos desde archivo
- `guardarContraseñas(List\<Contraseña\>)`: Persiste contraseñas
- `cargarContraseñas()`: Lee contraseñas desde archivo

**⚠️ INVESTIGAR:**
- `Serializable` en Java vs `Gson`/`Jackson` para JSON
- Cuál es más adecuado para este proyecto
- Cómo usar `ObjectOutputStream` y `ObjectInputStream`
- Alternativa: guardar en JSON con librerías Maven

---

## RESUMEN DE INVESTIGACIONES PENDIENTES

| Tema | Por qué lo necesitas | Dónde aprenderlo |
|------|----------------------|------------------|
| `java.time` (LocalDate, LocalDateTime) | Manejar fechas y horas | Documentación oficial Oracle |
|-----------------|-----------------------------|-----------------------|
| BCrypt / PBKDF2 | Encriptación de contraseñas | Maven + documentación |
|--------------------|----------------------------|--------------------|
| Serialización Java | Guardar objetos en archivo | Documentación Java |
|----------------|------------------------------|-----------------------|
| Gson o Jackson | Alternativa: guardar en JSON | Maven + documentación |
|--------------------------|--------------------------------------|-----------------------|
| Polimorfismo (IPregunta) | Manejar tipos de preguntas distintos | Revisar conceptos POO |
|-------------|--------------------------------|--------------------------------|
| Map en Java | Emparejamientos en clase Pareo | Documentación Java Collections |
|---------------------|------------------------|--------------------------|
| Swing UI components | Crear interfaz gráfica | Tutorial Swing (después) |

---

## MÓDULO 7: INTERFAZ GRÁFICA (SWING) - PENDIENTE

Será definida después que la lógica de clases esté completa.

**Componentes aproximados necesarios:**
- Pantalla de login (autenticación)
- Panel para estudiante (ver matriculas, hacer evaluaciones, ver desempeño)
- Panel para profesor (CRUD evaluaciones, asociar grupos, ver resultados)
- Panel para administrador (CRUD usuarios, cursos, grupos, reportes)
- Componentes para mostrar evaluaciones con timer