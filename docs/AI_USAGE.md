# Registro de uso de inteligencia artificial

## Por qué se utilizó

La IA se usó como acelerador de análisis y desarrollo dentro del tiempo limitado de la prueba. Permitió dedicar más tiempo a validar las reglas críticas -concurrencia, idempotencia y persistencia- sin delegar la responsabilidad técnica: cada resultado incorporado fue revisado y ejecutado localmente.

## Actividades asistidas

- Extracción y organización de RF01-RF07 desde el enunciado.
- Redacción inicial de la especificación, escenarios de aceptación y trazabilidad.
- Comparación de alternativas para concurrencia e idempotencia.
- Generación del esqueleto de código, pruebas, scripts y documentación.
- Creación de un concepto visual para orientar la interfaz Angular.
- Apoyo en diagnóstico de errores observados durante compilación y pruebas.

## Resultados incorporados

- Estructura spec-driven en `specs/001-preapproved-usage/`.
- Organización vertical por funcionalidades en backend y frontend.
- Servicio WebFlux/R2DBC con transacción reactiva y descuento condicional.
- Pruebas unitarias y de integración de autorización, rechazo, repetición, conflicto y concurrencia.
- Pruebas Angular del componente y del contrato HTTP.
- Formulario Angular, resultado y tabla de solicitudes recientes.
- Scripts PowerShell para preparar, ejecutar, detener y validar.

## Cómo se validaron

- Compilación con Java 21 como nivel objetivo.
- Nueve pruebas backend con JUnit, Mockito y Reactor Test.
- Ocho pruebas frontend con Jasmine, Karma y Chrome Headless.
- Build de producción de Angular.
- Smoke test de los endpoints y recorrido del flujo principal en navegador.
- Inspección visual en escritorio y móvil contra el concepto guardado en la spec.
- Revisión del diff y del historial Git antes de la entrega.

## Correcciones y descartes

- Se descartó RabbitMQ porque es opcional y no mejora la completitud obligatoria.
- Se descartaron arquitecturas más complejas (eventos, Docker y base externa) por exceder el alcance.
- Se corrigió el Maven Wrapper generado por Spring Initializr para manejar una ruta `.m2` no enlazada en PowerShell.
- Se corrigió el mapeo de mayúsculas/minúsculas de H2 detectado por las pruebas.
- Se adaptó la estructura inicialmente organizada por capas técnicas a slices verticales y se volvió a ejecutar toda la suite.
- Se corrigió el reinicio local para detectar y cerrar procesos huérfanos que mantenían bloqueado el JAR en Windows.
- El contenido ficticio del concepto visual no se usó como dato real; la UI consume únicamente el backend.

## Protección de información

No se enviaron credenciales, datos bancarios reales, secretos ni información confidencial. Los identificadores y valores usados provienen del enunciado de la prueba. El repositorio no contiene claves ni archivos de entorno con secretos.

## Responsabilidad

La IA propuso y aceleró artefactos; la persona candidata conserva la responsabilidad sobre las decisiones, la validación, las correcciones y la explicación técnica de la solución.
