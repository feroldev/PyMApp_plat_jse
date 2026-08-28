/* 
 * @(#)TestLogPLAT.java    1.6.2 25/07/30
 * 
 * Copyright (c) 1999-2025 OLMEDO Fernando R. {ferol.dev}
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */


package dev.ferol.pymapp.plat.jse.ccs;


import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.List;

import dev.ferol.pymapp.base.ccs.log.Log;
import dev.ferol.pymapp.base.ccs.log.LogLEVEL;
import dev.ferol.pymapp.base.ccs.log.LogTYPE;

import dev.ferol.pymapp.util.format.CSV;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;


/**
 * Clase de pruebas unitarias de la implementación de plataforma del CCS de
 * log {@link LogPLAT}, para los formatos CSV variante PyMApp
 * ({@link LogPLAT#setCsvLine(Path, LogTYPE, LogLEVEL, int, int, String,
 * String, Throwable, int)}) y NDJSON
 * ({@link LogPLAT#setJsonLine(Path, LogTYPE, LogLEVEL, int, int, String,
 * String, Throwable, int)}).<br>
 * Se verifican la estructura de 9 campos del formato CSV, el escape de los
 * caracteres reservados, el volcado de excepciones, los clamps de
 * {@code maxBytes} (mínimo 256) y {@code numRotation} (mínimo 1), y la
 * rotación/truncado de archivos de log con el patrón {@code %g}.<br>
 * Cada test opera sobre un directorio temporal propio (regla
 * {@link TemporaryFolder}); los métodos no dependen del estado del
 * {@link dev.ferol.pymapp.base.kernel.Kernel}.
 * <br><br>
 * 
 * @see LogPLAT
 * @see Log
 * @see LogTYPE
 * @see LogLEVEL
 * 
 * @author    OLMEDO Fernando R. {ferol.dev}
 * @version    1.6.2 25/07/30
 */
