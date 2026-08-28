/* 
 * @(#)ConfigPLAT.java    1.6.2 25/07/30
 * 
 * Copyright (c) 1999-2025 OLMEDO Fernando R. {ferol.dev}
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */


package dev.ferol.pymapp.plat.jse.ccs;


import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.BufferedReader;
import java.io.BufferedWriter;

import java.util.Date;

import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.charset.StandardCharsets;

import java.text.SimpleDateFormat;

import dev.ferol.pymapp.base.mod.ModState;
import dev.ferol.pymapp.base.exception.CCSResourceAccessException;
import dev.ferol.pymapp.base.exception.CCSResourceFormatException;
import dev.ferol.pymapp.base.exception.ExcMsg;
import dev.ferol.pymapp.base.validator.ParameterValidator;
import dev.ferol.pymapp.base.ccs.config.Config;
import dev.ferol.pymapp.base.ccs.log.LogLEVEL;
import dev.ferol.pymapp.base.ccs.log.LogTYPE;

import dev.ferol.pymapp.util.format.Properties;
import dev.ferol.pymapp.util.format.XML;
import dev.ferol.pymapp.util.format.JSON;

import dev.ferol.pymapp.plat.jse.PyMApp_plat_jse;


/**
 * Implementación de plataforma del cross-cutting service (CCS) de configuración
 * {@link Config}, agrupa funcionalidades relacionadas a la lectura y escritura
 * en recursos CCS en la plataforma Java SE.<br>
 * La clase {@code ConfigPLAT} es una clase de utilidad, es {@code final} y su
 * constructor es {@code private}, por ende no instanciable, sus métodos son
 * {@code static}, es en definitiva un conjunto de funciones especificas de la
 * plataforma a la que representa {@link PyMApp_plat_jse}.
 * <br><br>
 * 
 * @see #setTextKey(Path, String, String, boolean)
 * @see #setXmlKey(Path, String, String, boolean)
 * @see #setJsonKey(Path, String, String, boolean)
 * @see #getTextKey(Path, String)
 * @see #getXmlKey(Path, String)
 * @see #getJsonKey(Path, String)
 * @see PyMApp_plat_jse#setConfigKeyPLAT(String, String, String, String, boolean)
 * @see PyMApp_plat_jse#getConfigKeyPLAT(String, String, String)
 * @see Config
 * 
 * @author    OLMEDO Fernando R. {ferol.dev}
 * @version    1.6.2 25/07/30
 */
