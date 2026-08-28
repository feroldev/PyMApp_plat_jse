/* 
 * @(#)PyMApp_plat_jse.java    1.6.2 25/07/30
 * 
 * Copyright (c) 1999-2025 OLMEDO Fernando R. {ferol.dev}
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */


package dev.ferol.pymapp.plat.jse;


import java.nio.file.Paths;

import dev.ferol.pymapp.base.kernel.Kernel;
import dev.ferol.pymapp.base.kernel.KernelState;
import dev.ferol.pymapp.base.mod.ModManager;
import dev.ferol.pymapp.base.mod.ModPLAT;
import dev.ferol.pymapp.base.mod.ModState;
import dev.ferol.pymapp.base.exception.KernelIllegalStateException;
import dev.ferol.pymapp.base.exception.ModManagerIllegalStateException;
import dev.ferol.pymapp.base.exception.CCSResourceAccessException;
import dev.ferol.pymapp.base.exception.CCSResourceFormatException;
import dev.ferol.pymapp.base.exception.ExcMsg;
import dev.ferol.pymapp.base.validator.ParameterValidator;
import dev.ferol.pymapp.base.ccs.config.Config;
import dev.ferol.pymapp.base.ccs.log.Log;
import dev.ferol.pymapp.base.ccs.log.LogTYPE;
import dev.ferol.pymapp.base.ccs.log.LogLEVEL;
import dev.ferol.pymapp.base.ccs.log.LogTypeStatus;
import dev.ferol.pymapp.base.ccs.log.LogLevelStatus;
import dev.ferol.pymapp.base.ccs.i18n.I18n;

import dev.ferol.pymapp.plat.jse.ccs.ConfigPLAT;
import dev.ferol.pymapp.plat.jse.ccs.ConfigFORMAT;
import dev.ferol.pymapp.plat.jse.ccs.LogPLAT;
import dev.ferol.pymapp.plat.jse.ccs.LogFORMAT;


/**
 * Administrador del módulo {@code PyMApp_plat_jse}, este módulo provee de una
 * serie de clases diseñadas para permitirle a la aplicación que utiliza la
 * librería, adaptarse a la plataforma Java SE.<br>
 * El diseño de PyMApp está basado en la arquitectura de plugin más precisamente
 * en el de sistema de extensiones con configuración estática, esta característica
 * no solo abarca la lógica de negocio y la interfaz de usuario (UI), sino también
 * a la plataforma en donde se ejecutan, dado el carácter multiplataforma de la
 * librería se requiere de un módulo de plataforma mediante el cual los distintos
 * módulos puedan consumir las adaptaciones de los servicios en los que difieren
 * las distintas plataformas, permitiendo la interacción entre esta y la librería
 * sin que el resto de los módulos necesiten conocer la plataforma específica en
 * la que se están ejecutando.<br>
 * Como cualquier módulo de plataforma {@code PyMApp_plat_jse} implementa la
 * interfaz {@link ModPLAT} que a su vez extiende la interfaz predeterminada para
 * todos los módulos {@link ModManager}.
 * <br><br>
 * 
 * @see #setConfigExtens(String)
 * @see #setLogExtens(String)
 * @see #setConfigKey(String, String)
 * @see #setConfigNewKey(String, String)
 * @see #setLogLine(LogTYPE, LogLEVEL, String, Object, Throwable, int)
 * @see #getConfigExtens()
 * @see #getLogExtens()
 * @see #getConfigKey(String)
 * @see #getModI18n()
 * @see #setConfigKeyPLAT(String, String, String, String, boolean)
 * @see #setLogLinePLAT(String, String, LogTYPE, LogLEVEL, int, int, String, Object, Throwable, int)
 * @see #initialize(String)
 * @see ModPLAT
 * @see ModManager
 * 
 * @author    OLMEDO Fernando R. {ferol.dev}
 * @version    1.6.2 25/07/30
 */
