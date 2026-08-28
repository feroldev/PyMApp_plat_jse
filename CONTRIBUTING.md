# Contributing

Thank you for your interest in contributing to **PyMApp PLAT.mod - Java SE**.

Contributions are welcome through:

* Bug reports.
* Documentation improvements.
* Code improvements.
* Pull requests.

## Before You Start

1. Verify the issue can be reproduced with the current version (see `VERSION`).
2. Check the existing documentation (`README.md`, `doc/design/`) to confirm the expected behavior.
3. Open an issue before proposing major changes, so the design can be discussed first.
4. Keep changes focused and minimal. One concern per pull request.

## Development Environment

* **IDE:** Eclipse (project structure and `.classpath` are Eclipse-based; no Maven/Gradle build).
* **Java:** 8+ (Eclipse compliance/source/target 1.8).
* **Dependencies:** `PyMApp_base` and `PyMApp_util`.
* **Tests:** JUnit 4 (test sources in `src/test/java`). Run the full suite after changes.

## Code Standards

The project follows the standards of the PyMApp Framework:

* **Javadoc:** mandatory. First sentence is a concise summary; body explains the intent and the *why*. Use `<br>` for line breaks, `<br><br>` for paragraphs, `{@code ...}` for code and `{@link ...}` for references. Tags in order: `@param`, `@return` (non-void only), `@throws`, `@see`, `@author`, `@version`.
* **Package documentation:** every package must include a `package-info.java` file documenting its purpose and exposed services.
* **Logging:** mandatory use of `ccs.log` (`LogTYPE`, `LogLEVEL`) instead of `System.out` or `java.util.logging`.
* **Error messages:** use of `ExcMsg` templates for technical error messages.
* **i18n:** internationalized resources (Text, Images and Icons) managed by `ccs.i18n` must follow the naming convention `[ResourceType]_<ISO_CODE>.java`.

## Compatibility Rules

* **Java 8:** keep Java 8 compatibility. Do not use modern Java APIs (records, `List.of`, `String.isBlank`, switch expressions, etc.).
* **Core module:** this module depends on `PyMApp_base`; do not introduce changes that break the portability rules of the core (Java 8).
* **Decoupling:** the module is the only one that knows Java SE platform details; do not leak platform specifics to other layers.

## Pull Request Checklist

1. Code compiles with Java 8 without warnings.
2. Javadoc and `package-info.java` are up to date.
3. New functionality is covered by JUnit 4 tests.
4. `CHANGELOG.md` includes an entry under `[Unreleased]`.
5. `VERSION` is updated only if the change modifies the public contract (per Semantic Versioning).

Thank you for helping improve the project.

