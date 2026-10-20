# Steering de estructura vertical

## Backend

Organizar por capacidad que cambia junta:

```text
com.bancoias.preapproved
├── usage/      procesamiento y consulta de solicitudes
├── credit/     consulta y débito del cupo preaprobado
└── shared/     configuración y errores transversales
```

Una slice puede contener controlador, caso de uso, contrato, entidad, repositorio
y pruebas. No crear paquetes globales `controller`, `service`, `domain` o
`repository`. Mover algo a `shared` solo cuando sea usado por más de una slice y
no contenga una regla de negocio específica.

## Frontend

Cada flujo vive bajo `src/app/features/<feature>/` con componente, plantilla,
estilos, modelos, cliente HTTP y pruebas. `app.ts` se limita a componer features.

## Checklist para una nueva funcionalidad

1. Actualizar especificación y criterios de aceptación.
2. Crear o extender una slice vertical.
3. Agregar pruebas unitarias cerca del código.
4. Agregar integración cuando intervengan persistencia o transacciones.
5. Ejecutar `scripts/test.ps1` y actualizar la evidencia.
