# Plan de implementación

## Contexto técnico

- Backend: Java 21, Spring Boot 4.1.1, WebFlux, Spring Data R2DBC.
- Persistencia: H2 en archivo inicializada con SQL idempotente.
- Frontend: Angular 20.3, componentes standalone y formularios reactivos.
- Automatización local: PowerShell para preparar, ejecutar, detener y validar.

## Arquitectura mínima

```text
Angular (4200)
    | HTTP JSON
Spring WebFlux (8080)
    | servicio transaccional reactivo
R2DBC / H2 archivo
```

El backend separa API, aplicación y persistencia sin introducir módulos o patrones adicionales que no aporten al alcance de dos horas.

## Estrategia de consistencia

1. Buscar `requestReference`; si existe, comparar el payload.
2. Validar reglas que no modifican estado.
3. Ejecutar un `UPDATE` atómico: descontar solo si el preaprobado sigue activo, pertenece al cliente y conserva saldo suficiente.
4. Insertar el resultado en la misma transacción reactiva.
5. Aplicar restricción única a `requestReference`. Si dos transacciones compiten por la misma referencia, la perdedora revierte y recupera el resultado ya persistido.

Esta estrategia evita un coordinador en memoria y sigue siendo correcta con varias instancias que compartan la misma base relacional.

## Contrato y errores

- Solicitudes procesables, incluso rechazadas por negocio: HTTP 200 con `AUTHORIZED` o `REJECTED`.
- Referencia repetida con payload distinto: HTTP 409.
- Referencia inexistente en consulta: HTTP 404.
- Payload técnicamente inválido, por ejemplo sin referencia: HTTP 400.

## Verificación

- Pruebas de integración de autorización, rechazo, idempotencia, conflicto y concurrencia.
- Compilación de producción de Angular.
- Smoke test del API y recorrido navegador: cargar -> procesar -> mostrar resultado -> actualizar recientes.
- Inspección visual en escritorio y móvil contra `design-concept.png`.

## Riesgos y mitigaciones

| Riesgo | Mitigación |
|---|---|
| Carrera al descontar saldo | `UPDATE` condicional atómico dentro de transacción |
| Doble descuento por reintento | índice único + reversión transaccional + recuperación del original |
| Dependencias locales | scripts y Maven Wrapper; versiones fijadas |
| H2 no representa producción | limitación documentada; SQL y transacciones son migrables a PostgreSQL |

