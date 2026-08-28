# ADR-06 - Rotación de Archivos de Log

## Estado

Aceptado

## Contexto

Los recursos de log pueden crecer indefinidamente. Se necesita un mecanismo
simple de límite de tamaño y rotación sin dependencias externas, integrado en
el módulo de plataforma.

## Decisión

Implementar rotación manual en `LogPLAT`:

* `numRotation == 1`: al superar `maxBytes` el archivo activo se trunca.
* `numRotation > 1` y nombre con patrón `_%g`: al superar `maxBytes` se
  elimina el archivo de mayor índice (`numRotation - 1`), se desplaza el resto
  un índice hacia arriba y el archivo activo (índice `0`) pasa a índice `1`.
* `maxBytes` tiene un mínimo de 256 bytes.
* Sin patrón `_%g` en el nombre, el comportamiento equivale a
  `numRotation == 1`.

## Consecuencias

### Positivas

* Límite de tamaño predecible por archivo y rotación controlada.
* Sin dependencias externas de rotación (logback, log4j, etc.).
* El patrón `_%g` es el estándar de facto (análogo a la rotación de Java
  Logging), familiar para los consumidores.

### Negativas

* Rotación manual sin compresión ni sellado de tiempo en el nombre.
* El truncado (`numRotation == 1`) descarta información de forma
  irreversible.
* Rotación no atómica entre procesos concurrentes sobre el mismo recurso.

