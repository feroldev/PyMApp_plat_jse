# ADR-02 - Punto Único de Acceso

## Estado

Aceptado

## Contexto

Los módulos consumidores necesitan acceder a varios servicios de plataforma.

## Decisión

Centralizar el acceso mediante la clase `PyMApp_plat_jse`.

La clase actúa como:

* Singleton.
* Fachada.
* Administrador del ciclo de vida.

## Consecuencias

### Positivas

* API simplificada.
* Menor complejidad para consumidores.
* Control centralizado.

### Negativas

* Mayor responsabilidad concentrada en una sola clase.

