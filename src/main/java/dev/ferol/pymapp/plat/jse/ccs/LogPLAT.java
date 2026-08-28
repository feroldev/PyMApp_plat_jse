/* 
 * @(#)LogPLAT.java    1.6.2 25/07/30
 * 
 * Copyright (c) 1999-2025 OLMEDO Fernando R. {ferol.dev}
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */


package dev.ferol.pymapp.plat.jse.ccs;


import java.io.BufferedWriter;
import java.io.IOException;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;

import dev.ferol.pymapp.base.kernel.Kernel;
import dev.ferol.pymapp.base.exception.ExcMsg;
import dev.ferol.pymapp.base.validator.ParameterValidator;
import dev.ferol.pymapp.base.ccs.log.Log;
import dev.ferol.pymapp.base.ccs.log.LogLEVEL;
import dev.ferol.pymapp.base.ccs.log.LogTYPE;
import dev.ferol.pymapp.base.ccs.log.LogFormatLine;

import dev.ferol.pymapp.util.format.CSV;
import dev.ferol.pymapp.util.format.JSON;

import dev.ferol.pymapp.plat.jse.PyMApp_plat_jse;


/**
 * Implementación de plataforma del cross-cutting service (CCS) de log {@link Log},
 * agrupa funcionalidades relacionadas a la escritura en recursos CCS en la
 * plataforma Java SE.<br>
 * La clase {@code LogPLAT} es una clase de utilidad, es {@code final} y su
 * constructor es {@code private}, por ende no instanciable, sus métodos son
 * {@code static}, es en definitiva un conjunto de funciones especificas de la
 * plataforma a la que representa {@link PyMApp_plat_jse}.
 * <br><br>
 * 
 * @see #setCsvLine(Path, LogTYPE, LogLEVEL, int, int, String, String, Throwable, int)
 * @see #setJsonLine(Path, LogTYPE, LogLEVEL, int, int, String, String, Throwable, int)
 * @see PyMApp_plat_jse#setLogLinePLAT(String, String, LogTYPE, LogLEVEL, int, int, String, Object, Throwable, int)
 * @see Log
 * 
 * @author    OLMEDO Fernando R. {ferol.dev}
 * @version    1.6.2 25/07/30
 */
