# Formatos de Recursos CCS de Plataforma

## Objetivo

Especificar los formatos de recursos que el módulo `PyMApp_plat_jse` soporta
para los CCS de configuración y log, y las reglas de escritura, preservación
de formato, rotación y reemplazo atómico.

---

## Formatos de Configuración (`ConfigFORMAT`)

El formato se selecciona antes de la inicialización mediante
`setConfigFormat(ConfigFORMAT)` (predeterminado `TEXT`). Determina la
implementación utilizada por `setConfigKeyPLAT` y `getConfigKeyPLAT`.

### TEXT

Texto plano tipo `properties` compatible con `java.util.Properties`.

* Pares `clave=valor` con escape completo de clave y valor.
* Preserva comentarios (`#` y `!`), líneas vacías e indentación.
* Respeta las líneas de continuación (barra invertida final no escapada):
  al reemplazar una clave continuada se descartan los fragmentos siguientes.
* Lectura delegada a `java.util.Properties.load(Reader)`.

### XML

Properties XML (DTD de `java.util.Properties`).

* Requiere un recurso no vacío con la estructura raíz obligatoria
  (`<properties>`); un archivo vacío se rechaza con `CCSResourceFormatException`.
* Preserva comentarios XML (`<!-- ... -->`), elementos `<comment>` e
  indentación.
* Soporta entradas `entry` de una línea, auto-cerradas (`<entry .../>`) y
  multi-línea; al actualizar una clave multi-línea se normaliza a una línea.
* Al agregar claves nuevas se reutiliza la indentación de la primera entrada
  existente, o 8 espacios si no hay entradas.
* Lectura delegada a `java.util.Properties.loadFromXML(InputStream)`.

### JSON

Objeto plano clave-valor con una entrada por línea.

* Las llaves de apertura y cierre pueden compartir línea con una entrada y se
  normalizan a líneas propias.
* No se soporta más de una entrada por línea, objetos anidados ni arreglos;
  ante estas estructuras, o ante cadenas sin cerrar, se lanza
  `CCSResourceFormatException`.
* Claves únicas: si la clave existe se actualiza su valor en el lugar; los
  valores no textuales (numéricos, booleanos, `null`) de claves que no se
  actualizan se conservan tal cual.
* Solo se procesan valores de texto; una clave cuyo valor no sea texto se
  reemplaza por texto al actualizarla, y `getJsonKey` devuelve `null` si el
  valor no es texto.
* Se preserva el formato original (indentación, comas finales); al insertar
  una clave nueva se asegura la coma de la entrada previa cuando es necesaria.

---

## Formatos de Log (`LogFORMAT`)

El formato se selecciona antes de la inicialización mediante
`setLogFormat(LogFORMAT)` (predeterminado `CSV`). Determina la implementación
utilizada por `setLogLinePLAT`.

Los campos de la línea de log son formateados por `LogFormatLine` (PyMApp
Base), en el orden:

`Timestamp, InstanceID, logTYPE, logLEVEL, stackData, message, addData,
exceptionMessage, exceptionStack`

### CSV

Variante PyMApp con 3 variaciones respecto del CSV estándar:

1. Separador `|` (ASCII 124) en lugar de coma.
2. Sin nombres de campos en la primera línea (sin cabecera).
3. Los campos de texto libre (`message`, `addData`, `exceptionMessage`,
   `exceptionStack`) escapan el separador (`\|`), la barra invertida (`\\`) y
   los caracteres de control (`\n`, `\r`, `\t`, `\f`).

### JSON (NDJSON)

Newline Delimited JSON, compatible con JSON Lines.

* Cada línea es un objeto JSON de una sola línea con las claves
  `Timestamp`, `InstanceID`, `logTYPE`, `logLEVEL`, `stackData`, `message`,
  `addData`, `exceptionMessage`, `exceptionStack`.
* Los campos de texto libre se codifican con `JSON.escaping`, lo que permite
  saltos de línea dentro de los valores.

---

## Rotación de Archivos de Log

* `numRotation == 1`: al superar `maxBytes` el archivo se trunca.
* `numRotation > 1` y nombre con patrón `_%g`: al superar `maxBytes` se elimina
  el archivo de mayor índice (`numRotation - 1`), se desplaza el resto un
  índice hacia arriba y el archivo activo (índice `0`) pasa a índice `1`.
* `maxBytes` mínimo 256 bytes.
* Si el nombre no contiene `_%g`, el comportamiento es equivalente a
  `numRotation == 1`.

---

## Escritura Atómica de Configuración

La actualización de claves de configuración (TEXT, XML y JSON) nunca modifica
directamente el archivo original:

1. Se escribe un archivo temporal en el mismo directorio, con nombre
   `<archivo>_<yyMMddHHmmssSSS>.tmp`.
2. Se intenta el reemplazo atómico (`Files.move` con `ATOMIC_MOVE`), lo que
   garantiza que una lectura concurrente nunca observe un archivo parcial.
3. Si el sistema de archivos no soporta movimientos atómicos
   (`AtomicMoveNotSupportedException`), se registra la condición como log de
   depuración (si el módulo está `RUNNING`) y se reintenta el reemplazo sin
   la garantía atómica (`REPLACE_EXISTING`).
4. El archivo temporal se elimina en caso de error o si el reemplazo no se
   concretó.

