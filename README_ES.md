# PyMApp PLAT.mod - Java SE

[![Licencia: MPL 2.0](https://img.shields.io/badge/License-MPL_2.0-blue.svg)](https://mozilla.org/MPL/2.0/)
![Versión](https://img.shields.io/badge/Version-1.6.2-blue)
![Java](https://img.shields.io/badge/Java-8%2B-blue)

Módulo de plataforma del [PyMApp Framework](https://github.com/feroldev/PyMApp_Framework) que implementa la integración y ejecución sobre el entorno Java SE. Provee las adaptaciones de plataforma de los servicios transversales (CCS) —configuración, logging e internacionalización— al resto de los módulos del framework sin que estos necesiten conocer la plataforma específica en la que se están ejecutando.

---

## Características

* **Abstracción de plataforma para Java SE:** implementa el contrato `ModPLAT` (`dev.ferol.pymapp.base.mod.ModPLAT`) exponiendo la I/O de plataforma para los CCS `Config` y `Log` de `PyMApp_base`.
* **CCS Config (configuración):** lectura y escritura de recursos `clave=valor` en tres formatos:
  * `TEXT` — texto plano tipo properties, compatible con `java.util.Properties`.
  * `XML` — properties XML (DTD de `java.util.Properties`), preservando comentarios y elementos `<comment>`.
  * `JSON` — objeto plano clave-valor, una entrada por línea, preservando el formato original.
  * Escritura atómica mediante archivo temporal con sufijo único (`yyMMddHHmmssSSS`) y reemplazo seguro.
* **CCS Log (logging):** registro de líneas de log en dos formatos de salida:
  * `CSV` — variante PyMApp: separador `|` (ASCII 124), sin línea de cabecera, con escape del separador, barra invertida y caracteres de control.
  * `JSON` — NDJSON (Newline Delimited JSON, compatible con JSON Lines).
  * Rotación de archivos configurable (patrón `_%g`) y límite de tamaño (mínimo 256 bytes).
* **Ciclo de vida completo del módulo:** acceso Singleton, estados `CREATED → INITIALIZING → RUNNING → STOPPED/FAILED`, consistente con `ModState` de `PyMApp_base`.
* **Recursos CCS propios del módulo:** recurso `Config`, `Log` e `I18n` propios, inicializados por `initialize(String)` y configurables mediante las claves de configuración del módulo.
* **CCS I18n (internacionalización):** textos del módulo en 10 idiomas (español predeterminado): es, de, en, fr, it, ja, ko, pt, ru, zh.
* **API de conveniencia:** `setConfigKey`, `setConfigNewKey`, `getConfigKey`, `setLogLine` (3 sobrecargas) y `getModI18n` para los recursos propios del módulo.
* **Compatibilidad Java 8+:** sin APIs que comprometan la portabilidad con el núcleo del framework (`PyMApp_base`).

---

## Requisitos

* **Java SE 8 o superior** (compilado con compliance/source/target 1.8 en Eclipse).
* **Módulo PyMApp_base** (dev.ferol.pymapp.base) — obligatorio.
* **Módulo PyMApp_util** (dev.ferol.pymapp.util.format) — obligatorio.
* **Eclipse IDE** con JUnit 4 (solo para ejecutar la suite de tests en `src/test/java`).

---

## Ejemplo Básico

El módulo se configura antes de la inicialización del kernel y es el propio kernel quien lo inicializa internamente:

```java
import dev.ferol.pymapp.base.kernel.Kernel;
import dev.ferol.pymapp.base.ccs.log.LogTYPE;
import dev.ferol.pymapp.base.ccs.log.LogLEVEL;
import dev.ferol.pymapp.plat.jse.PyMApp_plat_jse;

public class Main {
    public static void main(String[] args) {
        // 1. Singleton del módulo de plataforma
        PyMApp_plat_jse plat = PyMApp_plat_jse.getInstance();

        // 2. Configurar formatos ANTES de inicializar el kernel (módulo en estado CREATED)
        plat.setConfigFormat(PyMApp_plat_jse.ConfigFORMAT.TEXT); // TEXT | XML | JSON
        plat.setConfigExtens(".config");
        plat.setLogFormat(PyMApp_plat_jse.LogFORMAT.CSV); // CSV | JSON
        plat.setLogExtens(".log");

        // 3. Registrar el módulo en el kernel (inicializa el PLAT internamente)
        Kernel kernel = Kernel.getInstance();
        kernel.setAppModPLAT(plat);
        kernel.initialize(args);
    }
}
```

> **Nota:** los setters de formato lanzan `ModManagerIllegalStateException` si el módulo ya no se encuentra en estado `CREATED`. Deben llamarse antes de `Kernel.initialize(String)`.

---

## Ejemplo Completo

Con el kernel en ejecución, el módulo expone sus recursos CCS propios y el contrato de plataforma:

```java
// --- API de conveniencia (recursos propios del módulo) ---

// Escritura / lectura de claves del recurso de configuración del módulo
plat.setConfigNewKey("mod.Log.Bytes", "1677722");
String bytes = plat.getConfigKey("mod.Log.Bytes");

// Registro de una línea de log en el recurso principal de logs del módulo
// (los AUDIT se registran en el recurso de auditoria general de la aplicación)
plat.setLogLine(LogTYPE.APPLICATION, LogLEVEL.INFORMATIONAL, "Módulo operativo.");

// Metadatos del módulo (funcionan mediante el CCS I18n)
String displayName = plat.getModDISPLAYNAME();
String description = plat.getModDESCRIPTION();

// --- Contrato PLAT (utilizado por los CCS de PyMApp_base) ---

// Escritura / lectura de cualquier recurso de configuración en el formato de la plataforma
plat.setConfigKeyPLAT("/ruta/a/config", "MiApp", "app.SingleUserMode", "true", true);
String valor = plat.getConfigKeyPLAT("/ruta/a/config", "MiApp", "app.SingleUserMode");

// Escritura de cualquier recurso de log con límite de tamaño y rotación
plat.setLogLinePLAT(
    "/ruta/a/log", "MiApp", // ruta y nombre del recurso
    LogTYPE.APPLICATION, LogLEVEL.INFORMATIONAL,
    1677722, 7, // maxBytes, numRotation (rotación con nombre "MiApp_%g.log")
    "Aplicación iniciada.", null, null, 0
);

// --- Ciclo de vida ---
plat.getState();   // ModState.RUNNING
plat.shutdown();   // ModState.STOPPED (o FAILED ante error)
```

---

## Configuración del Módulo

El módulo lee su propio recurso `Config` (`PyMApp_plat_jse` + extensión del formato configurado) al ejecutar `initialize(String)`. Claves:

| Clave | Predeterminado | Descripción |
| ----- | -------------- | ----------- |
| `mod.Log.TypeActive.<TYPE>` | `true` | Habilita/deshabilita cada `LogTYPE` (`SYSTEM`, `APPLICATION`, `EXCEPTION`, `AUDIT`, `DATABASE`, `DEBUG`). |
| `mod.Log.LevelActive.<LEVEL>` | `true` | Habilita/deshabilita cada `LogLEVEL` (`EMERGENCY`, `ALERT`, `CRITICAL`, `ERROR`, `WARNING`, `NOTICE`, `INFORMATIONAL`, `DEBUG`). |
| `mod.Log.Bytes` | `1677722` | Tamaño en bytes del recurso principal de logs del módulo. |
| `mod.Log.Rotation` | `1` | Cantidad de rotaciones del recurso principal de logs del módulo. |
| `mod.i18n.resPath` | `dev.ferol.pymapp.plat.jse.i18n` | Paquete root del CCS I18n del módulo. |

Se proveen recursos de referencia en tres formatos en `res/`: `PyMApp_plat_jse.config`, `PyMApp_plat_jse.json` y `PyMApp_plat_jse.xml`.

---

## Funcionamiento

* **Arquitectura:** arquitectura de plugin con configuración estática. El módulo de plataforma es el único que conoce los detalles de la plataforma Java SE; el resto de los módulos consume las adaptaciones mediante el contrato `ModPLAT`.
* **Dirección de la dependencia:** los CCS de `PyMApp_base` (`Config`, `Log`) delegan toda la I/O de recursos en los métodos `ModPLAT` (`setConfigKeyPLAT`, `getConfigKeyPLAT`, `setLogLinePLAT`); el módulo de plataforma delega la I/O concreta en las clases de utilidad `ConfigPLAT` y `LogPLAT`, y el formateo del texto de la línea de log en `LogFormatLine` de `PyMApp_base`.
* **Filtrado de logs:** la plataforma no decide qué se loguea; `Log` filtra por `LogTypeStatus`/`LogLevelStatus` antes de invocar `setLogLinePLAT`. El `setLogLine` propio del módulo aplica la misma regla con sus propios status. Los logs `AUDIT` se registran en el recurso de auditoria general de la aplicación.
* **Gestión de errores:** los errores de plataforma se propagan como `CCSResourceAccessException`/`CCSResourceFormatException` con plantillas `ExcMsg`; la capa CCS registra el log del error en el recurso maestro.

---

## Objetivos de Diseño

* **Desacoplamiento:** separación entre la lógica de negocio (Java) y los detalles de la plataforma; ningún otro módulo conoce la plataforma concreta.
* **Portabilidad:** la lógica funcional del framework no conoce detalles de Java SE; el módulo mantiene compatibilidad Java 8.
* **Punto único de acceso:** el módulo se expone como Singleton (`getInstance()` / `getModManager()`).
* **Recursos centralizados:** configuración, log e i18n del módulo gestionados por los CCS del framework.
* **Preservación de formato:** los recursos de configuración conservan comentarios y formato al actualizar claves.

---

## Limitaciones

* El formato `JSON` de configuración soporta un objeto plano clave-valor con una entrada por línea; objetos anidados, arreglos y más de una entrada por línea se rechazan con `CCSResourceFormatException`.
* El formato `JSON` de configuración solo procesa valores de texto; los valores no textuales de una clave existente se reemplazan al actualizar la clave.
* El formato `XML` de configuración requiere un recurso no vacío con la estructura raíz obligatoria.
* Solo plataforma Java SE; otras plataformas (p. ej. Android) son cubiertas por sus propios módulos PLAT del framework.

---

## Documentación

* `doc/design/01_architecture/` — documentos de arquitectura.
* `doc/design/02_adr/` — registros de decisiones de arquitectura (ADR).
* `doc/design/03_diagrams/` — diagramas PlantUML.

---

## Estructura del Proyecto

```
src/main/java/dev/ferol/pymapp/plat/jse/
├── PyMApp_plat_jse.java          # Administrador del módulo (Singleton, contrato ModPLAT)
├── ccs/
│   ├── ConfigPLAT.java           # I/O de plataforma: recursos de configuración (TEXT, XML, JSON)
│   └── LogPLAT.java              # I/O de plataforma: recursos de log (CSV, NDJSON) + rotación
└── i18n/text/
    ├── Text.java                 # Textos predeterminados (español)
    └── Text_<ISO>.java           # de, en, es, fr, it, ja, ko, pt, ru, zh
src/test/java/                    # Suite de tests JUnit 4
res/                              # Recursos del módulo: config (3 formatos)
doc/design/                       # Arquitectura, ADR y diagramas
```

---

## Información del Proyecto

Proyecto: PyMApp PLAT.mod - Java SE

Autor: Fernando R. Olmedo {ferol.dev}

Repositorio: https://github.com/feroldev/PyMApp_plat_jse

Versión: 1.6.2

Licencia: Mozilla Public License Version 2.0.

---

## Licencia

Copyright (c) 1999-2025 OLMEDO Fernando R. {ferol.dev}

Licenciado bajo Mozilla Public License Version 2.0. Puede obtener una copia de la licencia en https://mozilla.org/MPL/2.0/.

