# Steering técnico

## Stack

- Java 21 y Spring Boot WebFlux.
- Reactor (`Mono`/`Flux`) y R2DBC sin JPA/JDBC en el flujo HTTP.
- H2 en archivo para persistencia local reproducible.
- Angular standalone y formularios reactivos.
- PowerShell para instalación, ejecución y validación local.

## Calidad obligatoria

- Una regla aislable debe tener prueba unitaria.
- Persistencia, transacciones y concurrencia se validan con pruebas de integración.
- Componentes y servicios HTTP Angular deben tener pruebas unitarias.
- `scripts/test.ps1` debe ejecutar backend, frontend y build de producción.
- Ningún secreto, credencial o dato real se versiona.

## Persistencia

H2/R2DBC es una decisión de evaluación local. Una migración productiva debe usar
PostgreSQL R2DBC, migraciones versionadas y pruebas de aislamiento/carga.
