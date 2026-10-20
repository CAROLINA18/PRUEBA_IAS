# Decisiones técnicas

## 1. Persistencia local con H2 y R2DBC

**Contexto.** Las solicitudes deben persistirse, pero el alcance estimado es de dos horas y no se exige una base externa.

**Alternativas.** Memoria no cumple persistencia; PostgreSQL mejora la semejanza con producción, pero agrega instalación o Docker; H2 JDBC rompería el flujo no bloqueante.

**Decisión.** H2 en archivo con R2DBC. Los datos semilla se insertan solo si no existen y las solicitudes sobreviven reinicios.

**Trade-off.** H2 no reemplaza una validación productiva sobre PostgreSQL. La capa SQL está aislada para facilitar ese cambio.

**Validación.** Pruebas de integración levantan el contexto R2DBC y ejecutan lecturas, actualizaciones y transacciones reales.

## 2. Descuento condicional atómico

**Contexto.** Dos solicitudes concurrentes podrían leer el mismo saldo y sobregirar el cupo.

**Alternativas.** `synchronized` solo protege una instancia; bloqueo pesimista es válido, pero hace el flujo más dependiente del motor; versionado optimista exige reintentos.

**Decisión.** Un único `UPDATE` descuenta solo cuando estado, cliente y saldo siguen siendo válidos. El descuento y el registro se ejecutan con `TransactionalOperator`.

**Trade-off.** La razón después de perder la carrera es general (`saldo insuficiente`), suficiente para el enunciado.

**Validación.** La prueba concurrente envía dos usos de 700.000 sobre un saldo de 1.000.000 y comprueba una autorización, un rechazo y saldo final de 300.000.

## 3. Idempotencia mediante restricción única

**Contexto.** Un canal puede repetir la misma referencia, incluso de forma simultánea.

**Alternativas.** Una caché en memoria no funciona entre instancias; consultar antes de insertar no elimina por sí sola la carrera.

**Decisión.** `request_reference` es única. Se consulta primero para el camino común y la base resuelve la carrera. Si el insert pierde, la transacción revierte el descuento y el servicio recupera el original.

**Trade-off.** Una referencia reutilizada con otro payload responde 409; no se sobreescribe la evidencia original.

**Validación.** Pruebas separadas cubren repetición idéntica y conflicto.

## 4. WebFlux de extremo a extremo

**Contexto.** El backend debe usar Spring Boot WebFlux.

**Decisión.** Controladores `Mono`/`Flux`, repositorios R2DBC, `DatabaseClient` y transacciones reactivas. No se incluye JPA, JDBC ni llamadas bloqueantes en el procesamiento HTTP.

**Trade-off.** H2 local sirve para demostrar el comportamiento; una solución productiva debe dimensionar el pool y observar backpressure/latencia con el motor elegido.

**Validación.** Reactor Test suscribe y verifica los publishers sin convertir el servicio a una API imperativa.

## 5. Alcance deliberadamente mínimo

Se excluyen RabbitMQ, autenticación, administración de maestros, paginación avanzada, Docker y patrones distribuidos. Todos están fuera del criterio obligatorio o agregarían una dependencia innecesaria. La estructura conserva separación entre API, aplicación y persistencia sin crear capas ceremoniales.

