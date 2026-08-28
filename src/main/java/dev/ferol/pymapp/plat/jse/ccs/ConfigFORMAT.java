/* 
 * @(#)ConfigFORMAT.java    1.6.2 25/07/30
 * 
 * Copyright (c) 1999-2025 OLMEDO Fernando R. {ferol.dev}
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */


package dev.ferol.pymapp.plat.jse.ccs;


import dev.ferol.pymapp.base.ccs.config.Config;

import dev.ferol.pymapp.plat.jse.PyMApp_plat_jse;


/**
 * Formatos de archivo de configuración soportados por el módulo de plataforma
 * {@link PyMApp_plat_jse} para la lectura y escritura de recursos CCS
 * {@link Config}.<br>
 * El formato seleccionado determina la implementación de plataforma utilizada
 * por {@link PyMApp_plat_jse#setConfigKeyPLAT(String, String, String, String,
 * boolean)} y {@link PyMApp_plat_jse#getConfigKeyPLAT(String, String, String)}
 * para acceder al recurso de configuración.
 * <br><br>
 * 
 * @see PyMApp_plat_jse#setConfigKeyPLAT(String, String, String, String, boolean)
 * @see PyMApp_plat_jse#getConfigKeyPLAT(String, String, String)
 * @see PyMApp_plat_jse#setConfigFormat(ConfigFORMAT)
 * @see PyMApp_plat_jse#getConfigFormat()
 * @see Config
 * 
 * @author    OLMEDO Fernando R. {ferol.dev}
 * @version    1.6.2 25/07/30
 */
public enum ConfigFORMAT {
    /**
     * Formato de archivo de texto plano con pares clave-valor separados por
     * "=", compatible con {@link java.util.Properties}.
     */
    TEXT,
    /**
     * Formato de archivo XML compatible con {@link java.util.Properties}
     * via {@link java.util.Properties#loadFromXML(java.io.InputStream)} y
     * {@link java.util.Properties#storeToXML(java.io.OutputStream, String)}.
     */
    XML,
    /**
     * Formato de archivo JSON (JavaScript Object Notation) con pares
     * clave-valor de un único nivel de profundidad y una entrada por
     * línea.<br>
     * Los valores soportados son exclusivamente cadenas de texto JSON
     * (delimitadas por comillas dobles); los valores numéricos, booleanos
     * o {@code null} no son procesados por el CCS de configuración, y los
     * valores no textuales de una clave existente se reemplazan al
     * actualizar la clave.
     */
    JSON
    /**  */
    //SQLITE,
}