public final class LogPLAT {
    
    
/*----------------------------------------------------------------------------*/
/*                               Constructores                                */
/*----------------------------------------------------------------------------*/
    
    
    /**
     * Clase no instanciable.
     */
    private LogPLAT() {}
    
    
/*----------------------------------------------------------------------------*/
/*                                  Setters                                   */
/*----------------------------------------------------------------------------*/
    
    
    /**
     * Implementación de plataforma del cross-cutting service (CCS) de log
     * {@link Log}, para el formato de recurso de texto basado en CSV (Comma
     * Separated Values) variante PyMApp, que responde parcialmente al estándar
     * con 3 variaciones:
     * <br><br>
     * 1ra: se reemplaza la coma (,) por el carácter ASCII n° 124 ('|') (línea
     * simple vertical de recuadro gráfico).
     * <br><br>
     * 2da: no incluye los nombres de campos en la primera línea del archivo.
     * <br><br>
     * 3ra: los campos de texto libre ({@code message}, {@code addData},
     * {@code exceptionMessage} y {@code exceptionStack}) escapan el carácter
     * separador ({@code '|'}), la barra invertida ({@code \}) y los caracteres
     * de control de salto de línea ({@code \n}, {@code \r}, {@code \t} y
     * {@code \f}), registrando {@code \|}, {@code \\}, {@code \n}, {@code \r},
     * {@code \t} y {@code \f} respectivamente, garantizando la integridad de la
     * estructura de campos sin pérdida de información.
     * <br><br>
     * La línea de log se registra en el recurso representado por el parámetro
     * {@code resFile}.<br>
     * Se delega el manejo de las excepciones mediante la propagación.
     * <br><br>
     * 
     * @param resFile Ruta, Nombre y extensión, si corresponde, del archivo de
     *        log donde se desea registrar la línea.
     * @param logTYPE Tipo {@link LogTYPE} que se desea registrar.
     * @param logLEVEL Nivel {@link LogLEVEL} que se desea registrar.
     * @param maxBytes Tamaño máximo en bytes del archivo de log, en el caso de
     *        que {@code numRotation > 1} determina el tamaño del archivo antes
     *        de rotarlo, el valor mínimo de {@code maxBytes} es de 256.
     * @param numRotation Número de rotaciones del archivo log, si
     *        {@code numRotation == 1} el archivo se sobrescribe cuando supera
     *        el valor de {@code maxBytes}, si {@code numRotation > 1} se genera
     *        un nuevo archivo al superar el tamaño en byte determinado por
     *        {@code maxBytes} y se lo nombra con el nombre del archivo de log
     *        + "_%g" en donde "%g" es un número entre 0 y {@code numRotation},
     *        si en el nombre del archivo no se encuentra "_%g" el resultado es
     *        el mismo que si {@code numRotation == 1}.
     * @param message Mensaje principal del log que se desea registrar,
     *        se permite valor {@code null} o vacío.
     * @param addData Extiende la información relacionada con el evento,
     *        condiciones o pila que registra el log, se permite valor
     *        {@code null} o vacío.
     * @param exception Excepción que se desea registrar, se permite valor
     *        {@code null}.
     * @param stackAdjust Ajusta el indice de la pila que se desea registrar en
     *        el log, el indice predeterminado es 0 y corresponde a la linea desde
     *        la cual se invoca este método (el punto de llamada del código que
     *        usa el log).<br>
     *        Si se delega el registro a un método propio (wrappers, listeners,
     *        procesamiento por lotes) deben incrementar el ajuste en 1 por cada
     *        nivel de delegación intermedio, para que el log registre el punto
     *        de origen real y no el método interno que ejecuta la llamada:
     *        <br><br>
     *        {@code stackAdjust == 0}: registra la linea desde donde se invoca
     *        el CCS de log.<br>
     *        {@code stackAdjust == 1}: registra la linea del método que invoco
     *        al llamador (1 nivel hacia afuera).<br>
     *        {@code stackAdjust == N}: registra N niveles mas hacia afuera en
     *        la cadena de llamadas.<br>
     *        Valores negativos se desplazan hacia marcos internos del framework
     *        y carecen de utilidad practica; el indice se recorta automáticamente
     *        a los limites de la pila disponible, por lo que un ajuste excesivo
     *        registra el marco mas externo existente sin lanzar excepción.
     * 
     * @throws IllegalArgumentException Si los parámetros {@code resFile},
     *         {@code logTYPE} o {@code logLEVEL} son igual a {@code null}, o
     *         {@code resFile} es vacío.
     * @throws IOException Si se producen problemas de E/S al acceder al archivo.
     * 
     * @see #setJsonLine(Path, LogTYPE, LogLEVEL, int, int, String, String, Throwable, int)
     * @see PyMApp_plat_jse#setLogLinePLAT(String, String, LogTYPE, LogLEVEL, int, int, String, Object, Throwable, int)
     * @see LogFormatLine#format(String, String, LogTYPE, LogLEVEL, StackTraceElement, String, String, Throwable)
     * @see LogTYPE
     * @see LogLEVEL
     * @see Log
     */
    public static synchronized void setCsvLine(
        Path resFile,
        LogTYPE logTYPE,
        LogLEVEL logLEVEL,
        int maxBytes,
        int numRotation,
        String message,
        String addData,
        Throwable exception,
        int stackAdjust
    ) throws IllegalArgumentException, IOException {
      /*(a) Validación de Parámetros  */
        if (resFile == null || resFile.toString().trim().isEmpty()) {
            throw new IllegalArgumentException(String.format(ExcMsg.PARAM_NOT_NULL_EMPTY, "resFile"));
        }
        
        ParameterValidator.notNull(logTYPE, "logTYPE");
        ParameterValidator.notNull(logLEVEL, "logLEVEL");
        
        if (maxBytes < 256) {
            maxBytes = 256;
        }
        
        if (numRotation < 1) {
            numRotation = 1;
        }
        
        StackTraceElement[] stackData = Thread.currentThread().getStackTrace();
        
        stackAdjust = stackAdjust + Log.STACK_INDEX;
        if (stackAdjust <= 1) {
            stackAdjust = 1;
        } else {
            if (stackAdjust >= stackData.length) { // Evita ArrayIndexOutOfBoundsException
                stackAdjust = stackData.length - 1;
            }
        }
      /*(a)*/
        
        Path resFileActive = rotateFiles(resFile, maxBytes, numRotation); // Resolución del Archivo Activo de Escritura y Rotación
        
      /*(b) Formateo de línea de Log */
        ArrayList<String> logFields = LogFormatLine.format(
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date()),
            Kernel.getInstance().getInstanceID(),
            logTYPE,
            logLEVEL,
            stackData[stackAdjust],
            message,
            addData,
            exception
        );
        
