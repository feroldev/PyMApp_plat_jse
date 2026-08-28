# Abstracción de Plataforma

## Objetivo

PyMApp separa la lógica de negocio de los detalles específicos de la plataforma de ejecución.

Para lograrlo, los módulos en general interactúan únicamente con contratos definidos en PyMApp Base, mientras que las implementaciones concretas son aportadas por módulos de plataforma.

## Motivación

Sin una capa de abstracción, cualquier módulo que necesitara acceder a:

* Configuración
* Logging

quedaría acoplado a APIs concretas de Java SE.

Esto dificultaría la portabilidad del framework hacia otros entornos.

## Solución Adoptada

El módulo `PyMApp_plat_jse` implementa los contratos definidos por la capa Base.

```text
Aplicación
    │
      ▼
PyMApp Base
    │
      ▼
 ModPLAT
    │
      ▼
PyMApp_plat_jse
```

Los consumidores acceden únicamente a la interfaz `ModPLAT`.

La implementación concreta permanece oculta.

## Beneficios

* Bajo acoplamiento.
* Portabilidad.
* Sustitución de implementaciones.
* Evolución independiente de la plataforma.

## Limitaciones

La plataforma debe estar correctamente inicializada antes de que los servicios puedan utilizarse.

Las funcionalidades disponibles dependen de la implementación concreta de `ModPLAT`.