public class TestLogPLAT {
    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();
    private static final String STACK_FRAME = "TestLogPLAT";
    
    
/*----------------------------------------------------------------------------*/
/*                                    CSV                                     */
/*----------------------------------------------------------------------------*/
    
    
    /**
     * Verifica la estructura de la línea de log CSV: 9 campos separados por
     * el carácter {@code '|'} (variante PyMApp), con marca temporal, tipo y
     * nivel de log, y los campos de texto vacíos cuando no corresponden.
     */
    @Test
    public void testCsvLine_Estructura9Campos() throws Exception {
        Path logFile = Paths.get(tempFolder.getRoot().getAbsolutePath(), "test.log");
        
        LogPLAT.setCsvLine(logFile, LogTYPE.APPLICATION, LogLEVEL.INFORMATIONAL, 256, 1, "hola", "datos", null, 0);
        
        String line = readLine(logFile, 0);
        String[] fields = line.split("\\|", -1);
        
        assertEquals(9, fields.length);
        assertTrue(
            "Marca temporal con formato yyyy-MM-dd HH:mm:ss.SSS.", 
            fields[0].matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}\\.\\d{3}")
        );
        assertFalse(fields[1].isEmpty()); // InstanceID
        assertEquals("APPLICATION", fields[2]);
        assertEquals("INFORMATIONAL", fields[3]);
        assertTrue(fields[4].contains(STACK_FRAME)); // stackData
        assertEquals("hola", fields[5]);
        assertEquals("datos", fields[6]);
        assertEquals("", fields[7]);
        assertEquals("", fields[8]);
    }
    
    
    /**
     * Verifica el escape de los caracteres reservados del formato CSV
     * variante PyMApp ({@code '|'} y {@code '\'}) en los campos de texto
     * libre, y su recuperación mediante {@link CSV#unescaping(String)}.
     */
    @Test
    public void testCsvLine_EscapeSeparadorYBarra() throws Exception {
        Path logFile = Paths.get(tempFolder.getRoot().getAbsolutePath(), "test.log");
        String message = "a|b\\c";
        String addData = "d|e\\f";
        
        LogPLAT.setCsvLine(logFile, LogTYPE.APPLICATION, LogLEVEL.WARNING, 256, 1, message, addData, null, 0);
        
        String line = readLine(logFile, 0);
        String[] fields = line.split("\\|", -1);
        
        assertEquals(9, fields.length);
        assertEquals("a\\|b\\\\c", fields[5]);
        assertEquals("d\\|e\\\\f", fields[6]);
        assertEquals(message, CSV.unescaping(fields[5]));
        assertEquals(addData, CSV.unescaping(fields[6]));
    }
    
    
    /**
     * Verifica el registro de una excepción: los campos
     * {@code exceptionMessage} (equivalente a {@link Throwable#toString()})
     * y {@code exceptionStack} (volcado de pila con saltos de línea
     * ({@code \n})) resultan no vacíos.
     */
    @Test
    public void testCsvLine_Excepcion() throws Exception {
        Path logFile = Paths.get(tempFolder.getRoot().getAbsolutePath(), "test.log");
        Throwable exception = new IllegalArgumentException("mensaje de excepción");
        
        LogPLAT.setCsvLine(logFile, LogTYPE.EXCEPTION, LogLEVEL.ERROR, 256, 1, "fallo", null, exception, 0);
        
        String line = readLine(logFile, 0);
        String[] fields = line.split("\\|", -1);
        
        assertEquals(9, fields.length);
        assertEquals("java.lang.IllegalArgumentException: mensaje de excepción", fields[7]);
        assertTrue(fields[8].contains("at "));
        assertTrue(fields[8].contains("\\n")); // Saltos de Línea de la Pila Escapados en Formato CSV
        assertTrue(fields[8].contains(STACK_FRAME));
    }
    
    
    /**
     * Verifica que {@code addData == null} resulte en un campo vacío.
     */
    @Test
    public void testCsvLine_AddDataNulo() throws Exception {
        Path logFile = Paths.get(tempFolder.getRoot().getAbsolutePath(), "test.log");
        
        LogPLAT.setCsvLine(logFile, LogTYPE.APPLICATION, LogLEVEL.INFORMATIONAL, 256, 1, "mensaje", null, null, 0);
        
        assertEquals("", readLine(logFile, 0).split("\\|", -1)[6]);
    }
    
    
    /**
     * Verifica que un salto de línea real ({@code \n}) en el campo
     * {@code message} no rompa la estructura física del archivo: la línea se
     * registra como una sola línea física con el salto de línea escapado
     * ({@code \n}), recuperable sin pérdida mediante
     * {@link CSV#unescaping(String)}.
     */
    @Test
    public void testCsvLine_SaltoDeLineaRealEnMessage() throws Exception {
        Path logFile = Paths.get(tempFolder.getRoot().getAbsolutePath(), "test.log");
        String message = "linea1\nlinea2";
        
        LogPLAT.setCsvLine(logFile, LogTYPE.APPLICATION, LogLEVEL.INFORMATIONAL, 256, 1, message, null, null, 0);
        
        String line = readLine(logFile, 0);
        String[] fields = line.split("\\|", -1);
        
        assertEquals(1, countLines(logFile)); // Invariante: una línea física por registro
        assertEquals("linea1\\nlinea2", fields[5]); // Escapado Bruto de message
        assertEquals(message, CSV.unescaping(fields[5]));
    }
    
    
    /**
     * Verifica que un salto de línea real ({@code \n}) en el campo
     * {@code addData} no rompa la estructura física del archivo: la línea se
     * registra como una sola línea física y el contenido se recupera sin
     * pérdida mediante {@link CSV#unescaping(String)}.
     */
    @Test
    public void testCsvLine_SaltoDeLineaRealEnAddData() throws Exception {
        Path logFile = Paths.get(tempFolder.getRoot().getAbsolutePath(), "test.log");
        String addData = "linea1\nlinea2";
        
        LogPLAT.setCsvLine(logFile, LogTYPE.APPLICATION, LogLEVEL.INFORMATIONAL, 256, 1, "mensaje", addData, null, 0);
        
        String line = readLine(logFile, 0);
        String[] fields = line.split("\\|", -1);
        
        assertEquals(1, countLines(logFile)); // Invariante: una línea física por registro
        assertTrue(fields[6].contains("linea1\\nlinea2")); // Escapado Bruto de addData
        assertTrue(CSV.unescaping(fields[6]).startsWith(addData));
    }
    
    
    /**
     * Verifica que el registro de una excepción no rompa la estructura física
     * del archivo: el volcado de pila (campo {@code exceptionStack}) con
     * saltos de línea se escapa, quedando una sola línea física por registro.
     */
    @Test
    public void testCsvLine_ExcepcionUnaLineaFisica() throws Exception {
        Path logFile = Paths.get(tempFolder.getRoot().getAbsolutePath(), "test.log");
        Throwable exception = new IllegalArgumentException("mensaje de excepción");
        
        LogPLAT.setCsvLine(logFile, LogTYPE.EXCEPTION, LogLEVEL.ERROR, 256, 1, "fallo", null, exception, 0);
        
        assertEquals(1, countLines(logFile)); // Invariante: una línea física por registro
    }
    
    
    /**
     * Verifica el registro de una excepción sin mensaje: el campo
     * {@code exceptionMessage} resulta igual a {@link Throwable#toString()}
     * ({@code java.lang.IllegalArgumentException} sin sufijo de mensaje) y el
     * campo {@code exceptionStack} contiene el volcado de pila escapado.
     */
    @Test
    public void testCsvLine_ExcepcionSinMensaje() throws Exception {
        Path logFile = Paths.get(tempFolder.getRoot().getAbsolutePath(), "test.log");
        Throwable exception = new IllegalArgumentException();
        
        LogPLAT.setCsvLine(logFile, LogTYPE.EXCEPTION, LogLEVEL.ERROR, 256, 1, "fallo", null, exception, 0);
        
        String[] fields = readLine(logFile, 0).split("\\|", -1);
        
        assertEquals("java.lang.IllegalArgumentException", fields[7]);
        assertTrue(fields[8].contains("at "));
        assertTrue(fields[8].contains("\\n"));
    }
    
    
    /**
     * Verifica que un {@code stackAdjust} excesivo o negativo se recorte a
     * los límites de la pila disponible sin lanzar
     * {@link ArrayIndexOutOfBoundsException}, registrando igualmente la línea.
     */
    @Test
    public void testCsvLine_StackAdjustExtremos() throws Exception {
        Path logFile = Paths.get(tempFolder.getRoot().getAbsolutePath(), "test.log");
        
        LogPLAT.setCsvLine(logFile, LogTYPE.APPLICATION, LogLEVEL.INFORMATIONAL, 256,1,"ajuste excesivo",null,null,500);
        LogPLAT.setCsvLine(logFile, LogTYPE.APPLICATION, LogLEVEL.INFORMATIONAL, 256,1,"ajuste negativo",null,null,-50);
        
        assertEquals(2, countLines(logFile));
        assertFalse(readLine(logFile, 0).split("\\|", -1)[4].isEmpty());
        assertFalse(readLine(logFile, 1).split("\\|", -1)[4].isEmpty());
    }
    
    
    /**
     * Verifica el truncado del archivo de log con {@code numRotation == 1}:
     * al superar {@code maxBytes} el archivo se sobrescribe, quedando solo la
     * última línea registrada.
     */
    @Test
    public void testCsvLine_TruncadoSinRotacion() throws Exception {
        Path logFile = Paths.get(tempFolder.getRoot().getAbsolutePath(), "test.log");
        String bigMessage = longMessage(300);
        
        for (int i = 0; i < 6; i++) {
            LogPLAT.setCsvLine(logFile, LogTYPE.APPLICATION, LogLEVEL.INFORMATIONAL, 256, 1, bigMessage, null, null, 0);
        }
        
        assertEquals(1, countLines(logFile));
    }
    
    
    /**
     * Verifica la rotación de archivos con el patrón {@code %g} y
     * {@code numRotation == 3}: al superar {@code maxBytes} el archivo activo
     * (índice 0) se desplaza, quedando al final los archivos
     * {@code rot_0.log}, {@code rot_1.log} y {@code rot_2.log} con una línea
     * cada uno, y sin existir {@code rot_3.log}.
     */
    @Test
    public void testCsvLine_RotacionConPatron() throws Exception {
        Path logFile = Paths.get(tempFolder.getRoot().getAbsolutePath(), "rot_%g.log");
        String bigMessage = longMessage(300);
        
        for (int i = 0; i < 6; i++) {
            LogPLAT.setCsvLine(logFile, LogTYPE.APPLICATION, LogLEVEL.INFORMATIONAL, 256, 3, bigMessage, null, null, 0);
        }
        
        Path rot0 = Paths.get(tempFolder.getRoot().getAbsolutePath(), "rot_0.log");
        Path rot1 = Paths.get(tempFolder.getRoot().getAbsolutePath(), "rot_1.log");
        Path rot2 = Paths.get(tempFolder.getRoot().getAbsolutePath(), "rot_2.log");
        Path rot3 = Paths.get(tempFolder.getRoot().getAbsolutePath(), "rot_3.log");
        
        assertTrue(Files.exists(rot0));
        assertTrue(Files.exists(rot1));
        assertTrue(Files.exists(rot2));
        assertFalse(Files.exists(rot3));
        assertEquals(1, countLines(rot0));
        assertEquals(1, countLines(rot1));
        assertEquals(1, countLines(rot2));
    }
    
    
    /**
     * Verifica el desplazamiento real de contenidos en la rotación con el
     * patrón {@code %g} y {@code numRotation == 3}: tras seis registros, el
     * archivo activo (índice 0) contiene la última línea y los archivos
     * rotados las anteriores en orden (índice 1 la penúltima e índice 2 la
     * antepenúltima), evidenciando el corrimiento de archivos.
     */
    @Test
    public void testCsvLine_Rotacion_DesplazamientoDeContenidos() throws Exception {
        Path logFile = Paths.get(tempFolder.getRoot().getAbsolutePath(), "rot_%g.log");
        
        for (int i = 0; i < 6; i++) {
            String message = "linea-" + i + longMessage(300);
            LogPLAT.setCsvLine(logFile, LogTYPE.APPLICATION, LogLEVEL.INFORMATIONAL, 256, 3, message, null, null, 0);
        }
        
        String line0 = readLine(Paths.get(tempFolder.getRoot().getAbsolutePath(), "rot_0.log"), 0);
        String line1 = readLine(Paths.get(tempFolder.getRoot().getAbsolutePath(), "rot_1.log"), 0);
        String line2 = readLine(Paths.get(tempFolder.getRoot().getAbsolutePath(), "rot_2.log"), 0);
        
        assertTrue(line0.contains("linea-5")); // Última Línea en el Archivo Activo
        assertTrue(line1.contains("linea-4")); // Penúltima Línea en el Índice 1
        assertTrue(line2.contains("linea-3")); // Antepenúltima Línea en el Índice 2
    }
    
    
    /**
     * Verifica el comportamiento documentado de la rotación cuando el nombre
     * del archivo no contiene el patrón {@code %g} y {@code numRotation > 1}:
     * el resultado es el mismo que con {@code numRotation == 1}, es decir el
     * truncado del archivo al superar {@code maxBytes}, sin generación de
     * archivos rotados.
     */
    @Test
    public void testCsvLine_NumRotationMayor1_SinPatron_Trunca() throws Exception {
        Path logFile = Paths.get(tempFolder.getRoot().getAbsolutePath(), "test.log");
        String bigMessage = longMessage(300);
        
        for (int i = 0; i < 6; i++) {
            LogPLAT.setCsvLine(logFile, LogTYPE.APPLICATION, LogLEVEL.INFORMATIONAL, 256, 3, bigMessage, null, null, 0);
        }
        
        assertEquals(1, countLines(logFile)); // Comportamiento Equivalente a numRotation == 1
    }
    
    
    /**
     * Verifica el clamp de {@code maxBytes < 256} a 256: con un valor de 10
     * bytes y líneas de log menores a 256 bytes no se produce truncado.
     */
    @Test
    public void testCsvLine_ClampMaxBytes() throws Exception {
        Path logFile = Paths.get(tempFolder.getRoot().getAbsolutePath(), "test.log");
        
        for (int i = 0; i < 3; i++) {
            LogPLAT.setCsvLine(logFile, LogTYPE.APPLICATION, LogLEVEL.INFORMATIONAL, 10, 1,"mensaje corto",null,null,0);
        }
        
        assertEquals(3, countLines(logFile));
    }
    
    
    /**
     * Verifica el clamp de {@code numRotation < 1} a 1: con
     * {@code numRotation == 0} el comportamiento es el de truncado del
     * archivo base, sin generación de archivos rotados.
     */
    @Test
    public void testCsvLine_ClampNumRotation() throws Exception {
        Path logFile = Paths.get(tempFolder.getRoot().getAbsolutePath(), "test.log");
        String bigMessage = longMessage(300);
        
        for (int i = 0; i < 3; i++) {
            LogPLAT.setCsvLine(logFile, LogTYPE.APPLICATION, LogLEVEL.INFORMATIONAL, 256, 0, bigMessage, null, null, 0);
        }
        
        assertEquals(1, countLines(logFile));
    }
    
    
    /**
     * Verifica la validación de parámetros del formato CSV:
     * {@code resFile == null}, {@code logTYPE == null} o
     * {@code logLEVEL == null} lanzan {@link IllegalArgumentException}.
     */
    @Test
    public void testCsvLine_ParametrosInvalidos() throws Exception {
        Path logFile = Paths.get(tempFolder.getRoot().getAbsolutePath(), "test.log");
        
        assertThrows(IllegalArgumentException.class,
            () -> LogPLAT.setCsvLine(null, LogTYPE.APPLICATION, LogLEVEL.INFORMATIONAL, 256, 1, "m", null, null, 0));
        assertThrows(IllegalArgumentException.class,
            () -> LogPLAT.setCsvLine(logFile, null, LogLEVEL.INFORMATIONAL, 256, 1, "m", null, null, 0));
        assertThrows(IllegalArgumentException.class,
            () -> LogPLAT.setCsvLine(logFile, LogTYPE.APPLICATION, null, 256, 1, "m", null, null, 0));
    }
    
    