        StringBuilder logLine = new StringBuilder();
        
        logLine.append(logFields.get(0)).append(CSV.SEPARATOR); // Timestamp
        logLine.append(logFields.get(1)).append(CSV.SEPARATOR); // InstanceID
        logLine.append(logFields.get(2)).append(CSV.SEPARATOR); // logTYPE
        logLine.append(logFields.get(3)).append(CSV.SEPARATOR); // logLEVEL
        logLine.append(logFields.get(4)).append(CSV.SEPARATOR); // stackData
        logLine.append(CSV.escaping(logFields.get(5))).append(CSV.SEPARATOR); // message
        logLine.append(CSV.escaping(logFields.get(6))).append(CSV.SEPARATOR); // addData
        logLine.append(CSV.escaping(logFields.get(7))).append(CSV.SEPARATOR); // exceptionMessage
        logLine.append(CSV.escaping(logFields.get(8))); // exceptionStack
        
        logLine.append(System.lineSeparator()); // Salto de línea del Sistema
      /*(b)*/
        
      /*(c) Escritura de línea de Log */
        try (BufferedWriter writer = Files.newBufferedWriter(
                 resFileActive,
                 StandardCharsets.UTF_8,
                 StandardOpenOption.APPEND,
                 StandardOpenOption.CREATE
            )) {
            writer.write(logLine.toString());
        }
      /*(c)*/
    }
    
    
    /**
     * Implementación de plataforma del cross-cutting service (CCS) de log
     * {@link Log}, para el formato NDJSON (Newline Delimited JSON, compatible
     * con JSON Lines).<br>
     * Cada línea de log se escribe como un objeto JSON en una sola línea, con
     * los datos mapeados a claves con nombre. Los campos de texto libre pueden
     * contener saltos de línea ({@code \n}) que se codifican mediante
     * {@link JSON#escaping(String)}.<br>
     * La línea de log se registra en el recurso representado por el parámetro
     * {@code resFile}.<br>
     * Se delega el manejo de las excepciones mediante la propagación.
     * <br><br>
     * 
     * @param resFile Ruta, Nombre y extensión, si corresponde, del archivo de
     *        log donde se desea registrar la línea.
     * @param logTYPE Tipo {@link LogTYPE} que se desea registrar.
     * @param logLEVEL Nivel {@link LogLEVEL} que se desea registrar.
     * @param maxBytes Tamaño máximo en bytes del archivo de log, en el caso de
     *        que {@code numRotation > 1} determina el tamaño del archivo antes
     *        de rotarlo, el valor mínimo de {@code maxBytes} es de 256.
     * @param numRotation Número de rotaciones del archivo log, si
     *        {@code numRotation == 1} el archivo se sobrescribe cuando supera
     *        el valor de {@code maxBytes}, si {@code numRotation > 1} se genera
     *        un nuevo archivo al superar el tamaño en byte determinado por
     *        {@code maxBytes} y se lo nombra con el nombre del archivo de log
     *        + "_%g" en donde "%g" es un número entre 0 y {@code numRotation},
     *        si en el nombre del archivo no se encuentra "_%g" el resultado es
     *        el mismo que si {@code numRotation == 1}.
     * @param message Mensaje principal del log que se desea registrar,
     *        se permite valor {@code null} o vacío.
     * @param addData Extiende la información relacionada con el evento,
     *        condiciones o pila que registra el log, se permite valor
     *        {@code null} o vacío.
     * @param exception Excepción que se desea registrar, se permite valor
     *        {@code null}.
     * @param stackAdjust Ajusta el indice de la pila que se desea registrar en
     *        el log, el indice predeterminado es 0 y corresponde a la linea
     *        desde la cual se invoca este método (el punto de llamada del código
     *        que usa el log).<br>
     *        Si se delega el registro a un método propio (wrappers, listeners,
     *        procesamiento por lotes) deben incrementar el ajuste en 1 por cada
     *        nivel de delegación intermedio, para que el log registre el punto
     *        de origen real y no el método interno que ejecuta la llamada:
     *        <br><br>
     *        {@code stackAdjust == 0}: registra la linea desde donde se invoca
     *        el CCS de log.<br>
     *        {@code stackAdjust == 1}: registra la linea del método que invoco
     *        al llamador (1 nivel hacia afuera).<br>
     *        {@code stackAdjust == N}: registra N niveles mas hacia afuera en
     *        la cadena de llamadas.<br>
     *        Valores negativos se desplazan hacia marcos internos del framework
     *        y carecen de utilidad practica; el indice se recorta automáticamente
     *        a los limites de la pila disponible, por lo que un ajuste excesivo
     *        registra el marco mas externo existente sin lanzar excepción.
     * 
     * @throws IllegalArgumentException Si los parámetros {@code resFile},
     *         {@code logTYPE} o {@code logLEVEL} son igual a {@code null}, o
     *         {@code resFile} es vacío.
     * @throws IOException Si se producen problemas de E/S al acceder al archivo.
     * 
     * @see #setCsvLine(Path, LogTYPE, LogLEVEL, int, int, String, String, Throwable, int)
     * @see PyMApp_plat_jse#setLogLinePLAT(String, String, LogTYPE, LogLEVEL, int, int, String, Object, Throwable, int)
     * @see LogTYPE
     * @see LogLEVEL
     * @see Log
     */
    public static synchronized void setJsonLine(
        Path resFile,
        LogTYPE logTYPE,
        LogLEVEL logLEVEL,
        int maxBytes,
        int numRotation,
        String message,
        String addData,
        Throwable exception,
        int stackAdjust
    ) throws IllegalArgumentException, IOException {
      /*(a) Validación de Parámetros  */
        if (resFile == null || resFile.toString().trim().isEmpty()) {
            throw new IllegalArgumentException(String.format(ExcMsg.PARAM_NOT_NULL_EMPTY, "resFile"));
        }
        
        ParameterValidator.notNull(logTYPE, "logTYPE");
        ParameterValidator.notNull(logLEVEL, "logLEVEL");
        
        if (maxBytes < 256) {
            maxBytes = 256;
        }
        
        if (numRotation < 1) {
            numRotation = 1;
        }
        
        StackTraceElement[] stackData = Thread.currentThread().getStackTrace();
        
        stackAdjust = stackAdjust + Log.STACK_INDEX;
        if (stackAdjust <= 1) {
            stackAdjust = 1;
        } else {
            if (stackAdjust >= stackData.length) { // Evita ArrayIndexOutOfBoundsException
                stackAdjust = stackData.length - 1;
            }
        }
      /*(a)*/
        
        Path resFileActive = rotateFiles(resFile, maxBytes, numRotation); // Resolución del Archivo Activo de Escritura y Rotación
        
      /*(b) Formateo de línea de Log */
        ArrayList<String> logFields = LogFormatLine.format(
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date()),
            Kernel.getInstance().getInstanceID(),
            logTYPE,
            logLEVEL,
            stackData[stackAdjust],
            message,
            addData,
            exception
        );
        
        StringBuilder logLine = new StringBuilder().append("{"); // Apertura de línea JSON
        
        logLine.append("\"Timestamp\":").append(JSON.escaping(logFields.get(0))).append(","); // Timestamp
        logLine.append("\"InstanceID\":").append(JSON.escaping(logFields.get(1))).append(","); // InstanceID
        logLine.append("\"logTYPE\":").append(JSON.escaping(logFields.get(2))).append(","); // logTYPE
        logLine.append("\"logLEVEL\":").append(JSON.escaping(logFields.get(3))).append(","); // logLEVEL
        logLine.append("\"stackData\":").append(JSON.escaping(logFields.get(4))).append(","); // stackData
        logLine.append("\"message\":").append(JSON.escaping(logFields.get(5))).append(","); // message
        logLine.append("\"addData\":").append(JSON.escaping(logFields.get(6))).append(","); // addData
        logLine.append("\"exceptionMessage\":").append(JSON.escaping(logFields.get(7))).append(","); // exceptionMessage
        logLine.append("\"exceptionStack\":").append(JSON.escaping(logFields.get(8))); // exceptionStack
        
        logLine.append("}").append(System.lineSeparator()); // Cierre de línea JSON y Salto de línea del Sistema
      /*(b)*/
        
      /*(c) Escritura de línea de Log */
        try (BufferedWriter writer = Files.newBufferedWriter(
                 resFileActive,
                 StandardCharsets.UTF_8,
                 StandardOpenOption.APPEND,
                 StandardOpenOption.CREATE
            )) {
            writer.write(logLine.toString());
        }
      /*(c)*/
    }    
    
    
