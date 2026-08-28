# PyMApp PLAT.mod - Java SE

[![License: MPL 2.0](https://img.shields.io/badge/License-MPL_2.0-blue.svg)](https://mozilla.org/MPL/2.0/)
![Version](https://img.shields.io/badge/Version-1.6.2-blue)
![Java](https://img.shields.io/badge/Java-8%2B-blue)

Platform module of the [PyMApp Framework](https://github.com/feroldev/PyMApp_Framework) that implements the integration and execution layer over the Java SE environment. It provides the platform adaptations of the cross-cutting services (CCS) — configuration, logging and internationalization — to the rest of the framework modules without them needing to know the specific platform they are running on.

---

## Features

* **Platform abstraction for Java SE:** implements the `ModPLAT` contract (`dev.ferol.pymapp.base.mod.ModPLAT`) exposing the platform I/O for the CCS `Config` and `Log` of `PyMApp_base`.
* **CCS Config (configuration):** read/write of `key=value` resources in three formats:
  * `TEXT` — plain text properties compatible with `java.util.Properties`.
  * `XML` — properties XML (DTD of `java.util.Properties`), preserving comments and `<comment>` elements.
  * `JSON` — flat key-value object, one entry per line, preserving the original formatting.
  * Atomic write via temporary file with unique suffix (`yyMMddHHmmssSSS`) and safe replacement.
* **CCS Log (logging):** log line registration in two output formats:
  * `CSV` — PyMApp variant: `|` (ASCII 124) separator, no header line, escaping of the separator, backslash and control characters.
  * `JSON` — NDJSON (Newline Delimited JSON, JSON Lines compatible).
  * Configurable file rotation (`_%g` pattern) and size limit (minimum 256 bytes).
* **Full module lifecycle:** Singleton access, `CREATED → INITIALIZING → RUNNING → STOPPED/FAILED` states, consistent with `ModState` of `PyMApp_base`.
* **Module CCS resources:** own `Config`, `Log` and `I18n` resources initialized by `initialize(String)`, configurable through the module configuration keys.
* **CCS I18n (internationalization):** module texts in 10 languages (Spanish default): es, de, en, fr, it, ja, ko, pt, ru, zh.
* **Convenience API:** `setConfigKey`, `setConfigNewKey`, `getConfigKey`, `setLogLine` (3 overloads) and `getModI18n` for the module's own resources.
* **Java 8+ compatibility:** no APIs that compromise portability with the core of the framework (`PyMApp_base`).

---

## Requirements

* **Java SE 8 or higher** (compiled with Eclipse compliance/source/target 1.8).
* **PyMApp_base** module (dev.ferol.pymapp.base) — required.
* **PyMApp_util** module (dev.ferol.pymapp.util.format) — required.
* **Eclipse IDE** with JUnit 4 (only for running the test suite in `src/test/java`).

---

## Basic Example

The module is configured before kernel initialization and the kernel itself initializes it internally:

```java
import dev.ferol.pymapp.base.kernel.Kernel;
import dev.ferol.pymapp.base.ccs.log.LogTYPE;
import dev.ferol.pymapp.base.ccs.log.LogLEVEL;
import dev.ferol.pymapp.plat.jse.PyMApp_plat_jse;

public class Main {
    public static void main(String[] args) {
        // 1. Singleton of the platform module
        PyMApp_plat_jse plat = PyMApp_plat_jse.getInstance();

        // 2. Configure formats BEFORE initializing the kernel (module in CREATED state)
        plat.setConfigFormat(PyMApp_plat_jse.ConfigFORMAT.TEXT); // TEXT | XML | JSON
        plat.setConfigExtens(".config");
        plat.setLogFormat(PyMApp_plat_jse.LogFORMAT.CSV); // CSV | JSON
        plat.setLogExtens(".log");

        // 3. Register the module in the kernel (initializes the PLAT internally)
        Kernel kernel = Kernel.getInstance();
        kernel.setAppModPLAT(plat);
        kernel.initialize(args);
    }
}
```

> **Note:** the format setters throw `ModManagerIllegalStateException` if the module is no longer in `CREATED` state. They must be called before `Kernel.initialize(String)`.

---

## Full Example

Once the kernel is running, the module exposes its own CCS resources and the platform contract:

```java
// --- Convenience API (module's own resources) ---

// Write / read keys of the module configuration resource
plat.setConfigNewKey("mod.Log.Bytes", "1677722");
String bytes = plat.getConfigKey("mod.Log.Bytes");

// Log a line in the module's main log resource (AUDIT goes to the application audit resource)
plat.setLogLine(LogTYPE.APPLICATION, LogLEVEL.INFORMATIONAL, "Module operational.");

// Module metadata (works through the CCS I18n)
String displayName = plat.getModDISPLAYNAME();
String description = plat.getModDESCRIPTION();

// --- PLAT contract (used by the CCS of PyMApp_base) ---

// Write / read any configuration resource in the platform format
plat.setConfigKeyPLAT("/path/to/config", "MyApp", "app.SingleUserMode", "true", true);
String value = plat.getConfigKeyPLAT("/path/to/config", "MyApp", "app.SingleUserMode");

// Write any log resource with size limit and rotation
plat.setLogLinePLAT(
    "/path/to/log", "MyApp", // resource path and name
    LogTYPE.APPLICATION, LogLEVEL.INFORMATIONAL,
    1677722, 7, // maxBytes, numRotation (rotation name "MyApp_%g.log")
    "Application started.", null, null, 0
);

// --- Lifecycle ---
plat.getState();   // ModState.RUNNING
plat.shutdown();   // ModState.STOPPED (or FAILED on error)
```

---

## Module Configuration

The module reads its own `Config` resource (`PyMApp_plat_jse` + extension of the configured format) on `initialize(String)`. Keys:

| Key | Default | Description |
| --- | ------- | ----------- |
| `mod.Log.TypeActive.<TYPE>` | `true` | Enables/disables each `LogTYPE` (`SYSTEM`, `APPLICATION`, `EXCEPTION`, `AUDIT`, `DATABASE`, `DEBUG`). |
| `mod.Log.LevelActive.<LEVEL>` | `true` | Enables/disables each `LogLEVEL` (`EMERGENCY`, `ALERT`, `CRITICAL`, `ERROR`, `WARNING`, `NOTICE`, `INFORMATIONAL`, `DEBUG`). |
| `mod.Log.Bytes` | `1677722` | Size in bytes of the module's main log resource. |
| `mod.Log.Rotation` | `1` | Number of rotations of the module's main log resource. |
| `mod.i18n.resPath` | `dev.ferol.pymapp.plat.jse.i18n` | Root package of the module's CCS I18n. |

Reference resources in three formats are provided in `res/`: `PyMApp_plat_jse.config`, `PyMApp_plat_jse.json` and `PyMApp_plat_jse.xml`.

---

## How It Works

* **Architecture:** plugin architecture with static extension configuration. The platform module is the only one that knows the details of the Java SE platform; the rest of the modules consume the adaptations through the `ModPLAT` contract.
* **Direction of the dependency:** the CCS of `PyMApp_base` (`Config`, `Log`) delegate all resource I/O to `ModPLAT` methods (`setConfigKeyPLAT`, `getConfigKeyPLAT`, `setLogLinePLAT`); the platform module delegates the concrete I/O to the utility classes `ConfigPLAT` and `LogPLAT`, and the text formatting of the log line to `LogFormatLine` of `PyMApp_base`.
* **Log filtering:** the platform does not decide what is logged; `Log` filters by `LogTypeStatus`/`LogLevelStatus` before invoking `setLogLinePLAT`. The module's own `setLogLine` applies the same rule with its own statuses. `AUDIT` logs are written to the application's general audit resource.
* **Error handling:** platform errors are propagated as `CCSResourceAccessException`/`CCSResourceFormatException` using `ExcMsg` templates; the CCS layer logs the error in the master resource.

---

## Design Goals

* **Decoupling:** separation between business logic (Java) and platform specifics; no other module knows the concrete platform.
* **Portability:** the functional logic of the framework does not know Java SE details; the module keeps Java 8 compatibility.
* **Single entry point:** the module is exposed as a Singleton (`getInstance()` / `getModManager()`).
* **Centralized resources:** configuration, log and i18n of the module managed by the framework CCS.
* **Format preservation:** config resources keep comments and formatting when updating keys.

---

## Limitations

* The `JSON` config format supports a flat key-value object with one entry per line; nested objects, arrays and multiple entries per line are rejected with `CCSResourceFormatException`.
* The `JSON` config format only processes string values; non-textual values of a key are replaced when updating it.
* The `XML` config format requires a non-empty resource with the mandatory root structure.
* Platform Java SE only; other platforms (e.g. Android) are covered by their own PLAT modules of the framework.

---

## Documentation

* `doc/design/01_architecture/` — architecture documents.
* `doc/design/02_adr/` — architecture decision records (ADR).
* `doc/design/03_diagrams/` — PlantUML diagrams.

---

## Project Structure

```
src/main/java/dev/ferol/pymapp/plat/jse/
├── PyMApp_plat_jse.java          # Module manager (Singleton, ModPLAT contract)
├── ccs/
│   ├── ConfigPLAT.java           # Platform I/O: config resources (TEXT, XML, JSON)
│   └── LogPLAT.java              # Platform I/O: log resources (CSV, NDJSON) + rotation
└── i18n/text/
    ├── Text.java                 # Default texts (Spanish)
    └── Text_<ISO>.java           # de, en, es, fr, it, ja, ko, pt, ru, zh
src/test/java/                    # JUnit 4 test suite
res/                              # Module resources: config (3 formats)
doc/design/                       # Architecture, ADR and diagrams
```

---

## Project Information

Project: PyMApp PLAT.mod - Java SE

Author: Fernando R. Olmedo {ferol.dev}

Repository: https://github.com/feroldev/PyMApp_plat_jse

Version: 1.6.2

License: Mozilla Public License Version 2.0.

---

## Maven Central

PyMApp PLAT.mod - Java SE is available from Maven Central.

### Maven

```xml
<dependency>
    <groupId>dev.ferol</groupId>
    <artifactId>pymapp-plat-jse</artifactId>
    <version>1.6.2</version>
</dependency>
```

### Gradle

```gradle
implementation 'dev.ferol:pymapp-plat-jse:1.6.2'
```

The artifact is available at:

[PyMApp PLAT.mod - Java SE on Maven Central](https://central.sonatype.com/artifact/dev.ferol/pymapp-plat-jse?utm_source=chatgpt.com)

---

## License

Copyright (c) 1999-2025 OLMEDO Fernando R. {ferol.dev}

Licensed under the Mozilla Public License Version 2.0. You may obtain a copy of the License at https://mozilla.org/MPL/2.0/.

