# Escenarios de aceptación

## Autorización

```gherkin
Dado el preaprobado PRA-1001 activo con 1.000.000 COP para USR-10
Cuando proceso REF-001 por 600.000 COP
Entonces el resultado es AUTHORIZED
Y el saldo restante es 400.000 COP
Y la solicitud queda persistida con fecha del servidor
```

## Rechazo sin descuento

```gherkin
Dado el preaprobado PRA-1002 bloqueado con 800.000 COP
Cuando proceso una solicitud por 100.000 COP
Entonces el resultado es REJECTED con una razón comprensible
Y el saldo continúa en 800.000 COP
```

## Idempotencia

```gherkin
Dado que REF-001 ya fue procesada
Cuando reenvío REF-001 con los mismos datos
Entonces recibo el resultado original marcado como repetido
Y el saldo no cambia nuevamente
```

## Conflicto de referencia

```gherkin
Dado que REF-001 ya fue procesada
Cuando reenvío REF-001 con otro valor, cliente o preaprobado
Entonces recibo HTTP 409
Y se conserva la solicitud original
Y el saldo no cambia
```

## Concurrencia

```gherkin
Dado PRA-1001 con 1.000.000 COP
Cuando llegan simultáneamente dos referencias distintas por 700.000 COP cada una
Entonces exactamente una queda AUTHORIZED
Y la otra queda REJECTED
Y el saldo final es 300.000 COP
```

## Consultas e interfaz

```gherkin
Dado que existen solicitudes procesadas
Cuando consulto una referencia o las solicitudes recientes
Entonces veo los datos y resultados persistidos
Y desde Angular puedo procesar otra solicitud y observar el resultado y la tabla actualizada
```

