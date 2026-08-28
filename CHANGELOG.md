# Changelog

Todos los cambios relevantes de este proyecto serán documentados en este archivo.

El formato se basa en [Keep a Changelog](https://keepachangelog.com/es-ES/1.1.0/) y este proyecto respeta [Versionado Semántico](https://semver.org/lang/es/).

## [Unreleased]

### Agregado

* Documentación de diseño: formatos de recursos CCS de plataforma (`doc/design/01_architecture/04_resource_formats.md`).
* Documentación de diseño: configuración del módulo y claves propias (`doc/design/01_architecture/05_module_config.md`).
* ADR-04 (formatos de recursos), ADR-05 (escritura atómica), ADR-06 (rotación de logs) y ADR-07 (reintento de inicialización).

### Mejoras

* Documentación de diseño: corregidas las transiciones de ciclo de vida (error de `initialize()` vuelve a `CREATED`; `FAILED` solo por `shutdown()` fallido; `shutdown()` permitido desde `INITIALIZING`).
* Documentación de diseño: corregida la secuencia de inicio y el mapa de dependencias (`LogFormatLine`, `I18n`, `Kernel`, `Paths`).
* Documentación de diseño: incorporado el CCS I18n en arquitectura y componentes.

---

## [1.6.2] - 2025-07-30

### Primera publicación pública

#### Agregado

* Implementación del contrato `ModPLAT` (`dev.ferol.pymapp.base.mod.ModPLAT`) para la plataforma Java SE, con acceso Singleton (`getInstance()` / `getModManager()`) y ciclo de vida completo `CREATED → INITIALIZING → RUNNING → STOPPED/FAILED`.
* CCS Config de plataforma (`ConfigPLAT`) con soporte de lectura y escritura de claves en tres formatos:
  * `TEXT` — texto plano tipo properties compatible con `java.util.Properties`.
  * `XML` — properties XML (DTD de `java.util.Properties`), con preservación de comentarios, elementos `<comment>` e indentación.
  * `JSON` — objeto plano clave-valor con una entrada por línea y preservación del formato original.
  * Escritura atómica mediante archivo temporal con sufijo único (`yyMMddHHmmssSSS`) y reemplazo seguro.
* CCS Log de plataforma (`LogPLAT`) con soporte de registro de líneas de log en dos formatos:
  * `CSV` — variante PyMApp: separador `|` (ASCII 124), sin cabecera y con escape de separador, barra invertida y caracteres de control.
  * `JSON` — NDJSON (Newline Delimited JSON, compatible con JSON Lines).
  * Rotación de archivos de log con patrón `_%g` y límite de tamaño configurable (mínimo 256 bytes).
* CCS I18n del módulo con textos en 10 idiomas (español predeterminado): es, de, en, fr, it, ja, ko, pt, ru, zh.
* API de conveniencia sobre los recursos CCS propios del módulo: `setConfigKey`, `setConfigNewKey`, `getConfigKey`, `setLogLine` (3 sobrecargas) y `getModI18n`.
* Contrato de plataforma para los CCS del framework: `setConfigKeyPLAT`, `getConfigKeyPLAT` y `setLogLinePLAT`.
* Propiedades de configuración previas a la inicialización: `setConfigFormat`/`setConfigExtens` (TEXT, XML, JSON) y `setLogFormat`/`setLogExtens` (CSV, JSON), bloqueadas una vez inicializado el módulo.
* Inicialización propia de los recursos CCS del módulo en `initialize(String)` mediante las claves de configuración `mod.Log.TypeActive.*`, `mod.Log.LevelActive.*`, `mod.Log.Bytes`, `mod.Log.Rotation` y `mod.i18n.resPath`.
* Recursos de configuración de referencia en tres formatos (`res/PyMApp_plat_jse.config`, `.json` y `.xml`).
* Suite de tests JUnit 4 para el administrador del módulo, `ConfigPLAT`, `LogPLAT` y los recursos i18n.
* Documentación de diseño: arquitectura (`doc/design/01_architecture/`), registros de decisiones (ADR) y diagramas PlantUML.

#### Mejoras

* Compatibilidad Java 8+ del módulo.
* Gestión de errores unificada con plantillas `ExcMsg` y propagación de `CCSResourceAccessException`/`CCSResourceFormatException` hacia la capa CCS.
* Registro de logs de auditoría (`LogTYPE.AUDIT`) en el recurso de auditoria general de la aplicación (`Kernel`).

