# Ciclo de Vida del Módulo

## Objetivo

Controlar la inicialización y finalización de los servicios de plataforma.

## Estados

### CREATED

Estado inicial.

El módulo existe pero aún no fue inicializado.

### INITIALIZING

Se están creando e inicializando los servicios internos.

### RUNNING

Estado operativo.

Todos los servicios pueden utilizarse normalmente.

### STOPPED

El módulo fue detenido correctamente.

No se permiten nuevas operaciones.

### FAILED

Ocurrió un error durante la finalización (`shutdown()`).

El módulo queda fuera de servicio, sin posibilidad de recuperación.

## Transiciones

```text
CREATED
   │
     ▼
INITIALIZING
   │
   ├────► RUNNING
   │
   ├────► CREATED    (error en initialize(); permite reintento)
   │
   └────► STOPPED    (shutdown() durante INITIALIZING)
   │
     ▼
RUNNING
   │
   ├────► STOPPED    (shutdown() exitoso)
   │
   └────► FAILED     (error en shutdown())
```

## Reglas

* `initialize(...)` solo puede ejecutarse desde `CREATED`; ante un error durante
  la inicialización el módulo vuelve a `CREATED` y el reintento es permitido.
* Desde `STOPPED` o `FAILED` no se permite reinicialización.
* `shutdown()` puede ejecutarse desde `INITIALIZING` o `RUNNING`; ante un error
  el módulo pasa a `FAILED`.
* La mayoría de los servicios requieren `RUNNING`; excepcionalmente algunos
  (`getConfigKey`, `setLogLine`, `getModI18n`) admiten `INITIALIZING`, lo que
  permite su uso durante el proceso de inicialización del módulo.

## Responsabilidad

La clase `PyMApp_plat_jse` es responsable de administrar estas transiciones.