/*----------------------------------------------------------------------------*/
/*                                    JSON                                    */
/*----------------------------------------------------------------------------*/
    
    
    /**
     * Verifica la estructura de la línea de log NDJSON: un objeto JSON de una
     * sola línea con las claves mapeadas a nombre, incluyendo los campos de
     * texto vacíos cuando no corresponden.
     */
    @Test
    public void testJsonLine_Estructura() throws Exception {
        Path logFile = Paths.get(tempFolder.getRoot().getAbsolutePath(), "test.log");
        
        LogPLAT.setJsonLine(logFile, LogTYPE.APPLICATION, LogLEVEL.INFORMATIONAL, 256, 1, "hola", "datos", null, 0);
        
        String line = readLine(logFile, 0);
        
        assertTrue(line.startsWith("{"));
        assertTrue(line.endsWith("}"));
        assertTrue(line.contains("\"logTYPE\":\"APPLICATION\""));
        assertTrue(line.contains("\"logLEVEL\":\"INFORMATIONAL\""));
        assertTrue(line.contains("\"message\":\"hola\""));
        assertTrue(line.contains("\"addData\":\"datos\""));
        assertTrue(line.contains("\"exceptionMessage\":\"\""));
        assertTrue(line.contains("\"exceptionStack\":\"\""));
    }
    
    
    /**
     * Verifica el escape JSON de comillas dobles y barra invertida en los
     * campos de texto libre.
     */
    @Test
    public void testJsonLine_Escape() throws Exception {
        Path logFile = Paths.get(tempFolder.getRoot().getAbsolutePath(), "test.log");
        String message = "a\"b\\c";
        
        LogPLAT.setJsonLine(logFile, LogTYPE.APPLICATION, LogLEVEL.INFORMATIONAL, 256, 1, message, null, null, 0);
        
        assertTrue(readLine(logFile, 0).contains("\"message\":\"a\\\"b\\\\c\""));
    }
    
    
    /**
     * Verifica la codificación del salto de línea ({@code \n} escapado) en el
     * campo {@code addData} del formato JSON.
     */
    @Test
    public void testJsonLine_AddDataConLineBreak() throws Exception {
        Path logFile = Paths.get(tempFolder.getRoot().getAbsolutePath(), "test.log");
        String addData = "linea1\nlinea2"; // Salto de Línea Real que Debe Codificarse como \"\\n\"
        
        LogPLAT.setJsonLine(logFile, LogTYPE.APPLICATION, LogLEVEL.INFORMATIONAL, 256, 1, "mensaje", addData, null, 0);
        
        String line = readLine(logFile, 0);
        assertTrue(line.contains("\"addData\":\"linea1\\nlinea2\""));
        assertFalse(line.contains("\n"));
    }
    
    
    /**
     * Verifica que un salto de línea real ({@code \n}) en el campo
     * {@code message} no rompa la estructura física del archivo NDJSON: la
     * línea se registra como una sola línea física con el salto codificado
     * como secuencia de escape {@code \n}.
     */
    @Test
    public void testJsonLine_MensajeConSaltoReal_UnaLineaFisica() throws Exception {
        Path logFile = Paths.get(tempFolder.getRoot().getAbsolutePath(), "test.log");
        String message = "linea1\nlinea2";
        
        LogPLAT.setJsonLine(logFile, LogTYPE.APPLICATION, LogLEVEL.INFORMATIONAL, 256, 1, message, null, null, 0);
        
        assertEquals(1, countLines(logFile)); // Invariante: una línea física por registro
        assertTrue(readLine(logFile, 0).contains("\"message\":\"linea1\\nlinea2\""));
    }
    
    
    /**
     * Verifica el registro de una excepción en formato JSON: el mensaje de la
     * excepción en {@code exceptionMessage} y el volcado de pila en
     * {@code exceptionStack} con los saltos de línea escapados.
     */
    @Test
    public void testJsonLine_Excepcion() throws Exception {
        Path logFile = Paths.get(tempFolder.getRoot().getAbsolutePath(), "test.log");
        Throwable exception = new IllegalArgumentException("falla json");
        
        LogPLAT.setJsonLine(logFile, LogTYPE.EXCEPTION, LogLEVEL.ERROR, 256, 1, "fallo", null, exception, 0);
        
        String line = readLine(logFile, 0);
        
        assertTrue(line.contains("\"exceptionMessage\":\"java.lang.IllegalArgumentException: falla json\""));
        assertTrue(line.contains("\\n"));
        assertTrue(line.contains("at "));
        assertTrue(line.contains(STACK_FRAME));
    }
    
    
    /**
     * Verifica la rotación de archivos de log JSON con el patrón {@code %g}:
     * misma semántica de rotación que el formato CSV.
     */
    @Test
    public void testJsonLine_RotacionConPatron() throws Exception {
        Path logFile = Paths.get(tempFolder.getRoot().getAbsolutePath(), "rotj_%g.log");
        String bigMessage = longMessage(300);
        
        for (int i = 0; i < 4; i++) {
            LogPLAT.setJsonLine(logFile, LogTYPE.APPLICATION, LogLEVEL.INFORMATIONAL, 256, 3, bigMessage, null, null,0);
        }
        
        assertTrue(Files.exists(Paths.get(tempFolder.getRoot().getAbsolutePath(), "rotj_0.log")));
        assertTrue(Files.exists(Paths.get(tempFolder.getRoot().getAbsolutePath(), "rotj_1.log")));
        assertTrue(Files.exists(Paths.get(tempFolder.getRoot().getAbsolutePath(), "rotj_2.log")));
    }
    
    
    /**
     * Verifica que un {@code stackAdjust} excesivo o negativo se recorte a
     * los límites de la pila disponible sin lanzar
     * {@link ArrayIndexOutOfBoundsException}, registrando igualmente la línea
     * en formato JSON con el campo {@code stackData} no vacío.
     */
    @Test
    public void testJsonLine_StackAdjustExtremos() throws Exception {
        Path logFile = Paths.get(tempFolder.getRoot().getAbsolutePath(), "test.log");
        
        LogPLAT.setJsonLine(logFile, LogTYPE.APPLICATION, LogLEVEL.INFORMATIONAL,256,1,"ajuste excesivo",null,null,500);
        LogPLAT.setJsonLine(logFile, LogTYPE.APPLICATION, LogLEVEL.INFORMATIONAL,256,1,"ajuste negativo",null,null,-50);
        
        assertEquals(2, countLines(logFile));
        assertFalse(readLine(logFile, 0).contains("\"stackData\":\"\""));
        assertFalse(readLine(logFile, 1).contains("\"stackData\":\"\""));
    }
    
    
    /**
     * Verifica la validación de parámetros del formato JSON:
     * {@code resFile == null}, {@code logTYPE == null} o
     * {@code logLEVEL == null} lanzan {@link IllegalArgumentException}.
     */
    @Test
    public void testJsonLine_ParametrosInvalidos() throws Exception {
        Path logFile = Paths.get(tempFolder.getRoot().getAbsolutePath(), "test.log");
        
        assertThrows(IllegalArgumentException.class,
            () -> LogPLAT.setJsonLine(null, LogTYPE.APPLICATION, LogLEVEL.INFORMATIONAL, 256, 1, "m", null, null, 0));
        assertThrows(IllegalArgumentException.class,
            () -> LogPLAT.setJsonLine(logFile, null, LogLEVEL.INFORMATIONAL, 256, 1, "m", null, null, 0));
        assertThrows(IllegalArgumentException.class,
            () -> LogPLAT.setJsonLine(logFile, LogTYPE.APPLICATION, null, 256, 1, "m", null, null, 0));
    }
    
    
