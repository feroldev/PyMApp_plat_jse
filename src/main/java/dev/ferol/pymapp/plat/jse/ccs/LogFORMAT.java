/* 
 * @(#)LogFORMAT.java    1.6.2 25/07/30
 * 
 * Copyright (c) 1999-2025 OLMEDO Fernando R. {ferol.dev}
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */


package dev.ferol.pymapp.plat.jse.ccs;


import dev.ferol.pymapp.base.ccs.log.Log;
import dev.ferol.pymapp.base.ccs.log.LogLEVEL;
import dev.ferol.pymapp.base.ccs.log.LogTYPE;

import dev.ferol.pymapp.plat.jse.PyMApp_plat_jse;


/**
 * Formatos de salida de log soportados por el módulo de plataforma
 * {@link PyMApp_plat_jse} para el registro de lineas de log del CCS
 * {@link Log}.<br>
 * El formato seleccionado determina la implementación de plataforma
 * utilizada por {@link PyMApp_plat_jse#setLogLinePLAT(String, String,
 * LogTYPE, LogLEVEL, int, int, String, Object, Throwable, int)} para
 * escribir la línea de log.
 * <br><br>
 * 
 * @see PyMApp_plat_jse#setLogLinePLAT(String, String, LogTYPE, LogLEVEL, int, int, String, Object, Throwable, int)
 * @see PyMApp_plat_jse#setLogFormat(LogFORMAT)
 * @see PyMApp_plat_jse#getLogFormat()
 * @see Log
 * 
 * @author    OLMEDO Fernando R. {ferol.dev}
 * @version    1.6.2 25/07/30
 */
public enum LogFORMAT {
    /** Formato de salida de log en archivo de texto formato CSV variante PyMApp. */
    CSV,
    /** Formato de salida de log en archivo NDJSON (Newline Delimited JSON, compatible con JSON Lines). */
    JSON
    /**  */
    //SQLITE,
}
