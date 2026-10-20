# Especificación: uso de cupo preaprobado

**Estado:** Aprobada para implementación  
**Fecha:** 2026-10-19  
**Fuente:** prueba técnica BancoIAS  
**Alcance:** RF01-RF07; RabbitMQ queda explícitamente fuera por ser opcional.

## Problema

BancoIAS necesita procesar usos de cupos preaprobados sin autorizar más dinero del disponible, incluso cuando recibe solicitudes repetidas o concurrentes. Cada intento procesado debe quedar persistido y consultable.

## Historias de usuario

### HU-01 - Procesar una solicitud (P1)

Como operador, quiero enviar referencia, preaprobado, cliente y valor para conocer si la utilización fue autorizada o rechazada.

**Criterios de aceptación**

1. El sistema registra fecha y hora del procesamiento.
2. Autoriza únicamente cuando el valor es positivo, el preaprobado existe, pertenece al cliente, está activo y tiene saldo suficiente.
3. Una autorización descuenta exactamente una vez el valor solicitado.
4. Un rechazo conserva una razón general y no modifica el saldo.

### HU-02 - Reintentar con seguridad (P1)

Como canal consumidor, quiero reenviar una referencia sin duplicar el uso para poder recuperarme de errores de red.

**Criterios de aceptación**

1. La misma referencia y los mismos datos devuelven el resultado original.
2. La misma referencia con datos diferentes devuelve un conflicto controlado.
3. Ningún reintento vuelve a descontar saldo.

### HU-03 - Preservar saldo bajo concurrencia (P1)

Como dueño del producto, quiero que solicitudes simultáneas no autoricen un total superior al saldo disponible.

**Criterios de aceptación**

1. El descuento es atómico y condicionado a saldo suficiente.
2. El registro de la solicitud y el descuento ocurren en la misma transacción.
3. Si dos solicitudes compiten por saldo insuficiente para ambas, como máximo una queda autorizada.

### HU-04 - Consultar resultados (P2)

Como operador, quiero consultar una solicitud por referencia y ver las solicitudes recientes.

**Criterios de aceptación**

1. La consulta por referencia devuelve el resultado persistido o 404.
2. La consulta reciente devuelve hasta 20 solicitudes, de más nueva a más antigua.
3. Angular expone la consulta exacta por referencia y pagina localmente los resultados recientes en grupos de cinco.

### HU-05 - Operar desde Angular (P2)

Como operador, quiero procesar y revisar solicitudes desde una interfaz web integrada con el backend.

**Criterios de aceptación**

1. El formulario permite informar los cuatro campos obligatorios.
2. El resultado autorizado o rechazado es visible y comprensible.
3. La tabla de recientes se actualiza después de procesar y mediante una acción manual.
4. La interfaz funciona en escritorio y se adapta a una pantalla móvil.
5. La interfaz permite consultar una referencia persistida y navegar las páginas de resultados recientes.

## Requisitos funcionales trazables

| Origen | Requisito verificable | Evidencia prevista |
|---|---|---|
| RF01 | Procesar y fechar solicitudes | `POST /api/requests` + prueba de integración |
| RF02 | Aplicar cinco reglas de autorización | Pruebas de reglas de negocio |
| RF03 | Persistir resultado y razón sin afectar saldo al rechazar | H2 en archivo + pruebas |
| RF04 | No sobregirar con solicitudes concurrentes | Actualización SQL atómica + prueba concurrente |
| RF05 | Idempotencia y conflicto por referencia | Restricción única + pruebas de repetición/conflicto |
| RF06 | Consultar por referencia y recientes | Dos endpoints GET |
| RF07 | Formulario, resultado y recientes en Angular | Recorrido funcional en navegador |

## Entidades y reglas

### Preaprobado

- `id`: identificador de negocio.
- `customerId`: propietario.
- `status`: `ACTIVE` o `BLOCKED`.
- `availableAmount`: saldo COP no negativo.

### Solicitud de uso

- `requestReference`: identificador idempotente único.
- `preApprovedId`, `customerId`, `amount`: datos originales.
- `status`: `AUTHORIZED` o `REJECTED`.
- `rejectionReason`: texto general cuando se rechaza.
- `processedAt`: instante asignado por el servidor.
- `remainingAmount`: saldo observado después del procesamiento, cuando aplica.

### Invariantes

1. `availableAmount >= 0`.
2. Una `requestReference` representa un único conjunto de datos y resultado.
3. Solo `AUTHORIZED` disminuye saldo.
4. El total autorizado nunca supera el cupo disponible inicial.

## Supuestos explícitos

- Moneda única: COP; se usan valores decimales de precisión monetaria.
- La UI inicia con `USR-10`, pero permite seleccionar los datos semilla.
- No hay autenticación porque el enunciado la excluye.
- Persistencia local H2 en archivo para reducir requisitos de infraestructura.
- No se implementan paginación avanzada, administración de preaprobados ni RabbitMQ.

## Criterio de terminado

La entrega está terminada cuando compilan backend y frontend, pasan las pruebas automatizadas, se verifica el flujo principal en navegador y README explica instalación, ejecución, decisiones, uso de IA y limitaciones.
