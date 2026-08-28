# Configuración del Módulo

## Objetivo

Documentar los recursos CCS propios del módulo `PyMApp_plat_jse` y las claves
de configuración que controlan su inicialización.

---

## Recursos CCS Propios

Al ejecutar `initialize(String)` el módulo crea sus recursos CCS:

| Recurso | Clase | Nombre |
| ------- | ----- | ------ |
| Config | `Config` | `PyMApp_plat_jse` (+ extensión del formato configurado) |
| Log | `Log` | `PyMApp_plat_jse` (+ extensión del formato configurado) |
| I18n | `I18n` | Paquete root `dev.ferol.pymapp.plat.jse.i18n` (configurable) |

Se proveen recursos de referencia en tres formatos en `res/`:
`PyMApp_plat_jse.config`, `PyMApp_plat_jse.json` y `PyMApp_plat_jse.xml`.

---

## Claves de Configuración

| Clave | Predeterminado | Descripción |
| ----- | -------------- | ----------- |
| `mod.Log.TypeActive.<TYPE>` | `true` | Habilita/deshabilita cada `LogTYPE` (`SYSTEM`, `APPLICATION`, `EXCEPTION`, `AUDIT`, `DATABASE`, `DEBUG`) para el recurso principal de logs del módulo. |
| `mod.Log.LevelActive.<LEVEL>` | `true` | Habilita/deshabilita cada `LogLEVEL` (`EMERGENCY`, `ALERT`, `CRITICAL`, `ERROR`, `WARNING`, `NOTICE`, `INFORMATIONAL`, `DEBUG`) para el recurso principal de logs del módulo. |
| `mod.Log.Bytes` | `1677722` | Tamaño en bytes del recurso principal de logs del módulo. |
| `mod.Log.Rotation` | `1` | Cantidad de rotaciones del recurso principal de logs del módulo. |
| `mod.i18n.resPath` | `dev.ferol.pymapp.plat.jse.i18n` | Paquete root del CCS I18n del módulo. |

---

## Comportamiento de Lectura

* Si una clave `mod.Log.TypeActive.*` / `mod.Log.LevelActive.*` no existe o su
  lectura falla, el `LogTYPE`/`LogLEVEL` correspondiente se activa por defecto
  (`true`).
* Si `mod.Log.Bytes` o `mod.Log.Rotation` no existen o su lectura/parseo falla,
  se utilizan los valores predeterminados (`1677722` y `1`).
* Si `mod.i18n.resPath` no existe o está vacío, se utiliza el paquete root
  predeterminado del módulo.

---

## API de Conveniencia

Sobre los recursos propios, el módulo expone (además del contrato `ModPLAT`):

* `setConfigKey(String, String)` / `setConfigNewKey(String, String)` —
  escritura de claves en el recurso `Config` del módulo (requieren `RUNNING`).
* `getConfigKey(String)` — lectura de claves (admite `INITIALIZING`).
* `setLogLine(LogTYPE, LogLEVEL, ...)` — 3 sobrecargas; registra en el recurso
  `Log` del módulo, excepto los `AUDIT` que se registran en el recurso de
  auditoria general de la aplicación (`Kernel`).
* `getModI18n()` — acceso al recurso `I18n` del módulo (admite `INITIALIZING`).

