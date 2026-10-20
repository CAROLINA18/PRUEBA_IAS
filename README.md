# BancoIAS - uso de cupos preaprobados

Solución mínima de la prueba técnica Full Stack con backend reactivo en Spring Boot WebFlux, persistencia R2DBC/H2 y frontend Angular. Cubre RF01-RF07, idempotencia por referencia y consistencia ante solicitudes concurrentes.

## Inicio rápido en Windows

Requisitos soportados:

- Java 21 a 26.
- Node.js 20.19+, 22.12+ o 24.x.
- PowerShell 7 o Windows PowerShell 5.1.

Desde la raíz del repositorio:

```powershell
.\scripts\setup.ps1
.\scripts\run.ps1
```

Abre [http://127.0.0.1:4200](http://127.0.0.1:4200). Para detener ambos procesos:

```powershell
.\scripts\stop.ps1
```

`setup.ps1` verifica Java y Node.js, intenta instalarlos con `winget` si faltan, descarga Maven mediante el wrapper oficial y ejecuta `npm install`. `run.ps1` detiene cualquier instancia anterior de este mismo proyecto, compila el backend, inicia backend y frontend en segundo plano, espera sus health checks y guarda logs/PID temporales en `.run/`. Esto evita que Windows mantenga bloqueado el JAR durante una recompilación.

## Validación automática

```powershell
.\scripts\test.ps1
```

El script ejecuta 9 pruebas backend —4 unitarias y 5 de integración—, 14 pruebas unitarias Angular y el build de producción. Las pruebas cubren:

- autorización y descuento;
- rechazo sin descuento;
- repetición idempotente;
- conflicto de referencia con payload distinto;
- dos solicitudes simultáneas que compiten por el mismo saldo;
- validación del formulario, resultado, errores y sincronización del cliente;
- consulta exacta por referencia y paginación simple de recientes;
- contrato HTTP utilizado por el servicio Angular.

## Flujo de demostración

1. Ejecuta la aplicación y procesa `REF-001`, `PRA-1001`, `USR-10`, `600000`.
2. Confirma `AUTORIZADA` y saldo restante de 400.000 COP.
3. Envía de nuevo exactamente `REF-001`: se recupera el resultado original sin otro descuento.
4. Conserva `REF-001` y cambia el valor: la UI muestra el conflicto controlado.
5. Usa `PRA-1002`: la solicitud queda rechazada porque está bloqueado.
6. Consulta `REF-001` desde el buscador por referencia.
7. Revisa la tabla de solicitudes recientes, navega sus páginas y recarga la aplicación para comprobar persistencia.

## API

| Método | Ruta | Propósito |
|---|---|---|
| POST | `/api/requests` | Procesa una utilización |
| GET | `/api/requests/{requestReference}` | Consulta una referencia |
| GET | `/api/requests/recent` | Lista las últimas 20 |
| GET | `/api/pre-approved` | Lista preaprobados y saldo actual |
| GET | `/actuator/health` | Health check |

Ejemplo:

```powershell
$body = @{
  requestReference = 'REF-001'
  preApprovedId = 'PRA-1001'
  customerId = 'USR-10'
  amount = 600000
} | ConvertTo-Json

Invoke-RestMethod `
  -Method Post `
  -Uri 'http://localhost:8080/api/requests' `
  -ContentType 'application/json' `
  -Body $body
```

El contrato detallado está en [api-contract.md](specs/001-preapproved-usage/api-contract.md).

## Enfoque reactivo y consistencia

El camino HTTP no usa JPA ni JDBC: controlador, servicio, repositorio y transacción operan con `Mono`/`Flux`, Spring Data R2DBC, `DatabaseClient` y `TransactionalOperator`.

El saldo se descuenta mediante un `UPDATE` atómico condicionado a que el preaprobado continúe activo, pertenezca al cliente y tenga saldo. El descuento y el insert de la solicitud comparten una transacción reactiva. `request_reference` tiene restricción única; si dos peticiones con la misma referencia compiten, la transacción perdedora revierte y recupera el resultado original.

## Arquitectura vertical

El código se organiza por capacidad de negocio, de modo que cada cambio funcional quede localizado. Esta decisión encaja con un dominio pequeño de dos capacidades y evita repartir cada requerimiento entre capas técnicas globales:

```text
backend/.../usage/       procesar y consultar solicitudes
backend/.../credit/      consultar y debitar preaprobados
backend/.../shared/      configuración y errores transversales
frontend/src/app/features/preapproved-usage/  flujo completo de la pantalla
```

Cada slice conserva juntos endpoint, caso de uso, modelos, acceso a datos y pruebas que cambian por la misma razón. `shared` no contiene reglas de negocio. Las directrices para extender la solución están en `.kiro/steering/`.

## Por qué no se agregó RabbitMQ

WebFlux y RabbitMQ resuelven problemas distintos. WebFlux/R2DBC mantienen no bloqueante el flujo HTTP y el acceso a datos; RabbitMQ sirve para desacoplar trabajo asíncrono. Esta operación requiere una respuesta inmediata de autorización o rechazo y una transacción consistente sobre el cupo, por lo que una cola agregaría publicación, consumidor, reintentos, DLQ y correlación sin mejorar los RF obligatorios.

RabbitMQ tendría sentido si el resultado pudiera ser diferido, hubiera integraciones lentas, picos que amortiguar o eventos para otros sistemas. Esa evolución debería publicar mediante outbox transaccional. El análisis completo está en [DECISIONS.md](docs/DECISIONS.md).

## Buenas prácticas aplicadas

- contratos y criterios de aceptación definidos antes del código;
- slices verticales cohesionadas y `shared` limitado a asuntos transversales;
- flujo reactivo sin llamadas bloqueantes en el camino HTTP;
- reglas de consistencia respaldadas por la base: transacción, actualización condicional y restricción única;
- validación de entrada y traducción uniforme de errores HTTP;
- datos de negocio del formulario obtenidos de la API, no escritos en el HTML;
- pruebas unitarias, de integración, de concurrencia y build de producción automatizados;
- scripts reproducibles, dependencias fijadas, sin secretos ni datos reales versionados.

## Datos semilla

| Preaprobado | Cliente | Estado | Cupo inicial |
|---|---|---|---:|
| PRA-1001 | USR-10 | ACTIVE | 1.000.000 COP |
| PRA-1002 | USR-10 | BLOCKED | 800.000 COP |
| PRA-2001 | USR-20 | ACTIVE | 2.000.000 COP |

La base queda en `backend/data/` y no se versiona. Los datos semilla no sobrescriben saldos existentes al reiniciar.

## Desarrollo spec-driven

El primer commit contiene la especificación antes del código. Los artefactos están en `specs/001-preapproved-usage/`:

- `spec.md`: historias, reglas, invariantes, supuestos y trazabilidad RF01-RF07.
- `acceptance.md`: escenarios verificables.
- `api-contract.md`: contrato HTTP.
- `plan.md`: arquitectura y estrategia de consistencia.
- `tasks.md`: progreso de implementación y validación.
- `design-concept.png`: referencia visual de la UI.

Las decisiones y sus trade-offs están en [DECISIONS.md](docs/DECISIONS.md). El uso de IA, resultados incorporados, validaciones, correcciones y protección de datos está en [AI_USAGE.md](docs/AI_USAGE.md).

## Estructura

```text
backend/    Slices verticales Spring WebFlux + R2DBC + pruebas
frontend/   Slice vertical Angular standalone
scripts/    setup, ejecución, parada y validación
specs/      especificación y criterios de aceptación
docs/       decisiones y registro de IA
```

## Alcance y limitaciones

- Completado: RF01-RF07, persistencia, pruebas, scripts, decisiones y registro de IA.
- Deliberadamente excluido: RabbitMQ, autenticación, administración de maestros y paginación avanzada.
- H2 en archivo simplifica la evaluación local. En producción se recomienda PostgreSQL R2DBC, migraciones versionadas, observabilidad y pruebas de carga.
- La API corre en una sola zona horaria lógica y expone instantes ISO-8601; la UI los presenta con la zona del navegador.
