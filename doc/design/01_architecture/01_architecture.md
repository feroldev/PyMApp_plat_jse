# Arquitectura de PyMApp PLAT - Java SE

## Introducción

PyMApp PLAT - Java SE es la implementación de referencia de la capa de plataforma del framework PyMApp para entornos Java Standard Edition.

Su responsabilidad es proporcionar servicios básicos de infraestructura a los módulos de la aplicación manteniendo el desacoplamiento respecto de la plataforma subyacente.

---

## Objetivos

La plataforma debe proporcionar:

* Gestión centralizada de configuración.
* Servicios de logging.
* Servicios de internacionalización (i18n).
* Gestión del ciclo de vida.
* Punto único de acceso para los consumidores.

---

## Arquitectura General

```text
+------------------------+
|  Application Modules   |
+-----------+------------+
            |
            v
+------------------------+
|      ModPLAT API       |
+-----------+------------+
            ^
            |
+------------------------+
|    PyMApp_plat_jse     |
+-----------+------------+
            |
            +--------------------+
            |                    |
            v                    v
+----------------+     +----------------+
|   ConfigPLAT   |     |    LogPLAT     |
+----------------+     +----------------+
```

---

## Componentes Principales

### PyMApp_plat_jse

Componente principal del módulo.

Responsabilidades:

* Inicialización.
* Registro de servicios.
* Gestión del ciclo de vida.
* Exposición de la API pública.

### ConfigPLAT

Gestiona parámetros de configuración y propiedades de la aplicación.

### LogPLAT

Centraliza el acceso a mecanismos de logging.

### I18n del Módulo

Provee los recursos de internacionalización del módulo (`MOD_DISPLAYNAME`,
`MOD_DESCRIPTION` y textos propios) mediante el CCS `I18n` de PyMApp Base,
con textos en 10 idiomas (español predeterminado).

---

## Principios de Diseño

### Separación de Responsabilidades

Cada servicio posee una única responsabilidad claramente definida.

### Inversión de Dependencias

Los módulos consumidores dependen de contratos y no de implementaciones concretas.

### Portabilidad

La lógica funcional no conoce detalles específicos de Java SE.

### Centralización

La plataforma se expone mediante un único punto de entrada.

---

## Ciclo de Vida

El módulo sigue el siguiente flujo:

```text
CREATED
   |
   v
INITIALIZING
   |
   v
RUNNING
   |
   v
STOPPED
```

Ante errores críticos al inicializar el módulo, el estado vuelve a `CREATED`
permitiendo un reintento de `initialize()`:

```text
INITIALIZING
    |
    v
 CREATED
```

Ante errores críticos al finalizar el módulo (fallo en `shutdown()`):

```text
RUNNING
   |
   v
 FAILED
```

`shutdown()` también puede ejecutarse durante `INITIALIZING`, transicionando a
`STOPPED` o `FAILED` según el resultado. Los estados `STOPPED` y `FAILED` son
terminales.

---

## Dependencias Externas

### Java SE

* `java.io.BufferedReader`
* `java.io.BufferedWriter`
* `java.io.IOException`
* `java.io.InputStream`
* `java.io.Reader`
* `java.nio.charset.StandardCharsets`
* `java.nio.file.AtomicMoveNotSupportedException`
* `java.nio.file.Files`
* `java.nio.file.Path`
* `java.nio.file.Paths`
* `java.nio.file.StandardCopyOption`
* `java.nio.file.StandardOpenOption`
* `java.text.SimpleDateFormat`
* `java.util.ArrayList`
* `java.util.Date`

### PyMApp Base (`dev.ferol.pymapp.base`)

* `.kernel.Kernel`
* `.kernel.KernelState`
* `.mod.ModManager`
* `.mod.ModPLAT`
* `.mod.ModState`
* `.exception.KernelIllegalStateException`
* `.exception.ModManagerIllegalStateException`
* `.exception.CCSResourceAccessException`
* `.exception.CCSResourceFormatException`
* `.exception.ExcMsg`
* `.validator.ParameterValidator`
* `.ccs.config.Config`
* `.ccs.log.Log`
* `.ccs.log.LogTYPE`
* `.ccs.log.LogLEVEL`
* `.ccs.log.LogTypeStatus`
* `.ccs.log.LogLevelStatus`
* `.ccs.log.LogFormatLine`
* `.ccs.i18n.I18n`

### PyMApp Util (`dev.ferol.pymapp.util`)

* `.format.CSV`
* `.format.Properties`
* `.format.XML`
* `.format.JSON`

