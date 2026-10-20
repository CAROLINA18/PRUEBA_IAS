# Steering de producto

## Objetivo

Permitir que un canal procese y consulte usos de cupos preaprobados con una
respuesta clara, persistente e idempotente.

## Reglas invariantes

- Un monto debe ser mayor que cero.
- El preaprobado debe existir, pertenecer al cliente, estar activo y tener saldo.
- Todo intento termina persistido como autorizado o rechazado.
- Un rechazo nunca descuenta saldo.
- Repetir la misma referencia y payload recupera el resultado original.
- Reutilizar una referencia con datos diferentes produce un conflicto controlado.
- La concurrencia nunca puede dejar el saldo negativo.

## Alcance

Mantener la solución mínima. RabbitMQ, autenticación y administración de maestros
solo se incorporan si aparece un requisito explícito que los necesite.
