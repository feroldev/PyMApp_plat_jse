# ADR-04 - Formatos de Recursos CCS de Plataforma

## Estado

Aceptado

## Contexto

Los recursos CCS de configuración y log del framework necesitan ser
persistidos en archivos de la plataforma Java SE, pero los consumidores no
deben conocer ni depender de un formato concreto.

## Decisión

Soportar múltiples formatos seleccionables antes de la inicialización del
módulo:

* Configuración (`ConfigFORMAT`): `TEXT` (properties, predeterminado), `XML`
  (properties XML) y `JSON` (objeto plano, una entrada por línea).
* Log (`LogFORMAT`): `CSV` (variante PyMApp, predeterminado) y `JSON`
  (NDJSON).

Los formatos de configuración preservan comentarios, indentación y formato
original al actualizar claves. El formato JSON de configuración se restringe a
un objeto plano con una entrada por línea y valores de texto, rechazando
estructuras no soportadas con `CCSResourceFormatException`.

## Consecuencias

### Positivas

* Los consumidores usan un contrato único (`setConfigKeyPLAT`,
  `getConfigKeyPLAT`, `setLogLinePLAT`) independiente del formato.
* Interoperabilidad con herramientas estándar (properties, XML, JSON).
* Preservación del formato legible por humanos en recursos existentes.

### Negativas

* Mayor superficie de implementación (parsers propios para JSON y XML).
* Restricciones de formato JSON (plano, una entrada por línea, solo valores
  de texto) que pueden sorprender a consumidores acostumbrados a JSON
  genérico.

