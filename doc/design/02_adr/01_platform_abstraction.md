# ADR-01 - Abstracción de Plataforma

## Estado

Aceptado

## Contexto

PyMApp debe poder ejecutarse sobre diferentes plataformas sin modificar el resto de los módulos.

## Decisión

Introducir el contrato `ModPLAT` como punto de acceso a servicios dependientes de la plataforma.

Las implementaciones concretas se proveerán mediante módulos específicos.

## Consecuencias

### Positivas

* Portabilidad.
* Bajo acoplamiento.
* Mayor mantenibilidad.

### Negativas

* Incremento de capas de abstracción.
* Necesidad de implementar cada plataforma por separado.

