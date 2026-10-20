# Contrato HTTP

Base local: `http://localhost:8080/api`

## POST `/requests`

Solicitud:

```json
{
  "requestReference": "REF-001",
  "preApprovedId": "PRA-1001",
  "customerId": "USR-10",
  "amount": 600000
}
```

Respuesta procesada (HTTP 200):

```json
{
  "requestReference": "REF-001",
  "preApprovedId": "PRA-1001",
  "customerId": "USR-10",
  "amount": 600000,
  "status": "AUTHORIZED",
  "rejectionReason": null,
  "processedAt": "2026-10-19T21:30:00Z",
  "remainingAmount": 400000,
  "replayed": false
}
```

Una repetición idéntica devuelve el mismo cuerpo con `replayed: true`. Una repetición incompatible devuelve HTTP 409 con un objeto `code`, `message` y `timestamp`.

## GET `/requests/{requestReference}`

Devuelve HTTP 200 con el resultado persistido o HTTP 404.

## GET `/requests/recent`

Devuelve un arreglo de máximo 20 resultados ordenados por `processedAt` descendente.
La API mantiene este contrato mínimo sin parámetros `page`/`size`; Angular presenta
el arreglo en páginas de cinco elementos. Esto evita una paginación avanzada que
el enunciado no exige y conserva una sola consulta reactiva acotada.

## GET `/pre-approved`

Devuelve los preaprobados semilla y su saldo actual para poblar el formulario y facilitar la demostración.
