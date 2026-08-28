# ADR-05 - Escritura Atómica de Recursos de Configuración

## Estado

Aceptado

## Contexto

La actualización de claves en recursos de configuración (TEXT, XML y JSON)
requiere reescribir el archivo completo (lectura + escritura de todas las
líneas). Si el proceso se interrumpe o falla a mitad de camino, el recurso
queda truncado o corrupto, y una lectura concurrente puede observar un estado
inconsistente.

## Decisión

Escribir siempre en un archivo temporal ubicado en el mismo directorio del
recurso, con sufijo único basado en fecha/hora (`yyMMddHHmmssSSS`), y recién
luego reemplazar el original:

1. Se intenta el reemplazo atómico (`Files.move` con `ATOMIC_MOVE`).
2. Ante `AtomicMoveNotSupportedException` se registra la condición como log de
   depuración (si el módulo está `RUNNING`) y se reintenta con
   `REPLACE_EXISTING` sin garantía atómica.
3. En cualquier error se elimina el archivo temporal.

## Consecuencias

### Positivas

* Lecturas concurrentes nunca observan un archivo parcial o inconsistente
  (cuando el sistema de archivos soporta `ATOMIC_MOVE`).
* El recurso original permanece intacto ante fallos durante la escritura.

### Negativas

* Requiere espacio adicional para el archivo temporal.
* El reemplazo sin soporte atómico (sistemas de archivos antiguos o
  no locales) mantiene una ventana de inconsistencia.
* Los archivos temporales residuales deben limpiarse explícitamente ante
  fallos inesperados.