public final class PyMApp_plat_jse implements ModPLAT {
    private static PyMApp_plat_jse modManager; // Instancia Singleton
  /* Identificadores del Módulo */
    private static final String MOD_NAME = "PyMApp_plat_jse"; // Nombre Interno del Módulo (En lo Posible el Mismo Nombre que la Clase)
    private static final String MOD_VERSION = "1.6.2"; // Versión Interna del Módulo
  /* Entorno del Módulo */
    private static final Kernel KERNEL = Kernel.getInstance(); // Núcleo de la Librería (Singleton)
    private ModState state; // Indicador Estado del Módulo
    private ConfigFORMAT CONFIG_FORMAT = ConfigFORMAT.TEXT; // Formato de los Recursos CCS Config
    private String CONFIG_EXTENS = ".config"; // Extensión Predeterminada para Archivos CCS Config
    private LogFORMAT LOG_FORMAT = LogFORMAT.CSV; // Formato de los Recursos CCS Log
    private String LOG_EXTENS = ".log"; // Extensión Predeterminada para Archivos CCS Log
  /* Cross-Cutting Service que Implementa el Módulo */
    private static final String MOD_CONFIG_NAME = MOD_NAME; // Nombre del Recurso Principal de Configuración del Módulo
    private Config modConfig; // Recurso Principal de Configuración del Módulo
    private static final String MOD_LOG_NAME = MOD_NAME; // Nombre del Recurso Principal de Log del Módulo
    private Log modLog; // Recurso Principal de Log del Módulo
    private static final String CONFIG_KEY_MOD_LOG_TYPE_ACTIVE = "mod.Log.TypeActive."; // Clave de Configuración Madre de los LogTYPE Activos para el Módulo
    private static final String CONFIG_KEY_MOD_LOG_LEVEL_ACTIVE = "mod.Log.LevelActive."; // Clave de Configuración Madre de los LogLEVEL Activos para el Módulo
    private static final int MOD_LOG_BYTES = 1677722; // Tamaño Predeterminado en Bytes de modLog
    private static final String CONFIG_KEY_MOD_LOG_BYTES = "mod.Log.Bytes"; // Clave de Configuración del Tamaño Predeterminado en Bytes de modLog
    private static final int MOD_LOG_ROTATION = 1; // Cantidad Predeterminada de Rotación de modLog
    private static final String CONFIG_KEY_MOD_LOG_ROTATION = "mod.Log.Rotation"; // Clave de Configuración de la Cantidad Predeterminada de Rotación de modLog
    private static final String MOD_I18N_PATH = "dev.ferol.pymapp.plat.jse.i18n"; // Paquete Root Predeterminado del CCS I18n del Módulo
    private static final String CONFIG_KEY_MOD_I18N_PATH = "mod.i18n.resPath"; // Clave de Configuración del Paquete Root Predeterminado del CCS I18n del Módulo
    private I18n modI18n; // Recurso I18n de Internacionalización del Módulo
    
    
/*----------------------------------------------------------------------------*/
/*                               Constructores                                */
/*----------------------------------------------------------------------------*/
    
    
    /**
     * Constructor Singleton.
     * <br><br>
     * 
     * @see #getInstance()
     * @see #getModManager()
     */
    private PyMApp_plat_jse() {
        state = ModState.CREATED;
    }
    
    
    /**
     * Permite el acceso a la instancia única Singleton del administrador del
     * módulo. {@code getInstance()} actúa como constructor de creación estática,
     * invocando al constructor {@code private} para crear una única instancia
     * y guardarla en caché, al llamar a {@code getInstance()} devuelve la
     * instancia de tipo {@link PyMApp_plat_jse} almacenada en caché, única
     * instancia existente del administrador del módulo en el entorno de la
     * aplicación, el método {@code getInstance()} es {@code synchronized} esto
     * es necesaria para evitar que se produzca una condición de carrera, en donde
     * mas de un {@link Thread} intente acceder al mismo tiempo, {@code synchronized}
     * fuerza la sincronización del acceso al método.
     * <br><br>
     * 
     * @return Instancia Singleton de tipo {@link PyMApp_plat_jse} almacenada en
     *         caché, única instancia existente del administrador del módulo en el
     *         entorno de la aplicación que utiliza el módulo.
     * 
     * @see #getModManager()
     * @see #initialize(String)
     * @see #getState()
     */
    public static synchronized PyMApp_plat_jse getInstance() {
        if (modManager == null) {
            modManager = new PyMApp_plat_jse();
        }
        
        return modManager;
    }
    
    
/*----------------------------------------------------------------------------*/
/*                                  Setters                                   */
/*----------------------------------------------------------------------------*/
    
    
    /**
     * Establece el formato predeterminado para los recursos CCS de
     * configuración {@link Config}.<br>
     * Esta propiedad debe ser establecida antes de inicializar el módulo
     * ({@link #initialize(String)}) y no puede ser modificada posteriormente.
     * <br><br>
     * 
     * @param value Formato predeterminado para los recursos de configuración.
     * 
     * @throws ModManagerIllegalStateException Si el módulo ya fue inicializado
     *         {@link #getState()} {@code != }{@link ModState#CREATED}.
     * 
     * @see #getConfigFormat()
     * @see #setConfigExtens(String)
     * @see #getState()
     */
    public synchronized void setConfigFormat(ConfigFORMAT value) throws ModManagerIllegalStateException {
        propertyBlocked("ConfigFormat");
        
        if (value != null) {
            CONFIG_FORMAT = value;
        }
    }
    
    
    /**
     * Establece la extensión predeterminada para los recursos CCS de
     * configuración {@link Config}.<br>
     * Esta propiedad debe ser establecida antes de inicializar el módulo
     * ({@link #initialize(String)}) y no puede ser modificada posteriormente.
     * <br><br>
     * 
     * @param value Extensión predeterminada para los recursos de configuración.
     * 
     * @throws ModManagerIllegalStateException Si el módulo ya fue inicializado
     *         {@link #getState()} {@code != }{@link ModState#CREATED}.
     * 
     * @see #getConfigExtens()
     * @see #setConfigFormat(ConfigFORMAT)
     * @see #getState()
     */
    public synchronized void setConfigExtens(String value) throws ModManagerIllegalStateException {
        propertyBlocked("ConfigExtens");
        
        if (value == null || value.trim().isEmpty()) {
            value = "";
        }
        
        CONFIG_EXTENS = value;
    }
    
    
    /**
     * Establece el formato predeterminado para los recursos CCS de
     * configuración {@link Log}.<br>
     * Esta propiedad debe ser establecida antes de inicializar el módulo
     * ({@link #initialize(String)}) y no puede ser modificada posteriormente.
     * <br><br>
     * 
     * @param value Formato predeterminado para los recursos de log.
     * 
     * @throws ModManagerIllegalStateException Si el módulo ya fue inicializado
     *         {@link #getState()} {@code != }{@link ModState#CREATED}.
     * 
     * @see #getLogFormat()
     * @see #setLogExtens(String)
     * @see #getState()
     */
    public synchronized void setLogFormat(LogFORMAT value) throws ModManagerIllegalStateException {
        propertyBlocked("LogFormat");
        
        if (value != null) {
            LOG_FORMAT = value;
        }
    }
    
    
    /**
     * Establece la extensión predeterminada para los recursos CCS de
     * log {@link Log}.<br>
     * Esta propiedad debe ser establecida antes de inicializar el módulo
     * ({@link #initialize(String)}) y no puede ser modificada posteriormente.
     * <br><br>
     * 
     * @param value Extensión predeterminada para los recursos de log.
     * 
     * @throws ModManagerIllegalStateException Si el módulo ya fue inicializado
     *         {@link #getState()} {@code != }{@link ModState#CREATED}.
     * 
     * @see #getLogExtens()
     * @see #setLogFormat(LogFORMAT)
     * @see #getState()
     */
    public synchronized void setLogExtens(String value) throws ModManagerIllegalStateException {
        propertyBlocked("LogExtens");
        
        if (value == null || value.trim().isEmpty()) {
            value = "";
        }
        
        LOG_EXTENS = value;
    }
    
    
    /**
     * Establece el valor de una clave del recurso de configuración predeterminado
     * del módulo.<br>
     * En el caso de que el proceso de escritura del valor de la clave de
     * configuración genere una excepción se registra el log de la excepción en el
     * recurso principal de logs de la aplicación {@link Kernel#setLogLine(LogTYPE,
     * LogLEVEL, String, Object, Throwable, int)}.
     * <br><br>
     * 
     * @param key Clave que se desea modificar.
     * @param value Valor de la clave que se desea modificar.
     * 
     * @return {@code true} si la operación es exitosa o {@code false} si no lo fue.
     * 
     * @throws ModManagerIllegalStateException Si el módulo no se encuentra
     *         en {@link #getState()} {@code  == }{@link ModState#RUNNING}.
     * @throws IllegalArgumentException Si el parámetro {@code key} es igual a
     *         {@code null} o vacío.
     * 
     * @see #setConfigNewKey(String, String)
     * @see #getConfigKey(String)
     * @see #getState()
     * @see #setConfigKeyPLAT(String, String, String, String, boolean)
     */
    public boolean setConfigKey(String key, String value)
    throws ModManagerIllegalStateException, IllegalArgumentException {
        checkOperability();
      /*(a) Validación de Parámetros */
        ParameterValidator.notNullBlank(key, "key");
        
        if (value == null) {
            value = "";
        }
      /*(a)*/
        
        return modConfig.setKey(key, value);
    }
    
    
    /**
     * Agrega una nueva clave y su valor en el recurso de configuración
     * predeterminado del módulo. Si la clave ya existe solo actualiza el
     * valor de la misma.<br>
     * En el caso de que el proceso de escritura genere una excepción se registra
     * el log de la excepción en el recurso principal de logs de la aplicación
     * {@link Kernel#setLogLine(LogTYPE, LogLEVEL, String, Object, Throwable, int)}.
     * <br><br>
     * 
     * @param key Clave que se desea añadir.
     * @param value Valor de la clave que se desea añadir.
     * 
     * @return {@code true} si la operación es exitoso o {@code false} si no lo fue.
     * 
     * @throws ModManagerIllegalStateException Si el módulo no se encuentra
     *         en {@link #getState()} {@code  == }{@link ModState#RUNNING}.
     * @throws IllegalArgumentException Si el parámetro {@code key} es igual a
     *         {@code null} o vacío.
     * 
     * @see #setConfigKey(String, String)
     * @see #getConfigKey(String)
     * @see #getState()
     * @see #setConfigKeyPLAT(String, String, String, String, boolean)
     */
    public boolean setConfigNewKey(String key, String value)
    throws ModManagerIllegalStateException, IllegalArgumentException {
        checkOperability();
      /*(a) Validación de Parámetros */
        ParameterValidator.notNullBlank(key, "key");
        
        if (value == null) {
            value = "";
        }
      /*(a)*/
        
        return modConfig.setNewKey(key, value);
    }
    
    
    /**
     * Registra una línea de log en el recurso CCS log {@link Log} principal del
     * módulo, en el caso particular de los logs de auditoria {@code logTYPE.AUDIT}
     * estos son registrados en el recurso CCS de auditoria general de la aplicación
     * {@link Kernel#setLogLine(LogTYPE, LogLEVEL, String, Object, Throwable, int)}.<br>
     * El registro solo se efectúa si el {@link LogTYPE} y el {@link LogLEVEL}
     * de los parámetros {@code logTYPE} y {@code logLEVEL} se encuentran activos
     * en los correspondientes {@link LogTypeStatus} y {@link LogLevelStatus}
     * asociados al recurso principal de logs del módulo.
     * <br><br>
     * 
     * @param logTYPE Tipo {@link LogTYPE} que se desea registrar.
     * @param logLEVEL Nivel {@link LogLEVEL} que se desea registrar.
     * @param message Mensaje principal del log que se desea registrar,
     *        se permite valor {@code null} o vacío.
     * @param addData Extiende la información relacionada con el evento,
     *        condiciones o pila que registra el log, es de carácter opcional
     *        y puede ser de valor {@code null}. {@code addData} es de tipo
     *        {@code Object} lo que lo hace de uso general y esto se debe a el
     *        método {@link Object#toString()} que permite que sea procesado de
     *        la siguiente manera:
     *        <br><br>
     *        {@code (addData == null) ? "" : addData.toString()}
     *        <br><br>
     *        esto permite la utilización de una variedad muy amplia de clases
     *        tanto propias del lenguaje como creadas por el desarrollador,
     *        tan solo debe devolver la información adecuada a este parámetro
     *        mediante el método {@code toString()}.
     * @param exception Excepción que se desea registrar, se permite valor
     *        {@code null}.
     * @param stackAdjust Ajusta el indice de la pila que se desea registrar en el
     *        log, el indice predeterminado es 0 y corresponde a la linea desde la
     *        cual se invoca este método (el punto de llamada del código que usa el
     *        log).<br>
     *        Si se delega el registro a un método propio (wrappers, listeners,
     *        procesamiento por lotes) deben incrementar el ajuste en 1 por cada
     *        nivel de delegación intermedio, para que el log registre el punto de
     *        origen real y no el método interno que ejecuta la llamada:
     *        <br><br>
     *        {@code stackAdjust == 0}: registra la linea desde donde se invoca el
     *        CCS de log.<br>
     *        {@code stackAdjust == 1}: registra la linea del método que invoco al
     *        llamador (1 nivel hacia afuera).<br>
     *        {@code stackAdjust == N}: registra N niveles mas hacia afuera en la
     *        cadena de llamadas.<br>
     *        Valores negativos se desplazan hacia marcos internos del framework y
     *        carecen de utilidad practica; el indice se recorta automáticamente a
     *        los limites de la pila disponible, por lo que un ajuste excesivo
     *        registra el marco mas externo existente sin lanzar excepción.
     * 
     * @throws ModManagerIllegalStateException Si el módulo no se encuentra
     *         en {@link #getState()} {@code  == }{@link ModState#RUNNING} o
     *         {@link ModState#INITIALIZING}.
     * @throws IllegalArgumentException Si los parámetros {@code logTYPE} o
     *         {@code logLEVEL} son igual a {@code null}.
     * 
     * @see #setLogLine(LogTYPE, LogLEVEL, String, Object, Throwable)
     * @see #setLogLine(LogTYPE, LogLEVEL, String, Object)
     * @see LogTYPE
     * @see LogLEVEL
     * @see #getState()
     * @see #setLogLinePLAT(String, String, LogTYPE, LogLEVEL, int, int, String, Object, Throwable, int)
     */
    public void setLogLine(
        LogTYPE logTYPE,
        LogLEVEL logLEVEL,
        String message,
        Object addData,
        Throwable exception,
        int stackAdjust
    ) throws ModManagerIllegalStateException, IllegalArgumentException {
        checkInitializing();
      /*(a) Validación de Parámetro */
        ParameterValidator.notNull(logTYPE, "logTYPE");
        ParameterValidator.notNull(logLEVEL, "logLEVEL");
      /*(a)*/
        
        if (logTYPE == LogTYPE.AUDIT) { // Registra Log de Auditoria en Recurso para tal Fin
            stackAdjust = stackAdjust + 1; // Ajusta Stack para Registrar Pila en la Llamada a este Método
            KERNEL.setLogLine(logTYPE, logLEVEL, message, addData, exception, stackAdjust);
        } else if (modLog != null) { // Registro de Logs que no son de Auditoria
            stackAdjust = stackAdjust + 3; // Ajusta Stack para Registrar Pila en la Llamada a este Método
            modLog.setLine(logTYPE, logLEVEL, message, addData, exception, stackAdjust);
        }
    }
    
    
    /**
     * Sobrecarga del método {@link #setLogLine(LogTYPE, LogLEVEL, String, Object,
     * Throwable, int)} desarrollada para facilitar el registro de logs que están
     * relacionados con excepciones y la posibilidad de extender la información
     * del estado del sistema en el momento de la excepción ({@code addData}).<br>
     * El índice de la pila se ajusta en {@code stackAdjust == 1} para que registre
     * la llamada a este método. Equivalente a {@code setLogLine(logTYPE, logLEVEL,
     * message, addData, exception, 1)}.
     * <br><br>
     * 
     * @param logTYPE Tipo {@link LogTYPE} que se desea registrar.
     * @param logLEVEL Nivel {@link LogLEVEL} que se desea registrar.
     * @param message Mensaje principal del log que se desea registrar,
     *        se permite valor {@code null} o vacío.
     * @param addData Extiende la información relacionada con el evento,
     *        condiciones o pila que registra el log, es de carácter opcional
     *        y puede ser de valor {@code null}. {@code addData} es de tipo
     *        {@code Object} lo que lo hace de uso general y esto se debe a el
     *        método {@link Object#toString()} que permite que sea procesado de
     *        la siguiente manera:
     *        <br><br>
     *        {@code (addData == null) ? "" : addData.toString()}
     *        <br><br>
     *        esto permite la utilización de una variedad muy amplia de clases
     *        tanto propias del lenguaje como creadas por el desarrollador,
     *        tan solo debe devolver la información adecuada a este parámetro
     *        mediante el método {@code toString()}.
     * @param exception Excepción que se desea registrar, se permite valor
     *        {@code null}.
     * 
     * @throws ModManagerIllegalStateException Si el módulo no se encuentra
     *         en {@link #getState()} {@code  == }{@link ModState#RUNNING} o
     *         {@link ModState#INITIALIZING}.
     * @throws IllegalArgumentException Si los parámetros {@code logTYPE} o
     *         {@code logLEVEL} son igual a {@code null}.
     * 
     * @see #setLogLine(LogTYPE, LogLEVEL, String, Object, Throwable, int)
     * @see #setLogLine(LogTYPE, LogLEVEL, String, Object)
     * @see LogTYPE
     * @see LogLEVEL
     * @see #getState()
     */
    public void setLogLine(LogTYPE logTYPE, LogLEVEL logLEVEL, String message, Object addData, Throwable exception)
    throws ModManagerIllegalStateException, IllegalArgumentException {
        setLogLine(logTYPE, logLEVEL, message, addData, exception, 1);
    }
    
    
    /**
     * Sobrecarga del método {@link #setLogLine(LogTYPE, LogLEVEL, String, Object,
     * Throwable, int)} desarrollada para facilitar el registro de logs que no
     * están relacionados con excepciones pero que pueden requerir extender la
     * información del estado del sistema en el momento del log ({@code addData}).
     * El índice de la pila se ajusta en {@code stackAdjust == 1} para que registre
     * la llamada a este método. Equivalente a {@code setLogLine(logTYPE, logLEVEL,
     * message, addData, null, 1)}.
     * <br><br>
     * 
     * @param logTYPE Tipo {@link LogTYPE} que se desea registrar.
     * @param logLEVEL Nivel {@link LogLEVEL} que se desea registrar.
     * @param message Mensaje principal del log que se desea registrar,
     *        se permite valor {@code null} o vacío.
     * @param addData Extiende la información relacionada con el evento,
     *        condiciones o pila que registra el log, es de carácter opcional
     *        y puede ser de valor {@code null}. {@code addData} es de tipo
     *        {@code Object} lo que lo hace de uso general y esto se debe a el
     *        método {@link Object#toString()} que permite que sea procesado de
     *        la siguiente manera:
     *        <br><br>
     *        {@code (addData == null) ? "" : addData.toString()}
     *        <br><br>
     *        esto permite la utilización de una variedad muy amplia de clases
     *        tanto propias del lenguaje como creadas por el desarrollador,
     *        tan solo debe devolver la información adecuada a este parámetro
     *        mediante el método {@code toString()}.
     * 
     * @throws ModManagerIllegalStateException Si el módulo no se encuentra
     *         en {@link #getState()} {@code  == }{@link ModState#RUNNING} o
     *         {@link ModState#INITIALIZING}.
     * @throws IllegalArgumentException Si los parámetros {@code logTYPE} o
     *         {@code logLEVEL} son igual a {@code null}.
     * 
     * @see #setLogLine(LogTYPE, LogLEVEL, String, Object, Throwable, int)
     * @see #setLogLine(LogTYPE, LogLEVEL, String, Object, Throwable)
     * @see LogTYPE
     * @see LogLEVEL
     * @see #getState()
     */
    public void setLogLine(LogTYPE logTYPE, LogLEVEL logLEVEL, String message, Object addData)
    throws ModManagerIllegalStateException, IllegalArgumentException {
        setLogLine(logTYPE, logLEVEL, message, addData, null, 1);
    }
    
    
/*----------------------------------------------------------------------------*/
/*                                  Getters                                   */
/*----------------------------------------------------------------------------*/
    
    
    /**
     * Obtiene el tipo de formato predeterminado para los recursos CCS de
     * configuración {@link Config}.
     * <br><br>
     * 
     * @return Tipo de formato predeterminado para los recursos de configuración.
     * 
     * @see #setConfigFormat(ConfigFORMAT)
     * @see #getConfigExtens()
     * @see Config
     */
    public ConfigFORMAT getConfigFormat() {
        return CONFIG_FORMAT;
    }
    
    
    /**
     * Obtiene la extensión predeterminada para los recursos CCS de
     * configuración {@link Config}.
     * <br><br>
     * 
     * @return Extensión predeterminada para los recursos de configuración.
     * 
     * @see #setConfigExtens(String)
     * @see #getConfigFormat()
     * @see Config
     */
    public String getConfigExtens() {
        return CONFIG_EXTENS;
    }
    
    
    /**
     * Obtiene el tipo de formato predeterminado para los recursos CCS de
     * log {@link Log}.
     * <br><br>
     * 
     * @return Tipo de formato predeterminado para los recursos de log.
     * 
     * @see #setLogFormat(LogFORMAT)
     * @see #getLogExtens()
     * @see Log
     */
    public LogFORMAT getLogFormat() {
        return LOG_FORMAT;
    }
    
    
    /**
     * Obtiene la extensión predeterminada para los recursos CCS de
     * log {@link Log}.
     * <br><br>
     * 
     * @return Extensión predeterminada para los recursos de log.
     * 
     * @see #setLogExtens(String)
     * @see #getLogFormat()
     * @see Log
     */
    public String getLogExtens() {
        return LOG_EXTENS;
    }
    
    
    /**
     * Obtiene el valor de una clave del recurso de configuración predeterminado
     * del módulo.<br>
     * Si no es posible acceder al recurso o el proceso de lectura genere una
     * excepción se registra un log del error en el recurso principal de logs
     * de la aplicación {@link Kernel#setLogLine(LogTYPE, LogLEVEL, String,
     * Object, Throwable, int)} y luego se lanza la excepción
     * {@link CCSResourceAccessException}.
     * <br><br>
     * 
     * @param key Clave que se desea leer.
     * 
     * @return Valor de la clave solicitada, si no se encuentra la clave
     *         devuelve {@code null}.
     * 
     * @throws ModManagerIllegalStateException Si el módulo no se encuentra
     *         en {@link #getState()} {@code  == }{@link ModState#RUNNING} o
     *         {@link ModState#INITIALIZING}.
     * @throws IllegalArgumentException Si el parámetro {@code key} es igual a
     *         {@code null} o vacío.
     * @throws CCSResourceAccessException Si no es posible acceder al recurso
     *         o el proceso de lectura genera una excepción.
     * 
     * @see #setConfigKey(String, String)
     * @see #setConfigNewKey(String, String)
     * @see #getState()
     * @see #getConfigKeyPLAT(String, String, String)
     */
    public String getConfigKey(String key)
    throws ModManagerIllegalStateException, IllegalArgumentException, CCSResourceAccessException {
        checkInitializing();
        ParameterValidator.notNullBlank(key, "key");
        
        return modConfig.getKey(key);
    }
    
    
    /**
     * Obtiene el recurso CCS de internacionalización {@link I18n} predeterminado
     * del módulo.
     * <br><br>
     * 
     * @return Recurso CCS de internacionalización {@link I18n} predeterminado
     *         del módulo, si el módulo no fue inicializado al momento de llamar
     *         a {@code getModI18n()} el resultado será {@code null}.
     * 
     * @see #getState()
     * @see I18n
     */
    public I18n getModI18n() {
        return modI18n;
    }
    
    
/*----------------------------------------------------------------------------*/
/*                                  Checkups                                  */
/*----------------------------------------------------------------------------*/
    
    
    /**
     * Método de utilización exclusiva de la clase para chequear estado de
     * operatividad mínima ({@link #getState()} {@code  == }
     * {@link ModState#RUNNING} o {@link ModState#INITIALIZING}).
     * <br><br>
     * 
     * @throws ModManagerIllegalStateException Si el módulo no se encuentra
     *         en {@link #getState()} {@code  == }{@link ModState#RUNNING} o
     *         {@link ModState#INITIALIZING}.
     * 
     * @see #checkOperability()
     * @see #getState()
     */
    private void checkInitializing() throws ModManagerIllegalStateException {
        if (state != ModState.RUNNING && state != ModState.INITIALIZING) {
            throw new ModManagerIllegalStateException(ExcMsg.MOD_NOT_RUNNING);
        }
    }
    
    
/*----------------------------------------------------------------------------*/
/*                                                                            */
/*----------------------------------------------------------------------------*/
    
    
    /**
     * Método de utilización exclusiva de la clase para bloquear una propiedad
     * una vez inicializado el Módulo.
     * <br><br>
     * 
     * @param property Nombre de la propiedad bloqueada.
     * 
     * @throws ModManagerIllegalStateException Si se intenta modificar la propiedad
     *         y el Módulo ya fue inicializado
     *         ({@link #getState()} {@code != }{@link ModState#CREATED}).
     * 
     * @see #getState()
     * @see ModState
     */
    private void propertyBlocked(String property) throws ModManagerIllegalStateException {
        if (state != ModState.CREATED) {
            throw new ModManagerIllegalStateException(String.format(ExcMsg.MOD_ALREADY_INIT_PROP_CANNOT_MODIF,property));
        }
    }
    
    
/*----------------------------------------------------------------------------*/
/*-------------------------------- ModPLAT -----------------------------------*/
/*----------------------------------------------------------------------------*/
    
    
/*--------------------------------- Setters ----------------------------------*/
    
    
    /**
     * Implementación de plataforma del cross-cutting service (CCS) de
     * configuración {@link Config}. Establecer el valor de una clave del recurso
     * de configuración representado por los parámetros {@code resPath} y
     * {@code resName}, de no existir la clave y si {@code newKey == true} se
     * agrega al recurso.<br>
     * Si no es posible acceder al recurso o el proceso de escritura genere una
     * excepción se propaga el error a la capa CCS, que se encarga de registrar
     * el log de la excepción en el recurso principal de logs de la aplicación.
     * <br><br>
     * 
     * @param resPath Ruta del recurso de configuración.
     * @param resName Nombre del recurso de configuración.
     * @param key Clave que se desea modificar o agregar.
     * @param value Valor de la clave que se desea modificar o agregar.
     * @param newKey Habilita/deshabilita la inserción de la clave que se desea
     *        escribir, de no existir en el recurso de configuración.
     * 
     * @throws ModManagerIllegalStateException Si el módulo no se encuentra
     *         en {@link #getState()} {@code  == }{@link ModState#RUNNING}.
     * @throws IllegalArgumentException Si los parámetros {@code resPath},
     *         {@code resName} o {@code key} son igual a {@code null} o vacío.
     * @throws CCSResourceFormatException Si el formato del recurso seleccionado
     *         ({@link ConfigFORMAT}) no está soportado.
     * @throws CCSResourceAccessException Si no es posible acceder al recurso
     *         o el proceso de escritura genera una excepción.
     * 
     * @see #getConfigKeyPLAT(String, String, String)
     * @see #getConfigFormat()
     * @see #getConfigExtens()
     * @see Config
     * @see Config#setKey(String, String)
     */
    @Override
    public void setConfigKeyPLAT(String resPath, String resName, String key, String value, boolean newKey)
    throws ModManagerIllegalStateException, IllegalArgumentException, CCSResourceFormatException,
           CCSResourceAccessException {
        checkOperability();
      /*(a) Validación de Parámetros  */
        ParameterValidator.notNullBlank(resPath, "resPath");
        ParameterValidator.notNullBlank(resName, "resName");
        ParameterValidator.notNullBlank(key, "key");
        
        if (value == null) {
            value = "";
        }
      /*(a)*/
        
        resName = resName + CONFIG_EXTENS; // Agrega al Nombre del Archivo la Extensión
        
        try {
            
            switch (CONFIG_FORMAT) {
                case TEXT:
                    ConfigPLAT.setTextKey(Paths.get(resPath, resName), key, value, newKey);
                    break;
                case XML:
                    ConfigPLAT.setXmlKey(Paths.get(resPath, resName), key, value, newKey);
                    break;
                case JSON:
                    ConfigPLAT.setJsonKey(Paths.get(resPath, resName), key, value, newKey);
                    break;
                default:
                    throw new CCSResourceFormatException(String.format(ExcMsg.CCS_RES_UNSUPPORTED_FORMAT, CONFIG_FORMAT));
            }
        } catch (CCSResourceFormatException e) {
            throw e;
        } catch (Exception e) {
            throw new CCSResourceAccessException(String.format(ExcMsg.CCS_RES_CANNOT_ACCESS, resPath, resName), e);
        }
    }
    
    
    /**
     * Implementación de plataforma del cross-cutting service (CCS) de log
     * {@link Log}. Registra una línea de log en el recurso de log representado
     * por los parámetros {@code resPath} y {@code resName}.<br>
     * Si no es posible acceder al recurso o el proceso de escritura genere una
     * excepción se propaga el error a la capa CCS, que se encarga de registrar
     * el log de la excepción en el recurso principal de logs de la aplicación
     * con un único intento de retry por hilo.
     * <br><br>
     * 
     * @param resPath Ruta del recurso de registro log.
     * @param resName Nombre del recurso de registro log.
     * @param logTYPE Tipo {@link LogTYPE} que se desea registrar.
     * @param logLEVEL Nivel {@link LogLEVEL} que se desea registrar.
     * @param maxBytes Tamaño máximo en bytes del recurso de log, en el caso de
     *        que {@code numRotation > 1} determina el tamaño del recurso antes
     *        de rotarlo, el valor mínimo de {@code maxBytes} es de 256.
     * @param numRotation Número de rotaciones del recurso log, si
     *        {@code numRotation == 1} el recurso se sobrescribe cuando supera
     *        el valor de {@code maxBytes}, si {@code numRotation > 1} se genera
     *        un nuevo recurso al superar el tamaño en byte determinado por
     *        {@code maxBytes} y se los nombra con el nombre del recurso de log
     *        + "_%g" en donde "%g" es un número entre 0 y {@code numRotation},
     *        si en el nombre del recurso no se encuentra "_%g" el resultado es
     *        el mismo que si {@code numRotation == 1}.
     * @param message Mensaje principal del log que se desea registrar,
     *        se permite valor {@code null} o vacío.
     * @param addData Extiende la información relacionada con el evento,
     *        condiciones o pila que registra el log, es de carácter opcional
     *        y puede ser de valor {@code null}. {@code addData} es de tipo
     *        {@code Object} lo que lo hace de uso general y esto se debe a el
     *        método {@code toString()} de {@code Object} que permite que sea
     *        procesado de la siguiente manera:
     *        <br><br>
     *        {@code (addData == null) ? "" : addData.toString()}
     *        <br><br>
     *        esto permite la utilización de una variedad muy amplia de clases
     *        tanto propias del lenguaje como creadas por el desarrollador,
     *        tan solo debe devolver la información adecuada a este parámetro
     *        mediante el método {@code toString()}.
     * @param exception Excepción que se desea registrar, se permite valor
     *        {@code null}.
     * @param stackAdjust Ajusta el indice de la pila que se desea registrar en el
     *        log, el indice predeterminado es 0 y corresponde a la linea desde la
     *        cual se invoca este método (el punto de llamada del código que usa el
     *        log).<br>
     *        Si se delega el registro a un método propio (wrappers, listeners,
     *        procesamiento por lotes) deben incrementar el ajuste en 1 por cada
     *        nivel de delegación intermedio, para que el log registre el punto de
     *        origen real y no el método interno que ejecuta la llamada:
     *        <br><br>
     *        {@code stackAdjust == 0}: registra la linea desde donde se invoca el
     *        CCS de log.<br>
     *        {@code stackAdjust == 1}: registra la linea del método que invoco al
     *        llamador (1 nivel hacia afuera).<br>
     *        {@code stackAdjust == N}: registra N niveles mas hacia afuera en la
     *        cadena de llamadas.<br>
     *        Valores negativos se desplazan hacia marcos internos del framework y
     *        carecen de utilidad practica; el indice se recorta automáticamente a
     *        los limites de la pila disponible, por lo que un ajuste excesivo
     *        registra el marco mas externo existente sin lanzar excepción.
     * 
     * @throws ModManagerIllegalStateException Si el módulo no se encuentra
     *         en {@link #getState()} {@code  == }{@link ModState#RUNNING} o
     *         {@link ModState#INITIALIZING}.
     * @throws IllegalArgumentException Si los parámetros {@code resPath},
     *         {@code resName}, {@code logTYPE} o {@code logLEVEL} son igual a
     *         {@code null} o si {@code resPath} o {@code resName} son vacíos.
     * @throws CCSResourceFormatException Si el formato del recurso seleccionado
     *         ({@link LogFORMAT}) no está soportado.
     * @throws CCSResourceAccessException Si no es posible acceder al recurso
     *         o si el proceso de escritura genera una excepción.
     * 
     * @see #getLogFormat()
     * @see #getLogExtens()
     * @see LogTYPE
     * @see LogLEVEL
     * @see Log
     */
    @Override
    public void setLogLinePLAT(
        String resPath,
        String resName,
        LogTYPE logTYPE,
        LogLEVEL logLEVEL,
        int maxBytes,
        int numRotation,
        String message,
        Object addData,
        Throwable exception,
        int stackAdjust
    ) throws ModManagerIllegalStateException, IllegalArgumentException, CCSResourceFormatException,
             CCSResourceAccessException {
        checkInitializing();
      /*(a) Validación de Parámetros  */
        ParameterValidator.notNullBlank(resPath, "resPath");
        ParameterValidator.notNullBlank(resName, "resName");
        ParameterValidator.notNull(logTYPE, "logTYPE");
        ParameterValidator.notNull(logLEVEL, "logLEVEL");
        
        if (maxBytes < 256) {
            maxBytes = 256;
        }
        
        if (numRotation < 1) {
            numRotation = 1;
        }
      /*(a)*/
        
        if (numRotation > 1) {
            resName = resName + "_%g"; // Agrega al Nombre del Archivo la variable de rotación "%g" con "_" como separador
        }
        
        resName = resName + LOG_EXTENS; // Agrega al Nombre del Archivo la Extensión
        
        try {
            
            switch (LOG_FORMAT) {
                case CSV:
                    LogPLAT.setCsvLine(
                        Paths.get(resPath, resName),
                        logTYPE,
                        logLEVEL,
                        maxBytes,
                        numRotation,
                        message,
                        (addData == null) ? "" : addData.toString(),
                        exception,
                        stackAdjust
                    );
                    break;
                case JSON:
                    LogPLAT.setJsonLine(
                        Paths.get(resPath, resName),
                        logTYPE,
                        logLEVEL,
                        maxBytes,
                        numRotation,
                        message,
                        (addData == null) ? "" : addData.toString(),
                        exception,
                        stackAdjust
                    );
                    break;
                default:
                    throw new CCSResourceFormatException(String.format(ExcMsg.CCS_RES_UNSUPPORTED_FORMAT, LOG_FORMAT));
            }
        } catch (CCSResourceFormatException e) {
            throw e;
        } catch (Exception e) {
            throw new CCSResourceAccessException(String.format(ExcMsg.CCS_RES_CANNOT_ACCESS, resPath, resName), e);
        }
    }
    
    
/*--------------------------------- Getters ----------------------------------*/
    
    
    /**
     * Implementación de plataforma del cross-cutting service (CCS) de
     * configuración {@link Config}. Obtiene el valor de una clave del recurso
     * de configuración representado por los parámetros {@code resPath} y
     * {@code resName}.<br>
     * Si no es posible acceder al recurso o el proceso de escritura genere una
     * excepción se propaga el error a la capa CCS, que se encarga de registrar
     * el log de la excepción en el recurso principal de logs de la aplicación.
     * <br><br>
     * 
     * @param resPath Ruta del recurso de configuración.
     * @param resName Nombre del recurso de configuración.
     * @param key Clave que se desea leer.
     * 
     * @return Valor de la clave solicitada, si no se encuentra la clave
     *         devuelve {@code null}.
     * 
     * @throws ModManagerIllegalStateException Si se produce un error en la lectura
     *         del recurso y el módulo no se encuentra en {@link #getState()}
     *         {@code  == }{@link ModState#RUNNING} o {@link ModState#INITIALIZING}.
     * @throws IllegalArgumentException Si los parámetros {@code resPath},
     *         {@code resName} o {@code key} son igual a {@code null} o vacío.
     * @throws CCSResourceFormatException Si el formato del recurso seleccionado
     *         ({@link ConfigFORMAT}) no está soportado.
     * @throws CCSResourceAccessException Si no es posible acceder al recurso
     *         o el proceso de lectura genera una excepción.
     * 
     * @see #setConfigKeyPLAT(String, String, String, String, boolean)
     * @see #getConfigFormat()
     * @see #getConfigExtens()
     * @see Config
     * @see Config#getKey(String)
     */
    @Override
    public String getConfigKeyPLAT(String resPath, String resName, String key)
    throws ModManagerIllegalStateException, IllegalArgumentException, CCSResourceFormatException,
           CCSResourceAccessException {
      /*(a) Validación de Parámetros  */
        ParameterValidator.notNullBlank(resPath, "resPath");
        ParameterValidator.notNullBlank(resName, "resName");
        ParameterValidator.notNullBlank(key, "key");
      /*(a)*/
        
        resName = resName + CONFIG_EXTENS; // Agrega al Nombre del Archivo la Extensión
        
      /*(b) Lectura de Clave */
        try {
            
            switch (CONFIG_FORMAT) {
                case TEXT:
                    return ConfigPLAT.getTextKey(Paths.get(resPath, resName), key);
                case XML:
                    return ConfigPLAT.getXmlKey(Paths.get(resPath, resName), key);
                case JSON:
                    return ConfigPLAT.getJsonKey(Paths.get(resPath, resName), key);
                default:
                    throw new CCSResourceFormatException(String.format(ExcMsg.CCS_RES_UNSUPPORTED_FORMAT, CONFIG_FORMAT));
            }
        } catch (CCSResourceFormatException e) {
            throw e;
        } catch (Exception e) {
            throw new CCSResourceAccessException(String.format(ExcMsg.CCS_RES_CANNOT_ACCESS, resPath, resName), e);
        }
      /*(b)*/
    }
    
    
/*----------------------------------------------------------------------------*/
/*------------------------------- ModManager ---------------------------------*/
/*----------------------------------------------------------------------------*/
    
    
/*----------------------------- Inicializadores ------------------------------*/
    
    
    /**
     * Método para inicializar el módulo, mediante {@code initialize(String)} se le
     * pide al módulo que inicialice su entorno y componentes, conjuntamente con
     * {@link #getState()} permitir solicitar y confirmar la correcta
     * inicialización del módulo para su posterior utilización.
     * <br><br>
     * 
     * @param args Argumentos de inicialización. El parámetro forma parte de la
     *        interfaz {@link ModManager} pero su implementación es opcional y
     *        {@code PyMApp_plat_jse} no implementa argumentos de inicialización.
     * 
     * @throws KernelIllegalStateException Si el {@link Kernel} no fue inicializado
     *         {@link Kernel#getState()} {@code != }{@link KernelState#RUNNING} y
     *         {@link Kernel#getState()} {@code != }{@link KernelState#INITIALIZING}.
     * @throws ModManagerIllegalStateException Si el módulo ya fue inicializado
     *         {@link #getState()} {@code != }{@link ModState#CREATED}.
     * @throws CCSResourceAccessException Cuando no es posible acceder a alguno
     *         de los recursos CCS que requiere el módulo para su correcto
     *         funcionamiento ({@link Config}, {@link Log} y/o {@link I18n}).
     * 
     * @see #getInstance()
     * @see #getState()
     * @see #shutdown()
     */
    @Override
    public synchronized void initialize(String args)
    throws KernelIllegalStateException, ModManagerIllegalStateException, CCSResourceAccessException {
        if (KERNEL.getState() != KernelState.RUNNING && KERNEL.getState() != KernelState.INITIALIZING) { // Chequeo de Kernel con Operatividad Mínima
            throw new KernelIllegalStateException(ExcMsg.KERNEL_NOT_RUNNING);
        }
        
        if (state != ModState.CREATED) { // Evita Doble Inicialización del Módulo
            throw new ModManagerIllegalStateException(ExcMsg.MOD_ALREADY_INIT);
        }
        
        state = ModState.INITIALIZING;
        
        try {
      /*(a) Inicialización de Servicios Transversales (CCS) del Módulo */
              modConfig = new Config(MOD_CONFIG_NAME); // CCS Config
      /*(a1) CCS Log */
              String keyReading = null;
              
              LogTypeStatus modLogTypeStatus = new LogTypeStatus();
              for (LogTYPE logTYPE : LogTYPE.values()) {
                  
                  try {
                      keyReading = modConfig.getKey(CONFIG_KEY_MOD_LOG_TYPE_ACTIVE + logTYPE);
                  } catch (Exception e) {
                      keyReading = null;
                  }
                  
                  if (keyReading == null) {
                      modLogTypeStatus.setActive(logTYPE, true); // Seteo por Defecto
                  } else {
                      modLogTypeStatus.setActive(logTYPE, Boolean.parseBoolean(keyReading));
                  }
              }
              
              LogLevelStatus modLogLevelStatus = new LogLevelStatus();
              for (LogLEVEL logLEVEL : LogLEVEL.values()) {
                  
                  try {
                      keyReading = modConfig.getKey(CONFIG_KEY_MOD_LOG_LEVEL_ACTIVE + logLEVEL);
                  } catch (Exception e) {
                      keyReading = null;
                  }
                  
                  if (keyReading == null) {
                      modLogLevelStatus.setActive(logLEVEL, true); // Seteo por Defecto
                  } else {
                      modLogLevelStatus.setActive(logLEVEL, Boolean.parseBoolean(keyReading));
                  }
              }
              
              int logBYTES;
              try {
                  logBYTES = Integer.parseInt(modConfig.getKey(CONFIG_KEY_MOD_LOG_BYTES));
              } catch (Exception e) {
                  logBYTES = MOD_LOG_BYTES; // Seteo Predeterminado
              }
              
              int logROTATION;
              try {
                  logROTATION = Integer.parseInt(modConfig.getKey(CONFIG_KEY_MOD_LOG_ROTATION));
              } catch (Exception e) {
                  logROTATION = MOD_LOG_ROTATION; // Seteo Predeterminado
              }
              
              modLog = new Log(MOD_LOG_NAME, modLogTypeStatus, modLogLevelStatus, logBYTES, logROTATION);
      /*(a1)*/
      /*(a2)  CCS I18n */
              String modI18nResPath = null;
              
              modI18nResPath = modConfig.getKey(CONFIG_KEY_MOD_I18N_PATH);
              
              if (modI18nResPath == null || modI18nResPath.trim().isEmpty()) {
                  modI18nResPath = MOD_I18N_PATH; // Seteo Predeterminado
              }
              
              modI18n = new I18n(modI18nResPath);
      /*(a2)*/
      /*(a)*/
        } catch (Exception e) {
            state = ModState.CREATED;
            throw e;
        }
        
        state = ModState.RUNNING;
        
      /* (b) Log */
        StringBuilder logData = new StringBuilder()
            .append("Nombre  == ").append(MOD_NAME).append("\n")
            .append("Versión == ").append(MOD_VERSION).append("\n")
            .append("Cross-Cutting Service:").append("\n")
            .append("Recurso de Configuración == resPath: [").append(modConfig.getResPath()).append("] resName: [")
                                                             .append(modConfig.getResName()).append("]\n")
            .append("Recurso de Log           == resPath: [").append(modLog.getResPath()).append("] resName: [")
                                                             .append(modLog.getResName()).append("]\n")
            .append("Recurso i18n             == resPath: [").append(modI18n.getResPath());
        setLogLine(LogTYPE.APPLICATION, LogLEVEL.INFORMATIONAL, "Módulo Inicializado con Exito:", logData);
      /*(b)*/
    }
    
    
/*--------------------------------- Getters ----------------------------------*/
    
    
    /**
     * Permite el acceso a la instancia única Singleton del administrador del módulo.
     * {@code getModManager()} llama al constructor de creación estática
     * {@link #getInstance()} para crear una única instancia y guardarla en caché,
     * {@code getModManager()} devuelve la instancia de tipo {@link ModManager}
     * almacenada en caché, única instancia existente del administrador del módulo
     * en el entorno de la aplicación.
     * <br><br>
     * 
     * @return Instancia Singleton de tipo {@link ModManager} almacenada en
     *         caché, única instancia existente del administrador del módulo en
     *         el entorno de la aplicación que utiliza el módulo.
     * 
     * @see #getInstance()
     * @see #initialize(String)
     * @see #getState()
     * @see #shutdown()
     */
    @Override
    public ModManager getModManager() {
        return getInstance();
    }
    
    
    /**
     * Obtiene el estado del módulo ({@link ModState}).
     * <br><br>
     * 
     * @return Estado del módulo ({@link ModState}).
     * 
     * @see #initialize(String)
     * @see #shutdown()
     */
    @Override
    public synchronized ModState getState() {
        return state;
    }
    
    
    /**
     * Obtiene el nombre interno del módulo, este método puede ser llamado
     * independientemente del estado de inicialización del módulo.
     * <br><br>
     * 
     * @return Nombre interno del módulo.
     * 
     * @see #getModVERSION()
     * @see #getModDISPLAYNAME()
     * @see #getModDESCRIPTION()
      */
    @Override
    public String getModNAME() {
        return MOD_NAME;
    }
    
    
    /**
     * Obtiene la versión interna del módulo, este método puede ser llamado
     * independientemente del estado de inicialización del módulo.
     * <br><br>
     * 
     * @return Versión interna del módulo.
     * 
     * @see #getModNAME()
     * @see #getModDISPLAYNAME()
     * @see #getModDESCRIPTION()
     */
    @Override
    public String getModVERSION() {
        return MOD_VERSION;
    }
    
    
    /**
     * Obtiene el nombre público del módulo, este método implementa el CCS de
     * internacionalización {@link I18n}.
     * <br><br>
     * 
     * @return Nombre público del módulo.
     * 
     * @throws ModManagerIllegalStateException Si el módulo no se encuentra
     *         en {@link #getState()} {@code  == }{@link ModState#RUNNING}.
     * 
     * @see #getModNAME()
     * @see #getModVERSION()
     * @see #getModDESCRIPTION()
     * @see #getState()
     * @see I18n
     */
    @Override
    public String getModDISPLAYNAME() throws ModManagerIllegalStateException {
        checkOperability();
        
        return modI18n.getText("MOD_DISPLAYNAME");
    }
    
    
    /**
     * Obtiene una descripción breve del módulo, este método implementa el CCS
     * de internacionalización {@link I18n}.
     * <br><br>
     * 
     * @return Descripción breve del módulo.
     * 
     * @throws ModManagerIllegalStateException Si el módulo no se encuentra
     *         en {@link #getState()} {@code  == }{@link ModState#RUNNING}.
     * 
     * @see #getModNAME()
     * @see #getModVERSION()
     * @see #getModDISPLAYNAME()
     * @see #getState()
     * @see I18n
     */
    @Override
    public String getModDESCRIPTION() throws ModManagerIllegalStateException {
        checkOperability();
        
        return modI18n.getText("MOD_DESCRIPTION");
    }
    
    
/*----------------------------------------------------------------------------*/
    
    
    /**
     * Ejecutar la secuencia de cierre ordenado del módulo.<br>
     * De ejecutar correctamente la secuencia de cierre pasar al estado
     * {@link ModState#STOPPED}, de lo contrario debe adquirir el estado
     * {@link ModState#FAILED}.
     * <br><br>
     * 
     * @see #getState()
     * @see #initialize(String)
     */
    @Override
    public synchronized void shutdown() {
        if (!(state == ModState.INITIALIZING || state == ModState.RUNNING)) {
            return;
        }
        
        try {
            setLogLinePLAT(
                modLog.getResPath(),
                modLog.getResName(),
                LogTYPE.APPLICATION,
                LogLEVEL.INFORMATIONAL,
                modLog.getMaxBYTES(),
                modLog.getNumROTATION(),
                "Módulo Detenido con Exito.",
                null,
                null,
                0
            );
            state = ModState.STOPPED;
        } catch (Exception e) {
            state = ModState.FAILED;
        }
    }
}