/*----------------------------------------------------------------------------*/
/*                                   Helpers                                  */
/*----------------------------------------------------------------------------*/
    
    
    /**
     * Genera un mensaje de la longitud indicada (caracteres {@code 'x'}).
     * <br><br>
     * 
     * @param length Longitud deseada del mensaje.
     * 
     * @return Mensaje de la longitud indicada.
     */
    private String longMessage(int length) {
        return new String(new char[length]).replace('\0', 'x');
    }
    
    
    /**
     * Lee la línea de índice {@code index} del archivo de log.
     * <br><br>
     * 
     * @param logFile Ruta del archivo de log.
     * @param index Indice de la línea a leer.
     * 
     * @return Contenido de la línea solicitada.
     * 
     * @throws Exception Si falla la lectura del archivo.
     */
    private String readLine(Path logFile, int index) throws Exception {
        return Files.readAllLines(logFile, StandardCharsets.UTF_8).get(index);
    }
    
    
    /**
     * Cuenta la cantidad de líneas del archivo de log.
     * <br><br>
     * 
     * @param logFile Ruta del archivo de log.
     * 
     * @return Cantidad de líneas del archivo.
     * 
     * @throws Exception Si falla la lectura del archivo.
     */
    private int countLines(Path logFile) throws Exception {
        List<String> lines = Files.readAllLines(logFile, StandardCharsets.UTF_8);
        
        return lines.size();
    }
}