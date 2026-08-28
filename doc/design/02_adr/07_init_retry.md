# ADR-07 - Reintento de Inicialización

## Estado

Aceptado

## Contexto

La inicialización del módulo (`initialize(String)`) crea los recursos CCS
(`Config`, `Log`, `I18n`) y puede fallar por causas transitorias o de
configuración (recurso inaccesible, formato inválido, kernel no operativo).

## Decisión

Ante un error durante la inicialización, el módulo vuelve al estado `CREATED`
en lugar de `FAILED`, permitiendo un reintento completo de `initialize(...)`.

El estado `FAILED` queda reservado exclusivamente para errores durante la
finalización (`shutdown()`), donde no existe recuperación posible.

## Consecuencias

### Positivas

* Permite corregir la configuración o el entorno y reintentar la
  inicialización sin reiniciar la aplicación.
* `FAILED` conserva un significado inequívoco: el módulo no puede
  recuperarse.

### Negativas

* Un módulo en `CREATED` tras un error puede inducir a pensar que nunca fue
  inicializado.
* La lógica de reintento debe contemplar la posibilidad de ciclos de error
  repetidos.