public final class ConfigPLAT {
    private static final PyMApp_plat_jse modManager = PyMApp_plat_jse.getInstance(); // Manejador del Módulo (singleton)
    
    
/*----------------------------------------------------------------------------*/
/*                               Constructores                                */
/*----------------------------------------------------------------------------*/
    
    
    /**
     * Clase no instanciable.
     */
    private ConfigPLAT() {}
    
    
/*----------------------------------------------------------------------------*/
/*                                  Setters                                   */
/*----------------------------------------------------------------------------*/
    
    
    /**
     * Implementación de plataforma del cross-cutting service (CCS) de
     * configuración {@link Config}. Establecer el valor de una clave en el
     * recurso de configuración representado por el parámetro {@code resFile},
     * mas específicamente en archivos de texto con formato texto plano tipo
     * properties básico.<br>
     * Se delega el manejo de las excepciones mediante la propagación.
     * <br><br>
     * 
     * @param resFile Ruta, Nombre y extensión, si corresponde, del archivo de
     *        configuración.
     * @param key Clave que se desea modificar.
     * @param value Valor de la clave que se desea modificar, se permite valor
     *        {@code null} o {@code ""}, en ambos casos se trata como cadena
     *        vacia ({@code ""}).
     * @param newKey Habilita/deshabilita la inserción de la clave que se desea
     *        escribir, de no existir, en el archivo de configuración.
     * 
     * @throws IllegalArgumentException Si los parámetros {@code resFile} o
     *         {@code key} son igual a {@code null} o vacíos.
     * @throws CCSResourceAccessException Si el archivo {@code resFile} no
     *         existe o es inaccesible.
     * @throws IOException Si se produce un error en los procesos de lectura o
     *         escritura del archivo, o si se produce un error en el proceso de
     *         reemplazo del archivo temporal.
     * 
     * @see #setXmlKey(Path, String, String, boolean)
     * @see #setJsonKey(Path, String, String, boolean)
     * @see #getTextKey(Path, String)
     * @see PyMApp_plat_jse#setConfigKeyPLAT(String, String, String, String, boolean)
     * @see PyMApp_plat_jse#getConfigKeyPLAT(String, String, String)
     * @see Config
     */
    public static synchronized void setTextKey(Path resFile, String key, String value, boolean newKey)
    throws IllegalArgumentException, CCSResourceAccessException, IOException {
      /*(a) Validación de Parámetros  */
        resFileValidator(resFile);
        ParameterValidator.notNullBlank(key, "key");
        
        if (value == null) {
            value = "";
        }
      /*(a)*/
        
        Path tmpFile = getTmpFile(resFile); // Archivo Temporal con Sufijo único (yyMMddHHmmssSSS)
        
      /*(b) Lectura y Escritura de Claves */
        try (BufferedReader resFileBuff = Files.newBufferedReader(resFile, StandardCharsets.UTF_8);
             BufferedWriter tmpFileBuff = Files.newBufferedWriter(tmpFile, StandardCharsets.UTF_8)) {
            
            boolean keyUpdated = false;
            boolean skipContinuation = false; // Descartar Líneas de Continuación de la Entrada Reemplazada
            String lineReader;
            
            while ((lineReader = resFileBuff.readLine()) != null) {
                
                if (skipContinuation) { // Descarta Fragmentos de la Línea Continuada
                    
                    if (!hasOpenContinuation(lineReader)) {
                        skipContinuation = false;
                    }
                    continue;
                }
                
                String trimmed = lineReader.trim();
                if (!trimmed.isEmpty() && !trimmed.startsWith("#") && !trimmed.startsWith("!")) { // Ignora Líneas Vacías y Comentarios ("#" y "!")
                    
                    String rawKey = parseTextRawKey(lineReader); // Clave Escapada de la Línea
                    if (rawKey != null && key.equals(Properties.unescaping(rawKey))) { // Compara la Clave de la Línea con la Solicitada
                        skipContinuation = hasOpenContinuation(lineReader); // Si la Línea Original se Continuaba, se Descartan las Siguientes
                        lineReader = Properties.escapingKey(key) + "=" + Properties.escaping(value); // Línea Reemplazada con Escape Completo
                        keyUpdated = true;
                    }
                }
                
                tmpFileBuff.write(lineReader);
                tmpFileBuff.newLine();
            }
            
            if (!keyUpdated && newKey) { // Si No se Encontró la Clave y se Debe Agregar (newKey == true)
                tmpFileBuff.write(Properties.escapingKey(key) + "=" + Properties.escaping(value));
                tmpFileBuff.newLine();
            }
        } catch (IOException e) {
            Files.deleteIfExists(tmpFile); // Limpieza del Archivo Temporal en Caso de Error
            throw e;
        }
      /*(b)*/
        
        fileReplacement(resFile, tmpFile); // Reemplazo de Archivos
    }
    
    
    /**
     * Implementación de plataforma del cross-cutting service (CCS) de
     * configuración {@link Config}. Establecer el valor de una clave en el
     * recurso de configuración representado por el parámetro {@code resFile},
     * mas específicamente en archivos de configuración con formato XML de
     * propiedades (DTD de {@code java.util.Properties}).<br>
     * Se preservan los comentarios XML ({@code <!-- ... -->}) y los elementos
     * {@code <comment>}.<br>
     * Cuando se agrega una clave nueva se utiliza la indentación de la primera
     * entrada {@code <entry>} existente como referencia para respetar el formato
     * del archivo; si el archivo no contiene entradas se utiliza una indentación
     * de 8 espacios por defecto.<br>
     * Se delega el manejo de las excepciones mediante la propagación.
     * <br><br>
     *
     * @param resFile Ruta, Nombre y extensión, si corresponde, del archivo de
     *        configuración.
     * @param key Clave que se desea modificar.
     * @param value Valor de la clave que se desea modificar, se permite valor
     *        {@code null} o {@code ""}, en ambos casos se trata como cadena
     *        vacia ({@code ""}).
     * @param newKey Habilita/deshabilita la inserción de la clave que se desea
     *        escribir, de no existir, en el archivo de configuración.
     *
     * @throws IllegalArgumentException Si los parámetros {@code resFile} o
     *         {@code key} son igual a {@code null} o vacíos.
     * @throws CCSResourceFormatException Si el archivo {@code resFile} está
     *         vacío y carece de la estructura raíz obligatoria del formato XML.
     * @throws CCSResourceAccessException Si el archivo {@code resFile} no
     *         existe o es inaccesible.
     * @throws IOException Si se produce un error en los procesos de lectura o
     *         escritura del archivo, o si se produce un error en el proceso de
     *         reemplazo del archivo temporal.
     *
     * @see #setTextKey(Path, String, String, boolean)
     * @see #setJsonKey(Path, String, String, boolean)
     * @see #getXmlKey(Path, String)
     * @see PyMApp_plat_jse#setConfigKeyPLAT(String, String, String, String, boolean)
     * @see PyMApp_plat_jse#getConfigKeyPLAT(String, String, String)
     * @see Config
     */
    public static synchronized void setXmlKey(Path resFile, String key, String value, boolean newKey)
    throws IllegalArgumentException, CCSResourceFormatException, CCSResourceAccessException, IOException {
      /*(a) Validación de Parámetros  */
        resFileValidator(resFile);
        
        if (Files.size(resFile) == 0) { // Recurso XML Vacío: Estructura Raíz Obligatoria, Formato No Válido
            throw new CCSResourceFormatException(String.format(
                ExcMsg.CCS_RES_INVALID_FORMAT,
                resFile.getParent(),
                resFile.getFileName()
            ));
        }
        
        ParameterValidator.notNullBlank(key, "key");
        
        if (value == null) {
            value = "";
        }
      /*(a)*/
        
        Path tmpFile = getTmpFile(resFile); // Archivo Temporal con Sufijo único (yyMMddHHmmssSSS)
        
      /*(b) Lectura y Escritura de Claves */
        try (BufferedReader resFileBuff = Files.newBufferedReader(resFile, StandardCharsets.UTF_8);
             BufferedWriter tmpFileBuff = Files.newBufferedWriter(tmpFile, StandardCharsets.UTF_8)) {
            
            boolean keyUpdated = false;
            boolean inEntry = false;
            String entryKeyFound = null;
            String entryIndent = "";
            String entryIndentRef = null;
            String lineReader;
            
            while ((lineReader = resFileBuff.readLine()) != null) {
                
                if (inEntry) {
                    
                    if (lineReader.contains("</entry>")) { // Busca Cierre de "entry" Multi-Línea
                        
                        if (entryKeyFound != null && entryKeyFound.equals(key)) { // Reemplaza Bloque Multi-Línea por "entry" de una Línea
                            tmpFileBuff.write(
                                entryIndent +
                                "<entry key=\"" +
                                XML.escapingAttr(key) +
                                "\">" +
                                XML.escaping(value) +
                                "</entry>"
                            );
                            tmpFileBuff.newLine();
                            keyUpdated = true;
                        } else {  // Pasa el Bloque Completo (ya se escribió la apertura, solo escribe la línea de cierre)
                            tmpFileBuff.write(lineReader);
                            tmpFileBuff.newLine();
                        }
                        inEntry = false;
                        entryKeyFound = null;
                        entryIndent = "";
                        
                    } else if (entryKeyFound != null && entryKeyFound.equals(key)) { // Salta Líneas Internas del "entry" que se va a Reemplazar
                        continue;
                    } else { // Pasa Líneas Internas de "entry"s que no Coinciden
                        tmpFileBuff.write(lineReader);
                        tmpFileBuff.newLine();
                    }
                    continue;
                }
                
                String trimmed = lineReader.trim();
                if (trimmed.startsWith("<entry")) { // Detecta Apertura de <entry key="...">
                    String attrMatch = "key=\"";
                    
                    int keyStart = trimmed.indexOf(attrMatch);
                    if (keyStart != -1) {
                        keyStart = keyStart + attrMatch.length();
                        
                        int keyEnd = trimmed.indexOf("\"", keyStart);
                        if (keyEnd != -1) {
                            String fileKey = trimmed.substring(keyStart, keyEnd);
                            
                            fileKey = XML.unescaping(fileKey);
                            
                            int indentLen = lineReader.length() - trimmed.length();
                            String lineIndent = lineReader.substring(0, indentLen); // Captura Indentación de la Línea Original
                            
                            if (entryIndentRef == null) { // Captura Primera Indentación para Claves Nuevas
                                entryIndentRef = lineIndent;
                            }
                            
                            if (trimmed.contains("</entry>")) { // Es Single-Line? (contiene </entry> o termina con />)
                                
                                if (fileKey.equals(key)) { // Reemplaza la Línea Completa
                                    tmpFileBuff.write(
                                        lineIndent +
                                        "<entry key=\"" +
                                        XML.escapingAttr(key) +
                                        "\">" +
                                        XML.escaping(value) +
                                        "</entry>"
                                    );
                                    tmpFileBuff.newLine();
                                    keyUpdated = true;
                                } else {
                                    tmpFileBuff.write(lineReader);
                                    tmpFileBuff.newLine();
                                }
                                
                            } else if (trimmed.endsWith("/>")) { // Self-Closing, Tratar como si Tuviera Valor Vacío
                                
                                if (fileKey.equals(key)) {
                                    tmpFileBuff.write(
                                        lineIndent +
                                        "<entry key=\"" +
                                        XML.escapingAttr(key) +
                                        "\">" +
                                        XML.escaping(value) +
                                        "</entry>"
                                    );
                                    tmpFileBuff.newLine();
                                    keyUpdated = true;
                                } else {
                                    tmpFileBuff.write(lineReader);
                                    tmpFileBuff.newLine();
                                }
                            } else { // Multi-Línea: Espera Cierre en Líneas Posteriores
                                inEntry = true;
                                entryKeyFound = fileKey;
                                entryIndent = lineIndent;
                                
                                if (!fileKey.equals(key)) { // Escribe la Línea de Apertura para "entry"s que no se Modifican
                                    tmpFileBuff.write(lineReader);
                                    tmpFileBuff.newLine();
                                }
                            }
                            continue;
                        }
                    }
                }
                
                if (trimmed.equals("</properties>")) { // Detecta Cierre del Bloque "properties"
                    
                    if (!keyUpdated && newKey) { // Si No se Encontró la Clave y se Debe Agregar (newKey == true)
                        tmpFileBuff.write(
                            (entryIndentRef != null ? entryIndentRef : "        ") + // Referencia de Indentación o 8 Espacios por Defecto
                            "<entry key=\"" +
                            XML.escapingAttr(key) +
                            "\">" +
                            XML.escaping(value) +
                            "</entry>"
                        );
                        tmpFileBuff.newLine();
                    }
                    tmpFileBuff.write(lineReader);
                    tmpFileBuff.newLine();
                    continue;
                }
                tmpFileBuff.write(lineReader); // Líneas que no son "entry" ni "properties" ("comment", etc..)
                tmpFileBuff.newLine();
            }
        } catch (IOException e) {
            Files.deleteIfExists(tmpFile); // Limpieza del Archivo Temporal en Caso de Error
            throw e;
        }
      /*(b)*/
        
        fileReplacement(resFile, tmpFile); // Reemplazo de Archivos
    }
    
    
    /**
     * Implementación de plataforma del cross-cutting service (CCS) de
     * configuración {@link Config}. Establecer el valor de una clave en el
     * recurso de configuración representado por el parámetro {@code resFile},
     * mas específicamente en archivos de configuración con formato JSON
     * (objeto plano clave-valor).
     * <br><br>
     * Restricciones de formato:
     * <br><br>
     * El recurso es un objeto JSON plano con una entrada por línea. Las llaves
     * de apertura ({@code {}}) y cierre ({@code }}) pueden compartir línea con
     * una entrada (p. ej. {@code {"clave": "valor"}} o
     * {@code {"clave": "valor",}}) y el proceso las normaliza a líneas propias.
     * <br><br>
     * No se soporta más de una entrada por línea, ni objetos anidados, ni
     * arreglos; ante estas estructuras, o ante cadenas de texto sin cerrar, se
     * lanza {@link CCSResourceFormatException}.
     * <br><br>
     * Unicidad de claves:<br>
     * Las claves son únicas: si la clave ya existe en el recurso se actualiza
     * su valor en el lugar con el texto indicado, sin importar si el valor
     * existente es una cadena de texto JSON o un valor no textual (numérico,
     * booleano o {@code null}); no se permite la existencia de dos claves con
     * el mismo nombre. El parámetro {@code newKey} solo determina el
     * comportamiento cuando la clave no existe en el recurso.<br>
     * Los valores no textuales de claves que no se actualizan se conservan tal
     * cual en el recurso.
     * <br><br>
     * Se preserva el formato original del archivo (indentación, comas finales),
     * modificando la línea que contiene la clave solicitada o, al agregar una
     * clave nueva, la coma final de la entrada previa cuando sea necesaria.<br>
     * Se delega el manejo de las excepciones mediante la propagación.
     * <br><br>
     *
     * @param resFile Ruta, Nombre y extensión, si corresponde, del archivo de
     *        configuración.
     * @param key Clave que se desea modificar.
     * @param value Valor de la clave que se desea modificar, se permite valor
     *        {@code null} o {@code ""}, en ambos casos se trata como cadena
     *        vacia ({@code ""}).
     * @param newKey Habilita/deshabilita la inserción de la clave que se desea
     *        escribir, de no existir, en el archivo de configuración.
     *
     * @throws IllegalArgumentException Si los parámetros {@code resFile} o
     *         {@code key} son igual a {@code null} o vacíos.
     * @throws CCSResourceFormatException Si el archivo {@code resFile} está
     *         vacío y carece de la estructura raíz obligatoria del formato
     *         JSON, o si alguna línea del recurso posee más de una entrada,
     *         llaves o corchetes fuera de cadenas (objetos anidados, arreglos),
     *         cadenas de texto sin cerrar o contenido no conforme al formato.
     * @throws CCSResourceAccessException Si el archivo {@code resFile} no
     *         existe o es inaccesible.
     * @throws IOException Si se produce un error en los procesos de lectura o
     *         escritura del archivo, o si se produce un error en el proceso de
     *         reemplazo del archivo temporal.
     *
     * @see #setTextKey(Path, String, String, boolean)
     * @see #setXmlKey(Path, String, String, boolean)
     * @see #getJsonKey(Path, String)
     * @see PyMApp_plat_jse#setConfigKeyPLAT(String, String, String, String, boolean)
     * @see PyMApp_plat_jse#getConfigKeyPLAT(String, String, String)
     * @see Config
     */
    public static synchronized void setJsonKey(Path resFile, String key, String value, boolean newKey)
    throws IllegalArgumentException, CCSResourceFormatException, CCSResourceAccessException, IOException {
      /*(a) Validación de Parámetros  */
        resFileValidator(resFile);

        if (Files.size(resFile) == 0) { // Recurso JSON Vacío: Estructura Raíz Obligatoria, Formato No Válido
            throw new CCSResourceFormatException(String.format(
                ExcMsg.CCS_RES_INVALID_FORMAT,
                resFile.getParent(),
                resFile.getFileName()
            ));
        }

        ParameterValidator.notNullBlank(key, "key");

        if (value == null) {
            value = "";
        }
      /*(a)*/
        
        Path tmpFile = getTmpFile(resFile); // Archivo Temporal con Sufijo único (yyMMddHHmmssSSS)
        
      /*(b) Lectura y Escritura de Claves */
        try (BufferedReader resFileBuff = Files.newBufferedReader(resFile, StandardCharsets.UTF_8);
             BufferedWriter tmpFileBuff = Files.newBufferedWriter(tmpFile, StandardCharsets.UTF_8)) {
            
            boolean keyUpdated = false;
            String closingBraceLine = null;
            String heldLine = null;
            String detectedIndent = null;
            
            String lineReader;
            while ((lineReader = resFileBuff.readLine()) != null) {
                
                String trimmed = lineReader.trim();
                String indent = lineReader.substring(0, lineReader.length() - trimmed.length()); // Indentación de la Línea Original
                
                if (trimmed.isEmpty()) { // Línea en Blanco: Pasa Tal Cual
                    if (heldLine != null) {
                        tmpFileBuff.write(heldLine);
                        tmpFileBuff.newLine();
                        heldLine = null;
                    }
                    tmpFileBuff.write(lineReader);
                    tmpFileBuff.newLine();
                    continue;
                }
                
                if (trimmed.equals("}") || trimmed.equals("},")) { // Detección de Línea de Cierre de Objeto JSON
                    closingBraceLine = lineReader;
                    continue; // La Entrada Retenida Previa Espera la Posible Coma de la Inserción
                }
                
      /*(b1) Normalización de Llaves: "{" y "}" en Líneas Propias */
                String openBrace = null;
                String closeBrace = null;
                
                String content = trimmed;
                if (content.startsWith("{")) { // Llave de Apertura Compartida con la Entrada
                    openBrace = "{";
                    content = content.substring(1).trim();
                }
                
                if (content.endsWith("},")) { // Llave de Cierre Compartida con la Entrada (con Coma)
                    closeBrace = "},";
                    content = content.substring(0, content.length() - 2).trim();
                } else if (content.endsWith("}")) { // Llave de Cierre Compartida con la Entrada
                    closeBrace = "}";
                    content = content.substring(0, content.length() - 1).trim();
                }
      /*(b1)*/
                
      /*(b2) Validación de Formato de la Línea */
                if (!content.isEmpty()) {
                    
                    if (hasBraceOutsideStrings(content)) { // Llaves o Corchetes Fuera de Cadenas: Anidados, Arreglos o Posición Inválida
                        throw new CCSResourceFormatException(String.format(
                            ExcMsg.CCS_RES_INVALID_FORMAT,
                            resFile.getParent(),
                            resFile.getFileName()
                        ));
                    }
                    
                    int entryCount = countJsonEntries(content);
                    if (entryCount != 1 || content.charAt(0) != '"') { // Solo se Soporta una Entrada por Línea
                        throw new CCSResourceFormatException(String.format(
                            ExcMsg.CCS_RES_INVALID_FORMAT,
                            resFile.getParent(),
                            resFile.getFileName()
                        ));
                    }
                }
      /*(b2)*/
                
                if (heldLine != null) { // Libera la Entrada Retenida (se Confirma que Existe una Línea Posterior)
                    tmpFileBuff.write(heldLine);
                    tmpFileBuff.newLine();
                    heldLine = null;
                }
                
                if (openBrace != null) { // Llave de Apertura en Línea Propia
                    tmpFileBuff.write(indent + openBrace);
                    tmpFileBuff.newLine();
                }
                
                if (!content.isEmpty()) { // Procesa la Entrada: Posible Actualización de la Clave
                    String lineToEmit = indent + content; // Por Defecto, la Línea se Emite sin Modificar
                    
                    int keyStart = 1;
                    
                    int keyEnd = parseJsonStringEnd(content, keyStart - 1); // Indice Posterior a la Comilla de Cierre (Salta las Escapadas)
                    if (keyEnd != -1) {
                        String fileKey = content.substring(keyStart, keyEnd - 1);
                        fileKey = JSON.unescaping(fileKey); // Unescapa por si Contiene \"
                        
                        if (detectedIndent == null) { // Toma la Primera Indentación Encontrada como Referencia
                            detectedIndent = indent;
                        }
                        
                        int colonPos = content.indexOf(":", keyEnd);
                        if (colonPos != -1 && fileKey.equals(key)) {
                            
                            int valueStart = content.indexOf("\"", colonPos + 1);
                            if (valueStart != -1) {
                                
                                int valueEnd = parseJsonStringEnd(content, valueStart);
                                if (valueEnd == -1) { // Cadena de Valor sin Cerrar: Formato No Válido
                                    throw new CCSResourceFormatException(String.format(
                                        ExcMsg.CCS_RES_INVALID_FORMAT,
                                        resFile.getParent(),
                                        resFile.getFileName()
                                    ));
                                }
                                
                                String trailing = content.substring(valueEnd); // Sufijo Original (",", "}" o Vacío)
                                lineToEmit = indent + JSON.escaping(key) + ": " + JSON.escaping(value) + trailing;
                                keyUpdated = true;
                            } else { // Valor No Textual (Numérico, Booleano o null): Reemplazo en el Lugar
                                int valueEnd = content.length();
                                
                                for (int i = colonPos + 1; i < content.length(); i++) {
                                    if (content.charAt(i) == ',' || content.charAt(i) == '}') {
                                        valueEnd = i;
                                        break;
                                    }
                                }
                                
                                String token = content.substring(colonPos + 1, valueEnd).trim();
                                if (token.isEmpty()) { // Línea Sin Valor Tras el Separador: Formato No Válido
                                    throw new CCSResourceFormatException(String.format(
                                        ExcMsg.CCS_RES_INVALID_FORMAT,
                                        resFile.getParent(),
                                        resFile.getFileName()
                                    ));
                                }
                                
                                String trailing = content.substring(valueEnd).trim(); // Sufijo Original (",", "}" o Vacío)
                                lineToEmit = indent + JSON.escaping(key) + ": " + JSON.escaping(value) + trailing;
                                keyUpdated = true;
                            }
                        }
                    }
                    
                    heldLine = lineToEmit; // Retiene la Entrada hasta Confirmar si es o no la Última del Objeto
                }
                
                if (closeBrace != null) { // Llave de Cierre Diferida al Final del Objeto
                    closingBraceLine = indent + closeBrace;
                }
            }
            
      /*(b3) Inserción de Clave Nueva o Liberación de la Última Entrada */
            if (!keyUpdated && newKey) { // Si No se Encontró la Clave y se Debe Agregar (newKey == true)
                
                if (heldLine != null) { // Existe una Entrada Previa: Asegura la Coma de Separación
                    String heldTrim = heldLine.trim();
                    
                    if (!heldTrim.endsWith(",")) {
                        tmpFileBuff.write(heldLine + ",");
                    } else { // Entrada con Coma ya Presente
                        tmpFileBuff.write(heldLine);
                    }
                    tmpFileBuff.newLine();
                }
                
                String newEntryIndent = (detectedIndent != null) ? detectedIndent : "    "; // Referencia o 4 Espacios por Defecto
                tmpFileBuff.write(newEntryIndent + JSON.escaping(key) + ": " + JSON.escaping(value)); // Última Entrada: Sin Coma Final
                tmpFileBuff.newLine();
                
            } else if (heldLine != null) { // No se Agrega Clave Nueva: Libera la Última Entrada Retenida sin Modificar
                tmpFileBuff.write(heldLine);
                tmpFileBuff.newLine();
            }
      /*(b3)*/
            
            if (closingBraceLine != null) {
                tmpFileBuff.write(closingBraceLine);
                tmpFileBuff.newLine();
            }
        } catch (IOException e) {
            Files.deleteIfExists(tmpFile); // Limpieza del Archivo Temporal en Caso de Error
            throw e;
        }
      /*(b)*/
        
        fileReplacement(resFile, tmpFile); // Reemplazo de Archivos
    }
    
    
/*----------------------------------------------------------------------------*/
/*                                  Getters                                   */
/*----------------------------------------------------------------------------*/
    
    
    /**
     * Implementación de plataforma del cross-cutting service (CCS) de
     * configuración {@link Config}. Obtiene el valor de una clave en el
     * recurso de configuración representado por el parámetro {@code resFile},
     * mas específicamente en archivos de texto plano de tipo properties básico.<br>
     * Se delega el manejo de las excepciones mediante la propagación.
     * <br><br>
     * 
     * @param resFile Ruta, Nombre y extensión, si corresponde, del archivo de
     *        configuración.
     * @param key Clave que se desea leer.
     * 
     * @return Valor de la clave solicitada, si no se encuentra la clave
     *         devuelve {@code null}.
     * 
     * @throws IllegalArgumentException Si los parámetros {@code resFile} o
     *         {@code key} son igual a {@code null} o vacíos.
     * @throws CCSResourceAccessException Si el archivo {@code resFile} no
     *         existe o es inaccesible.
     * @throws IOException Si se produce un error en los procesos de lectura del
     *         archivo.
     * 
     * @see #getXmlKey(Path, String)
     * @see #getJsonKey(Path, String)
     * @see #setTextKey(Path, String, String, boolean)
     * @see PyMApp_plat_jse#getConfigKeyPLAT(String, String, String)
     * @see PyMApp_plat_jse#setConfigKeyPLAT(String, String, String, String, boolean)
     * @see Config
     */
    public static synchronized String getTextKey(Path resFile, String key)
    throws IllegalArgumentException, CCSResourceAccessException, IOException {
      /*(a) Validación de Parámetros  */
        resFileValidator(resFile);
        ParameterValidator.notNullBlank(key, "key");
      /*(a)*/
        
      /*(b) Lectura de Clave */
        try (Reader resFileBuff = Files.newBufferedReader(resFile, StandardCharsets.UTF_8)) {
            java.util.Properties resFileProps = new java.util.Properties();
            resFileProps.load(resFileBuff);
            
            return resFileProps.getProperty(key);
        }
      /*(b)*/
    }
    
    
    /**
     * Implementación de plataforma del cross-cutting service (CCS) de
     * configuración {@link Config}. Obtiene el valor de una clave en el
     * recurso de configuración representado por el parámetro {@code resFile},
     * mas específicamente en archivos de configuración con formato XML de
     * propiedades (DTD de {@code java.util.Properties}).<br>
     * Se delega el manejo de las excepciones mediante la propagación.
     * <br><br>
     * 
     * @param resFile Ruta, Nombre y extensión, si corresponde, del archivo de
     *        configuración.
     * @param key Clave que se desea leer.
     * 
     * @return Valor de la clave solicitada, si no se encuentra la clave
     *         devuelve {@code null}.
     * 
     * @throws IllegalArgumentException Si los parámetros {@code resFile} o
     *         {@code key} son igual a {@code null} o vacíos.
     * @throws CCSResourceAccessException Si el archivo {@code resFile} no
     *         existe o es inaccesible.
     * @throws IOException Si se produce un error en los procesos de lectura del
     *         archivo.
     *
     * @see #setXmlKey(Path, String, String, boolean)
     * @see #getJsonKey(Path, String)
     * @see #getTextKey(Path, String)
     * @see PyMApp_plat_jse#getConfigKeyPLAT(String, String, String)
     * @see PyMApp_plat_jse#setConfigKeyPLAT(String, String, String, String, boolean)
     * @see Config
     */
    public static synchronized String getXmlKey(Path resFile, String key)
    throws IllegalArgumentException, CCSResourceAccessException, IOException {
      /*(a) Validación de Parámetros  */
        resFileValidator(resFile);
        ParameterValidator.notNullBlank(key, "key");
      /*(a)*/
        
      /*(b) Lectura de Clave */
        try (InputStream resFileIS = Files.newInputStream(resFile)) {
            java.util.Properties resFileProps = new java.util.Properties();
            resFileProps.loadFromXML(resFileIS);
            
            return resFileProps.getProperty(key);
        }
      /*(b)*/
    }
    
    
    /**
     * Implementación de plataforma del cross-cutting service (CCS) de
     * configuración {@link Config}. Obtiene el valor de una clave en el
     * recurso de configuración representado por el parámetro {@code resFile},
     * mas específicamente en archivos de configuración con formato JSON
     * (objeto plano clave-valor).<br>
     * Se delega el manejo de las excepciones mediante la propagación.
     * <br><br>
     *
     * @param resFile Ruta, Nombre y extensión, si corresponde, del archivo de
     *        configuración.
     * @param key Clave que se desea leer.
     *
     * @return Valor de la clave solicitada, si no se encuentra la clave o si
     *         su valor no es una cadena de texto JSON (p. ej. numérico,
     *         booleano o {@code null}) devuelve {@code null}.
     *
     * @throws IllegalArgumentException Si los parámetros {@code resFile} o
     *         {@code key} son igual a {@code null} o vacíos.
     * @throws CCSResourceAccessException Si el archivo {@code resFile} no
     *         existe o es inaccesible.
     * @throws IOException Si se produce un error en los procesos de lectura del
     *         archivo.
     *
     * @see #setJsonKey(Path, String, String, boolean)
     * @see #getXmlKey(Path, String)
     * @see #getTextKey(Path, String)
     * @see PyMApp_plat_jse#getConfigKeyPLAT(String, String, String)
     * @see PyMApp_plat_jse#setConfigKeyPLAT(String, String, String, String, boolean)
     * @see Config
     */
    public static synchronized String getJsonKey(Path resFile, String key)
    throws IllegalArgumentException, CCSResourceAccessException, IOException {
      /*(a) Validación de Parámetros  */
        resFileValidator(resFile);
        ParameterValidator.notNullBlank(key, "key");
      /*(a)*/
        
      /*(b) Lectura de Clave */
        try (BufferedReader resFileBuff = Files.newBufferedReader(resFile, StandardCharsets.UTF_8)) {
            
            String lineReader;
            
            while ((lineReader = resFileBuff.readLine()) != null) {
                String trimmed = lineReader.trim();
                
                if (trimmed.startsWith("\"")) { // Busca Patrón: "key": "..."
                    int keyStart = 1;
                    int keyEnd = parseJsonStringEnd(trimmed, keyStart - 1); // Indice Posterior a la Comilla de Cierre (Salta las Escapadas)
                    
                    if (keyEnd != -1) {
                        String fileKey = trimmed.substring(keyStart, keyEnd - 1);
                        fileKey = JSON.unescaping(fileKey);
                        
                        if (fileKey.equals(key)) {
                            int colonPos = trimmed.indexOf(":", keyEnd);
                            
                            if (colonPos != -1) {
                                int valueStart = trimmed.indexOf("\"", colonPos + 1);
                                
                                if (valueStart != -1) {
                                    int valueEnd = parseJsonStringEnd(trimmed, valueStart);
                                    
                                    if (valueEnd != -1) {
                                        String rawValue = trimmed.substring(valueStart + 1, valueEnd - 1);
                                        
                                        return JSON.unescaping(rawValue);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
      /*(b)*/
        
        return null;
    }
    
    
    /**
     * Genera la ruta y nombre de un archivo temporal a partir del parámetro
     * {@code resFile}, dicho archivo temporal es utilizado como destino
     * intermedio de escritura durante las operaciones de actualización de
     * claves de configuración, evitando modificar directamente el archivo
     * original hasta que el proceso de escritura se complete con éxito.<br>
     * El nombre del archivo temporal se compone del nombre original de
     * {@code resFile} seguido de un sufijo con la fecha y hora actual en
     * formato {@code yyMMddHHmmssSSS} (con precisión de milisegundos) y la
     * extensión {@code .tmp}, lo que reduce la probabilidad de colisión entre
     * archivos temporales generados en llamadas sucesivas sobre el mismo recurso.<br>
     * El archivo temporal se ubica en el mismo directorio que {@code resFile}
     * ({@link Path#resolveSibling(String)}), requisito necesario para que
     * {@link #fileReplacement(Path, Path)} pueda intentar el reemplazo en forma
     * atómica ({@link StandardCopyOption#ATOMIC_MOVE}), garantía que en la
     * generalidad de los sistemas de archivos solo aplica cuando origen y destino
     * residen en el mismo volumen.
     * <br><br>
     * 
     * @param resFile Ruta, Nombre y extensión, si corresponde, del archivo de
     *        configuración del cual se desea crear un archivo temporal.
     * 
     * @return Ruta del archivo temporal, ubicado en el mismo directorio que
     *         {@code resFile}, con sufijo único basado en la fecha y hora actual.
     * 
     * @see #fileReplacement(Path, Path)
     * @see #setTextKey(Path, String, String, boolean)
     * @see #setXmlKey(Path, String, String, boolean)
     * @see #setJsonKey(Path, String, String, boolean)
     */
    private static Path getTmpFile(Path resFile) {
        String tmpFileSuffix = new SimpleDateFormat("yyMMddHHmmssSSS").format(new Date());
        String tmpFileName = String.format("%s_%s.tmp", resFile.getFileName(), tmpFileSuffix);
        
        return resFile.resolveSibling(tmpFileName);
    }
    
    
/*----------------------------------------------------------------------------*/
/*                                 Predicados                                 */
/*----------------------------------------------------------------------------*/
    
    
    /**
     * Indica si el carácter es espacio en blanco según la semántica de
     * {@code java.util.Properties} ({@code ' '}, {@code '\t'} y {@code '\f'}),
     * espejando el parser de {@code load(java.io.Reader)}.
     * <br><br>
     * 
     * @param charAt Carácter a evaluar.
     * 
     * @return {@code true} si el carácter es un espacio en blanco de properties.
     * 
     * @see #parseTextRawKey(String)
     */
    private static boolean isPropWhiteSpace(char charAt) {
        return charAt == ' ' || charAt == '\t' || charAt == '\f';
    }
    
    
    /**
     * Indica si la línea termina con una barra invertida sin escapar (cantidad
     * impar), condición que hace que la línea continúe en la siguiente línea
     * según la semántica de {@code java.util.Properties}.<br>
     * Los espacios en blanco finales se ignoran antes del análisis, tal como lo
     * hace el parser de {@code load(java.io.Reader)}.
     * <br><br>
     * 
     * @param line Línea del archivo de properties a analizar.
     * 
     * @return {@code true} si la línea abre una continuación.
     * 
     * @see #setTextKey(Path, String, String, boolean)
     */
    private static boolean hasOpenContinuation(String line) {
        int end = line.length();
        
        while (end > 0 && isPropWhiteSpace(line.charAt(end - 1))) { // Ignora Espacios Finales
            end--;
        }
        
        int backslashCount = 0;
        for (int i = end - 1; i >= 0 && line.charAt(i) == '\\'; i--) {
            backslashCount++;
        }
        
        return (backslashCount % 2) == 1; // Cantidad Impar: la Última Barra no Está Escapada
    }    
    
    
/*----------------------------------------------------------------------------*/
/*                                   Parse                                    */
/*----------------------------------------------------------------------------*/
    
    
    /**
     * Extrae la clave (bruta, sin procesar secuencias de escape) de una línea
     * natural de un archivo de properties, es decir el texto ubicado entre la
     * indentación inicial y el primer separador no escapado ({@code =},
     * {@code :} o espacio en blanco).<br>
     * Espeja la regla de terminación de claves del parser de
     * {@code java.util.Properties}; una barra invertida protege al carácter
     * siguiente de actuar como separador.<br>
     * Si la línea no posee separador se considera que toda la línea es la clave,
     * es decir una clave sin valor asignado (equivalente a {@code clave=}); el
     * método la devuelve completa.
     * <br><br>
     * 
     * @param line Línea del archivo de properties a analizar.
     * 
     * @return Clave escapada de la línea, sin procesar secuencias de escape, o
     *         {@code null} si la línea no contiene una clave (línea vacía o
     *         compuesta únicamente por espacios en blanco).
     * 
     * @see #setTextKey(Path, String, String, boolean)
     * @see Properties#unescaping(String)
     */
    private static String parseTextRawKey(String line) {
        int i = 0;
        int len = line.length();
        
        while (i < len && isPropWhiteSpace(line.charAt(i))) { // Salta la Indentación Inicial
            i++;
        }
        
        boolean escaped = false;
        int keyStart = i;
        int keyEnd = -1;
        
        for (; i < len; i++) {
            char charAt = line.charAt(i);
            
            if (escaped) {
                escaped = false;
            } else if (charAt == '\\') {
                escaped = true;
            } else if (charAt == '=' || charAt == ':' || isPropWhiteSpace(charAt)) {
                keyEnd = i;
                break;
            }
        }
        
        if (keyEnd == -1) { // Sin Separador: Toda la Línea es la Clave (Clave sin Valor)
            return (keyStart < len) ? line.substring(keyStart) : null;
        }
        
        String rawKey = line.substring(keyStart, keyEnd);
        return rawKey.isEmpty() ? null : rawKey;
    }
    
    
    /**
     * Localiza el índice de finalización de una cadena de texto JSON (delimitada
     * por comillas dobles) dentro de una línea, ignorando de manera segura las
     * comillas que se encuentren escapadas.<br>
     * El método inicia el análisis a partir de la posición especificada por
     * {@code start}, la cual obligatoriamente debe apuntar a la comilla doble de
     * apertura ({@code "}). Si encuentra una barra invertida ({@code \}) durante
     * el recorrido, omite el carácter inmediatamente posterior para evitar falsos
     * positivos con comillas de cierre escapadas.
     * <br><br>
     * 
     * @param line Línea de texto del archivo JSON que se desea analizar.
     * @param start Indice base donde se sitúa la comilla de apertura del
     *        valor JSON.
     * 
     * @return Indice inmediatamente posterior a la comilla de cierre
     *         correspondiente (es decir, {@code indice_comilla_cierre + 1}), o
     *         {@code -1} si la posición de inicio es inválida o la cadena no se
     *         cierra correctamente en la línea.
     */
    private static int parseJsonStringEnd(String line, int start) {
        if (start >= line.length() || line.charAt(start) != '"') {
            return -1;
        }
        
        for (int i = start + 1; i < line.length(); i++) {
            
            if (line.charAt(i) == '\\') {
                i++;
            } else if (line.charAt(i) == '"') {
                return i + 1;
            }
        }
        
        return -1;
    }
    
    
    /**
     * Cuenta la cantidad de entradas {@code "clave": valor} presentes en el
     * contenido de una línea de un archivo de configuración JSON de objeto
     * plano, ignorando de manera segura las comillas escapadas y las comillas
     * internas de los valores de texto.<br>
     * Una entrada se reconoce por una cadena de texto JSON ({@code "..."})
     * seguida, tras espacios en blanco, del carácter separador de clave
     * {@code ':'}.<br>
     * El método es utilizado por {@link #setJsonKey(Path, String, String,
     * boolean)} para garantizar el soporte de una sola entrada por línea.
     * <br><br>
     *
     * @param content Contenido de la línea a analizar.
     *
     * @return Cantidad de entradas {@code "clave": valor} detectadas.
     *
     * @see #setJsonKey(Path, String, String, boolean)
     * @see #parseJsonStringEnd(String, int)
     */
    private static int countJsonEntries(String content) {
        int entryCount = 0;

        for (int i = 0; i < content.length(); i++) {

            if (content.charAt(i) == '"') {

                int stringEnd = parseJsonStringEnd(content, i);
                if (stringEnd != -1) {

                    int cursor = stringEnd;
                    while (cursor < content.length() &&
                           (content.charAt(cursor) == ' ' || content.charAt(cursor) == '\t')) {
                        cursor++;
                    }

                    if (cursor < content.length() && content.charAt(cursor) == ':') {
                        entryCount++;
                        i = cursor;
                    } else {
                        i = stringEnd - 1;
                    }
                }
            }
        }

        return entryCount;
    }


    /**
     * Indica si el contenido de una línea de un archivo de configuración JSON
     * de objeto plano posee llaves ({@code '{'}, {@code '}'}) o corchetes
     * ({@code '['}, {@code ']'}) fuera de cualquier cadena de texto JSON,
     * condición que delata objetos anidados, arreglos o llaves en posiciones
     * no soportadas por el formato.<br>
     * Las llaves y corchetes internos de los valores de texto (delimitados por
     * comillas dobles) se ignoran.<br>
     * Si el contenido posee una cadena de texto sin cerrar, el resultado es
     * {@code true} (formato no válido).
     * <br><br>
     *
     * @param content Contenido de la línea a analizar.
     *
     * @return {@code true} si existe una llave o corchete fuera de cadenas, o
     *         una cadena de texto sin cerrar.
     *
     * @see #setJsonKey(Path, String, String, boolean)
     * @see #parseJsonStringEnd(String, int)
     */
    private static boolean hasBraceOutsideStrings(String content) {
        for (int i = 0; i < content.length(); i++) {

            char charAt = content.charAt(i);
            if (charAt == '"') {

                int stringEnd = parseJsonStringEnd(content, i);
                if (stringEnd == -1) {
                    return true; // Cadena de Texto sin Cerrar: Formato No Válido
                }
                i = stringEnd - 1;
            } else if (charAt == '{' || charAt == '}' || charAt == '[' || charAt == ']') {
                return true; // Llave o Corchete Fuera de Cadena: Anidados, Arreglos o Posición Inválida
            }
        }

        return false;
    }

    
    
/*----------------------------------------------------------------------------*/
/*                                                                            */
/*----------------------------------------------------------------------------*/
    
    
    /**
     * Válida que el recurso de configuración representado por {@code resFile}
     * cumpla las condiciones mínimas para ser procesado por los métodos de
     * plataforma del CCS {@link Config}.<br>
     * Se considera válido cuando {@code resFile} no es {@code null} ni vacío,
     * y hace referencia a un archivo regular existente con permisos de
     * lectura ({@link Files#isRegularFile(Path, java.nio.file.LinkOption...)} y
     * {@link Files#isReadable(Path)}).<br>
     * Este método no valida permisos de escritura; dicha condición se verifica de
     * forma implícita al intentar la escritura del archivo temporal o el
     * reemplazo del recurso original en {@link #fileReplacement(Path, Path)}.
     * <br><br>
     * 
     * @param resFile Ruta, Nombre y extensión, si corresponde, del archivo de
     *        configuración a validar.
     * 
     * @throws IllegalArgumentException Si {@code resFile} es igual a {@code null}
     *         o vacíos.
     * @throws CCSResourceAccessException Si el archivo {@code resFile} no
     *         existe o es inaccesible.
     * 
     * @see #setTextKey(Path, String, String, boolean)
     * @see #setXmlKey(Path, String, String, boolean)
     * @see #setJsonKey(Path, String, String, boolean)
     * @see #getTextKey(Path, String)
     * @see #getXmlKey(Path, String)
     * @see #getJsonKey(Path, String)
     */
    private static void resFileValidator(Path resFile) throws IllegalArgumentException, CCSResourceAccessException {
        if (resFile == null || resFile.toString().trim().isEmpty()) {
            throw new IllegalArgumentException(String.format(ExcMsg.PARAM_NOT_NULL_EMPTY, "resFile"));
        }
        
        if (!Files.isReadable(resFile) || !Files.isRegularFile(resFile)) { // Validación de Existencia de Recurso
            throw new CCSResourceAccessException(String.format(
                ExcMsg.CCS_RES_CANNOT_ACCESS,
                resFile.getParent(),
                resFile.getFileName()
            ));
        }
    }
    
    
    /**
     * Reemplaza el recurso de configuración original {@code resFile} por el
     * archivo temporal {@code tmpFile} generado durante la escritura de una
     * clave de configuración, una vez completado dicho proceso con éxito.<br>
     * Se intenta primero un reemplazo atómico
     * ({@link StandardCopyOption#ATOMIC_MOVE}), garantizando que una lectura
     * concurrente sobre {@code resFile} nunca observe un archivo parcialmente
     * escrito o inconsistente.<br>
     * Si el sistema de archivos subyacente no soporta movimientos atómicos
     * ({@link AtomicMoveNotSupportedException}), se registra la condición como
     * log de depuración en el recurso principal de logs del módulo (solo si este
     * se encuentra en estado {@link ModState#RUNNING}) y se reintenta el
     * reemplazo sin la garantía atómica ({@link StandardCopyOption#REPLACE_EXISTING}).
     * <br><br>
     * 
     * @param resFile Ruta, Nombre y extensión, si corresponde, del archivo de
     *        configuración original a intercambiar.
     * @param tmpFile Ruta, Nombre y extensión, si corresponde, del archivo de
     *        configuración temporal a intercambiar.
     * 
     * @throws IOException Si se produce un error en el reemplazo del archivo
     *         temporal, tanto en el intento atómico como en el intento de
     *         contingencia sin garantía atómica.
     * 
     * @see #getTmpFile(Path)
     * @see #setTextKey(Path, String, String, boolean)
     * @see #setXmlKey(Path, String, String, boolean)
     * @see #setJsonKey(Path, String, String, boolean)
     */
    private static void fileReplacement(Path resFile, Path tmpFile) throws IOException {
        try {
            try {
                Files.move(tmpFile, resFile, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE); // Reemplazo de Archivos ATOMIC
            } catch (AtomicMoveNotSupportedException e) {
                
                if (modManager.getState() == ModState.RUNNING) {
                    modManager.setLogLine(
                        LogTYPE.EXCEPTION,
                        LogLEVEL.DEBUG,
                        "Reemplazo de Archivos Temporales de Configuración, SO no Compatible ATOMIC.",
                        null,
                        e,
                        2
                    );
                }
                
                Files.move(tmpFile, resFile, StandardCopyOption.REPLACE_EXISTING);  // Reemplazo de Archivos Sin ATOMIC (por si SO No Compatible)
            }
        } finally {
            try {
                Files.deleteIfExists(tmpFile); // Limpia si el Files.move(..) no se Concretó
            } catch (IOException e2) {
                if (modManager.getState() == ModState.RUNNING) {
                    modManager.setLogLine(
                        LogTYPE.EXCEPTION,
                        LogLEVEL.WARNING,
                        "No se pudo eliminar el archivo temporal de configuración.",
                        tmpFile.toString(),
                        e2,
                        2
                    );
                }
            }
        }
    }
}
