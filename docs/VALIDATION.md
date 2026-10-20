# Evidencia de validación

Fecha: 19 de octubre de 2026.

## Entorno utilizado

- Java 25, compilando el proyecto para Java 21.
- Node.js 24.14.1.
- Spring Boot 4.1.1, WebFlux y R2DBC.
- Angular 20.3.

## Validaciones automatizadas

| Validación | Resultado |
| --- | --- |
| Pruebas unitarias del backend | 4 ejecutadas, 0 fallos, 0 errores |
| Pruebas de integración del backend | 5 ejecutadas, 0 fallos, 0 errores |
| Pruebas unitarias de Angular | 8 ejecutadas, 0 fallos, Chrome Headless |
| Compilación de producción de Angular | Correcta, paquete inicial de 237,98 kB |
| Arranque mediante `scripts/run.ps1` | Backend y frontend disponibles |
| Flujo real desde la interfaz | Solicitud autorizada y visible en recientes |
| Consola del navegador | Sin errores ni advertencias |
| Vista móvil de 390 px | Sin desbordamiento horizontal de la página |

Las pruebas del backend cubren validación aislada, consulta, repetición idempotente,
conflicto por referencia, autorización, rechazo sin débito y dos solicitudes
concurrentes que compiten por el mismo saldo. Las pruebas Angular cubren el
formulario, carga inicial, resultado, error, sincronización del cliente y rutas HTTP.

## QA funcional y visual

Se comprobó el flujo en resoluciones de 1536 x 1024 y 390 x 844. El navegador
integrado no pudo enlazar la pestaña local después de dos intentos, por lo que la
misma revisión se ejecutó con Playwright contra la aplicación local.

Lista de fidelidad respecto al concepto visual:

1. Encabezado, marca, título y paleta azul se conservan.
2. Formulario y resultado mantienen la composición de dos columnas en escritorio.
3. Jerarquía tipográfica, espacios y tamaños de controles son coherentes.
4. El estado autorizado usa semántica verde y presenta los datos clave.
5. La tabla de recientes conserva bordes, etiquetas de estado y actualización.
6. En móvil los paneles se apilan y la tabla usa desplazamiento interno.

Las diferencias intencionales son el uso de datos reales de la API en lugar de
filas ilustrativas y el estado vacío inicial antes de procesar una solicitud.

## Limitaciones conscientes

- H2 persistente simplifica la ejecución local; una solución productiva debería
  utilizar un motor como PostgreSQL y validar su aislamiento bajo carga real.
- No se agregó RabbitMQ porque el enunciado lo presenta como opcional y el flujo
  solicitado no requiere procesamiento asíncrono adicional.
- La validación cubre el navegador Chromium usado por Playwright; no constituye
  una matriz completa de compatibilidad entre navegadores.