/*----------------------------------------------------------------------------*/
/*                                                                            */
/*----------------------------------------------------------------------------*/
    
    
    /**
     * Implementa la rotación manual de archivos de log reemplazando el patrón
     * {@code %g} del nombre por el índice numérico correspondiente.<br>
     * El archivo activo de escritura se determina reemplazando el patrón
     * {@code %g} por {@code 0} en el nombre base. Si el archivo activo supera
     * el tamaño máximo especificado, se procede a la rotación o truncado, si
     * {@code numRotation > 1} y el nombre contiene {@code %g}, se elimina
     * el archivo de mayor índice ({@code numRotation - 1}), se incrementa el
     * índice del resto en uno y se renombra el activo (índice {@code 0}) a índice
     * {@code 1}, dejando libre el índice {@code 0} para nueva escritura, en caso
     * contrario, el archivo activo se trunca directamente.<br>
     * Retorna la ruta del archivo activo de escritura (índice {@code 0} si
     * aplica patrón, o el archivo base si no).
     * <br><br>
     *
     * @param resFile Archivo de log base que puede contener el patrón {@code %g}
     *        en su nombre, utilizado como plantilla para construir los nombres de
     *        los archivos rotados.
     * @param maxBytes Tamaño máximo en bytes del archivo de log antes de rotar
     *        o truncar.
     * @param numRotation Número máximo de archivos de rotación.
     * 
     * @return Ruta del archivo activo de escritura ({@code resFile} con
     *         {@code %g} reemplazado por {@code 0}, o {@code resFile} si no
     *         contiene el patrón).
     * 
     * @throws IOException Si se producen problemas de E/S al acceder a los archivos.
     * 
     * @see #setCsvLine(Path, LogTYPE, LogLEVEL, int, int, String, String, Throwable, int)
     * @see #setJsonLine(Path, LogTYPE, LogLEVEL, int, int, String, String, Throwable, int)
     */
    private static Path rotateFiles(Path resFile, int maxBytes, int numRotation) throws IOException {
      /*(a) Resolución del Archivo Activo de Escritura */
        String basePath = resFile.toString();
        boolean hasPattern = basePath.contains("%g");
        Path resFileActive = hasPattern ? Paths.get(basePath.replace("%g", "0")) : resFile;
      /*(a)*/
        
      /*(b) Rotación de Archivos */
        if (Files.exists(resFileActive) && Files.size(resFileActive) >= maxBytes) {
            
            if (numRotation > 1 && hasPattern) {
      /*(b1) Eliminación del Archivo de Mayor Indice */
                Path fileOldest = Paths.get(basePath.replace("%g", String.valueOf(numRotation - 1)));
                Files.deleteIfExists(fileOldest);
      /*(b1)*/
      /*(b2) Desplazamiento de Archivos */
                for (int i = numRotation - 2; i >= 1; i--) {
                    Path fileSRC = Paths.get(basePath.replace("%g", String.valueOf(i)));
                    Path fileDST = Paths.get(basePath.replace("%g", String.valueOf(i + 1)));
                    
                    if (Files.exists(fileSRC)) {
                        Files.move(fileSRC, fileDST, StandardCopyOption.REPLACE_EXISTING);
                    }
                }
      /*(b2)*/
      /*(b3) Renombrado del Archivo Activo (Indice 0) al Indice 1 */
                if (Files.exists(resFileActive)) {
                    Files.move(
                        resFileActive,
                        Paths.get(basePath.replace("%g", "1")),
                        StandardCopyOption.REPLACE_EXISTING
                    );
                }
      /*(b3)*/
            } else {
                Files.newBufferedWriter(resFileActive, StandardOpenOption.TRUNCATE_EXISTING).close();
            }
        }
      /*(b)*/
        return resFileActive;
    }
}
