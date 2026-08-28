# ADR-03 - Gestión de Ciclo de Vida

## Estado

Aceptado

## Contexto

Los servicios de plataforma requieren inicialización controlada.

## Decisión

Implementar una máquina de estados simple.

Estados:

* CREATED
* INITIALIZING
* RUNNING
* STOPPED
* FAILED

## Consecuencias

### Positivas

* Comportamiento predecible.
* Detección temprana de errores.
* Control de recursos.

### Negativas

* Lógica adicional de validación de estados.

