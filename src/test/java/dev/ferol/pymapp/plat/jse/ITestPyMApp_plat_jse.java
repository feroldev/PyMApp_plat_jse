/* 
 * @(#)ITestPyMApp_plat_jse.java    1.6.2 25/07/30
 * 
 * Copyright (c) 1999-2025 OLMEDO Fernando R. {ferol.dev}
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */


package dev.ferol.pymapp.plat.jse;


import java.io.File;

import java.lang.reflect.Field;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.List;
import java.util.Locale;

import dev.ferol.pymapp.base.ccs.config.Config;
import dev.ferol.pymapp.base.ccs.i18n.I18n;
import dev.ferol.pymapp.base.ccs.log.LogLEVEL;
import dev.ferol.pymapp.base.ccs.log.LogTYPE;
import dev.ferol.pymapp.base.ccs.ImageUI;
import dev.ferol.pymapp.base.exception.KernelIllegalStateException;
import dev.ferol.pymapp.base.exception.ModManagerIllegalStateException;
import dev.ferol.pymapp.base.kernel.Kernel;
import dev.ferol.pymapp.base.kernel.KernelState;
import dev.ferol.pymapp.base.mod.ModManager;
import dev.ferol.pymapp.base.mod.ModState;
import dev.ferol.pymapp.base.mod.ModUI;

import dev.ferol.pymapp.plat.jse.ccs.ConfigFORMAT;
import dev.ferol.pymapp.plat.jse.ccs.LogFORMAT;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runners.MethodSorters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;


/**
 * Clase de pruebas de integración del administrador del módulo de plataforma
 * {@link PyMApp_plat_jse} con el {@link Kernel} de la librería.<br>
 * Se realiza un bootstrap completo del {@link Kernel} en directorios temporales
 * con los siguientes recursos:
 * <br><br>
 * #). Recurso de configuración del módulo (fixture {@code PyMApp_plat_jse.config}
 * en formato texto).<br>
 * #). Recurso de configuración de la aplicación ({@code TestApp.config}, vacío).<br>
 * #). Recurso de configuración del módulo base ({@code PyMApp_base.config},
 * con la clave {@code app.Locale=es} para forzar el idioma de la aplicación).<br>
 * #). Módulo de UI de prueba (stub {@link StubModUI}).<br>
 * <br><br>
 * El {@link Kernel#initialize(String)} inicializa automáticamente el módulo de
 * plataforma, quedando ambos en {@link ModState#RUNNING} y
 * {@link KernelState#RUNNING} respectivamente.<br>
 * Los tests de formatos XML y JSON conmutan los campos privados
 * {@code CONFIG_FORMAT} y {@code LOG_FORMAT} por reflexión (los setters quedan
 * bloqueados tras la inicialización), restaurando el valor original en un
 * bloque {@code try/finally}.<br>
 * El test de cierre ({@code testZZ_...}) se ejecuta al final gracias a
 * {@link FixMethodOrder}, apagando primero el {@link Kernel} (el módulo aún
 * operativo registra un log de cierre limpio) y luego el módulo.
 * <br><br>
 * 
 * @see PyMApp_plat_jse
 * @see TestPyMApp_plat_jse
 * @see Kernel
 * @see ModUI
 * 
 * @author    OLMEDO Fernando R. {ferol.dev}
 * @version    1.6.2 25/07/30
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ITestPyMApp_plat_jse {
    @ClassRule
    public static TemporaryFolder tempFolder = new TemporaryFolder();
    
    private static final String APP_NAME = "TestApp";
    private static final String MOD_CONFIG_NAME = "PyMApp_plat_jse";
    private static final String CONFIG_EXTENS = ".config";
    private static final String LOG_EXTENS = ".log";
    
    private static File configDir;
    private static File logDir;
    
    /** Fixture del recurso de configuración del módulo (formato texto). */
    private static final String MODULE_CONFIG_FIXTURE =
        "# Configuración de Prueba del Módulo PyMApp_plat_jse\n"+ 
        "mod.Log.TypeActive.SYSTEM=true\n" +
        "mod.Log.TypeActive.APPLICATION=true\n" +
        "mod.Log.TypeActive.EXCEPTION=true\n" +
        "mod.Log.TypeActive.AUDIT=true\n" +
        "mod.Log.TypeActive.DATABASE=true\n" +
        "mod.Log.TypeActive.DEBUG=false\n" +
        "mod.Log.LevelActive.EMERGENCY=true\n" +
        "mod.Log.LevelActive.ALERT=true\n" +
        "mod.Log.LevelActive.CRITICAL=true\n" +
        "mod.Log.LevelActive.ERROR=true\n" +
        "mod.Log.LevelActive.WARNING=true\n" +
        "mod.Log.LevelActive.NOTICE=true\n" +
        "mod.Log.LevelActive.INFORMATIONAL=true\n" +
        "mod.Log.LevelActive.DEBUG=false\n" +
        "mod.Log.Bytes=\n" +
        "mod.Log.Rotation=\n" +
        "mod.i18n.resPath=\n";
    
    /** Fixture de un recurso de configuración XML (DTD de java.util.Properties). */
    private static final String XML_RESOURCE_FIXTURE =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
        "<!DOCTYPE properties SYSTEM \"http://java.sun.com/dtd/properties.dtd\">\n" +
        "<properties>\n" +
        "  <entry key=\"clave1\">valor1</entry>\n" +
        "</properties>\n";
    
    /** Fixture de un recurso de configuración JSON (objeto plano multilínea). */
    private static final String JSON_RESOURCE_FIXTURE = "{\n" + "  \"clave1\": \"valor1\"\n" + "}\n";
    
    
/*----------------------------------------------------------------------------*/
/*                               Bootstrap                                    */
/*----------------------------------------------------------------------------*/
    
    
    /**
     * Bootstrap del {@link Kernel} en directorios temporales: creación de los
     * recursos de configuración (módulo, aplicación y módulo base), configuración
     * de las propiedades mínimas del {@link Kernel} (incluyendo el stub de UI)
     * e inicialización del mismo, que inicializa automáticamente el módulo de
     * plataforma {@link PyMApp_plat_jse}.
     * <br><br>
     * 
     * @throws Exception Si falla la creación de directorios o archivos
     *         temporales, o si el {@link Kernel} no puede inicializarse.
     */
    @BeforeClass
    public static void setUpClass() throws Exception {
        configDir = tempFolder.newFolder("config");
        logDir = tempFolder.newFolder("log");
        File appDir = tempFolder.newFolder("app");
        File modDir = tempFolder.newFolder("mod");
        
        Files.write(Paths.get(configDir.getAbsolutePath(), MOD_CONFIG_NAME + CONFIG_EXTENS),
            MODULE_CONFIG_FIXTURE.getBytes(StandardCharsets.UTF_8));
        Files.write(Paths.get(configDir.getAbsolutePath(), APP_NAME + CONFIG_EXTENS), new byte[0]);
        Files.write(Paths.get(configDir.getAbsolutePath(), "PyMApp_base" + CONFIG_EXTENS),
            "app.Locale=es\n".getBytes(StandardCharsets.UTF_8));
        
        Kernel kernel = Kernel.getInstance();
        kernel.setAppProductName("TestProduct");
        kernel.setAppName(APP_NAME);
        kernel.setAppVersion("1.0.0");
        kernel.setAppPath(appDir.getAbsolutePath());
        kernel.setAppModPath(modDir.getAbsolutePath());
        kernel.setAppModPLAT(PyMApp_plat_jse.getInstance());
        kernel.setAppModUI(new StubModUI());
        kernel.setAppConfigPath(configDir.getAbsolutePath());
        kernel.setAppConfig(new Config(configDir.getAbsolutePath(), APP_NAME));
        kernel.setAppLogPath(logDir.getAbsolutePath());
        kernel.setAppI18n(new I18n("dev.ferol.pymapp.base.i18n"));
        kernel.setAppLocale(new Locale("es"));
        
        kernel.initialize(null);
    }
    
    
    /**
     * Secuencia de cierre defensiva: detiene el módulo de plataforma y el
     * {@link Kernel} si alguno de ellos aún se encuentra operativo, sin
     * lanzar excepción.
     */
    @AfterClass
    public static void tearDownClass() {
        PyMApp_plat_jse mod = PyMApp_plat_jse.getInstance();
        ModState modState = mod.getState();
        
        if (modState == ModState.RUNNING || modState == ModState.INITIALIZING) {
            mod.shutdown();
        }
        
        Kernel kernel = Kernel.getInstance();
        KernelState kernelState = kernel.getState();
        
        if (kernelState == KernelState.RUNNING || kernelState == KernelState.INITIALIZING) {
            kernel.shutdown();
        }
    }
    
    
/*----------------------------------------------------------------------------*/
/*                          Estado y Entidad del Módulo                       */
/*----------------------------------------------------------------------------*/
    
    
    /**
     * Verifica el estado del módulo tras la inicialización del {@link Kernel}:
     * {@link ModState#RUNNING}, Singleton coherente e identificadores internos.
     */
    @Test
    public void test01_EstadoModuloInicializado() {
        PyMApp_plat_jse mod = PyMApp_plat_jse.getInstance();
        
        assertEquals(ModState.RUNNING, mod.getState());
        assertSame(mod, mod.getModManager());
        assertEquals("PyMApp_plat_jse", mod.getModNAME());
        assertEquals("1.6.2", mod.getModVERSION());
        assertNotNull(mod.getModI18n());
    }
    
    
    /**
     * Verifica el nombre público y la descripción del módulo a través del CCS
     * i18n, con el idioma de la aplicación forzado a español
     * ({@code app.Locale=es}).
     */
    @Test
    public void test02_ModDISPLAYNAMEyDESCRIPTION() {
        PyMApp_plat_jse mod = PyMApp_plat_jse.getInstance();
        
        assertEquals("PyMApp PLAT.mod - Java SE", mod.getModDISPLAYNAME());
        assertTrue(mod.getModDESCRIPTION().startsWith("Módulo de plataforma de PyMApp"));
    }
    
    
    /**
     * Verifica el recurso CCS i18n del módulo: lectura directa de la clave
     * {@code MOD_DISPLAYNAME} en español.
     */
    @Test
    public void test03_ModI18n() {
        PyMApp_plat_jse mod = PyMApp_plat_jse.getInstance();
        
        assertEquals("PyMApp PLAT.mod - Java SE", mod.getModI18n().getText("MOD_DISPLAYNAME", new Locale("es")));
    }
    
    
/*----------------------------------------------------------------------------*/
/*                    Configuración por Plataforma (ConfigPLAT)               */
/*----------------------------------------------------------------------------*/
    
    
    /**
     * Verifica la escritura y lectura de una clave de configuración en formato
     * texto mediante {@link PyMApp_plat_jse#setConfigKeyPLAT(String, String,
     * String, String, boolean)} y {@link PyMApp_plat_jse#getConfigKeyPLAT(
     * String, String, String)}.
     */
    @Test
    public void test04_ConfigKeyPLAT_Texto() throws Exception {
        PyMApp_plat_jse mod = PyMApp_plat_jse.getInstance();
        String resPath = configDir.getAbsolutePath();
        Path resFile = Paths.get(resPath, "ResTexto" + CONFIG_EXTENS);
        
        Files.write(resFile, new byte[0]); // El Recurso debe Existir Previamente
        
        mod.setConfigKeyPLAT(resPath, "ResTexto", "clave1", "valor1", true);
        
        assertEquals("valor1", mod.getConfigKeyPLAT(resPath, "ResTexto", "clave1"));
        assertTrue(Files.exists(resFile));
    }
    
    
    /**
     * Verifica la actualización de una clave existente ({@code newKey == false})
     * y la no inserción de una clave inexistente con {@code newKey == false}
     * en formato texto.
     */
    @Test
    public void test05_ConfigKeyPLAT_TextoActualizacion() throws Exception {
        PyMApp_plat_jse mod = PyMApp_plat_jse.getInstance();
        String resPath = configDir.getAbsolutePath();
        Path resFile = Paths.get(resPath, "ResTexto" + CONFIG_EXTENS);
        
        if (!Files.exists(resFile)) { // El Recurso debe Existir Previamente
            Files.write(resFile, new byte[0]);
        }
        
        mod.setConfigKeyPLAT(resPath, "ResTexto", "clave1", "valorActualizado", false);
        assertEquals("valorActualizado", mod.getConfigKeyPLAT(resPath, "ResTexto", "clave1"));
        
        mod.setConfigKeyPLAT(resPath, "ResTexto", "claveNoExistente", "valor", false);
        assertNull(mod.getConfigKeyPLAT(resPath, "ResTexto", "claveNoExistente"));
    }
    
    
    /**
     * Verifica la escritura y lectura de claves en formato XML: se conmuta el
     * campo {@code CONFIG_FORMAT} por reflexión (el setter queda bloqueado tras
     * la inicialización), se opera sobre un recurso XML existente y se restaura
     * el formato original.
     */
    @Test
    public void test06_ConfigKeyPLAT_Xml() throws Exception {
        PyMApp_plat_jse mod = PyMApp_plat_jse.getInstance();
        String resPath = configDir.getAbsolutePath();
        Path resFile = Paths.get(resPath, "ResXml" + CONFIG_EXTENS);
        
        Files.write(resFile, XML_RESOURCE_FIXTURE.getBytes(StandardCharsets.UTF_8));
        
        ConfigFORMAT original = mod.getConfigFormat();
        
        try {
            setConfigFormatField(ConfigFORMAT.XML);
            
            mod.setConfigKeyPLAT(resPath, "ResXml", "clave2", "valor2", true);
            assertEquals("valor2", mod.getConfigKeyPLAT(resPath, "ResXml", "clave2"));
            assertEquals("valor1", mod.getConfigKeyPLAT(resPath, "ResXml", "clave1"));
        } finally {
            setConfigFormatField(original);
        }
    }
    
    
    /**
     * Verifica la escritura y lectura de claves en formato JSON: se conmuta el
     * campo {@code CONFIG_FORMAT} por reflexión, se opera sobre un recurso JSON
     * existente y se restaura el formato original.
     */
    @Test
    public void test07_ConfigKeyPLAT_Json() throws Exception {
        PyMApp_plat_jse mod = PyMApp_plat_jse.getInstance();
        String resPath = configDir.getAbsolutePath();
        Path resFile = Paths.get(resPath, "ResJson" + CONFIG_EXTENS);
        
        Files.write(resFile, JSON_RESOURCE_FIXTURE.getBytes(StandardCharsets.UTF_8));
        
        ConfigFORMAT original = mod.getConfigFormat();
        
        try {
            setConfigFormatField(ConfigFORMAT.JSON);
            
            mod.setConfigKeyPLAT(resPath, "ResJson", "clave2", "valor2", true);
            assertEquals("valor2", mod.getConfigKeyPLAT(resPath, "ResJson", "clave2"));
            assertEquals("valor1", mod.getConfigKeyPLAT(resPath, "ResJson", "clave1"));
        } finally {
            setConfigFormatField(original);
        }
    }
    
    
    /**
     * Verifica los métodos de configuración del recurso predeterminado del
     * módulo: {@link PyMApp_plat_jse#setConfigKey(String, String)},
     * {@link PyMApp_plat_jse#setConfigNewKey(String, String)} y
     * {@link PyMApp_plat_jse#getConfigKey(String)}, incluyendo la actualización
     * de una clave existente y el tratamiento de valor {@code null} como
     * cadena vacía.
     */
    @Test
    public void test08_ConfigKey_RecursoModulo() throws Exception {
        PyMApp_plat_jse mod = PyMApp_plat_jse.getInstance();
        
        assertTrue(mod.setConfigNewKey("mod.test.clave", "valorNueva"));
        assertEquals("valorNueva", mod.getConfigKey("mod.test.clave"));
        
        assertTrue(mod.setConfigKey("mod.test.clave", "valorActualizada"));
        assertEquals("valorActualizada", mod.getConfigKey("mod.test.clave"));
        
        assertTrue(mod.setConfigKey("mod.test.vacia", null));
        assertEquals("", mod.getConfigKey("mod.test.vacia"));
    }
    
    
/*----------------------------------------------------------------------------*/
/*                          Log por Plataforma (LogPLAT)                      */
/*----------------------------------------------------------------------------*/
    
    
    /**
     * Verifica el registro de una línea de log en formato CSV mediante
     * {@link PyMApp_plat_jse#setLogLinePLAT(String, String, LogTYPE, LogLEVEL,
     * int, int, String, Object, Throwable, int)}: el archivo se crea en la
     * ruta indicada y la línea posee 9 campos separados por el carácter
     * {@code '|'}.
     */
    @Test
    public void test09_LogLinePLAT_Csv() throws Exception {
        PyMApp_plat_jse mod = PyMApp_plat_jse.getInstance();
        String resPath = logDir.getAbsolutePath();
        
        mod.setLogLinePLAT(resPath, "ResLog", LogTYPE.APPLICATION, LogLEVEL.INFORMATIONAL,
            256, 1, "mensaje de prueba", "datos adicionales", null, 0);
        
        Path logFile = Paths.get(resPath, "ResLog" + LOG_EXTENS);
        assertTrue(Files.exists(logFile));
        
        String line = Files.readAllLines(logFile, StandardCharsets.UTF_8).get(0);
        String[] fields = line.split("\\|", -1);
        
        assertEquals(9, fields.length);
        assertEquals("APPLICATION", fields[2]);
        assertEquals("INFORMATIONAL", fields[3]);
        assertEquals("mensaje de prueba", fields[5]);
        assertEquals("datos adicionales", fields[6]);
        assertEquals("", fields[7]);
        assertEquals("", fields[8]);
    }
    
    
    /**
     * Verifica el ruteo de los logs de auditoría: {@link PyMApp_plat_jse#setLogLine(
     * LogTYPE, LogLEVEL, String, Object, Throwable, int)} con
     * {@code logTYPE == LogTYPE.AUDIT} registra en el recurso de auditoría de
     * la aplicación {@code Audit.log}.
     */
    @Test
    public void test10_LogLine_Audit() throws Exception {
        PyMApp_plat_jse mod = PyMApp_plat_jse.getInstance();
        
        mod.setLogLine(LogTYPE.AUDIT, LogLEVEL.INFORMATIONAL, "evento de auditoría", "usuario test", null);
        
        Path auditFile = Paths.get(logDir.getAbsolutePath(), "Audit" + LOG_EXTENS);
        assertTrue(Files.exists(auditFile));
        
        String line = Files.readAllLines(auditFile, StandardCharsets.UTF_8).get(0);
        assertTrue(line.contains("|AUDIT|"));
    }
    
    
    /**
     * Verifica el registro de una línea de log en formato NDJSON: se conmuta el
     * campo {@code LOG_FORMAT} por reflexión, se registra una línea y se
     * restaura el formato original.
     */
    @Test
    public void test11_LogLinePLAT_Json() throws Exception {
        PyMApp_plat_jse mod = PyMApp_plat_jse.getInstance();
        String resPath = logDir.getAbsolutePath();
        
        LogFORMAT original = mod.getLogFormat();
        
        try {
            setLogFormatField(LogFORMAT.JSON);
            
            mod.setLogLinePLAT(resPath, "ResLogJson", LogTYPE.EXCEPTION, LogLEVEL.ERROR,
                256, 1, "mensaje json", "datos json", new IllegalArgumentException("falla"), 0);
        } finally {
            setLogFormatField(original);
        }
        
        Path logFile = Paths.get(resPath, "ResLogJson" + LOG_EXTENS);
        assertTrue(Files.exists(logFile));
        
        String line = Files.readAllLines(logFile, StandardCharsets.UTF_8).get(0);
        assertTrue(line.startsWith("{"));
        assertTrue(line.endsWith("}"));
        assertTrue(line.contains("\"logTYPE\":\"EXCEPTION\""));
        assertTrue(line.contains("\"logLEVEL\":\"ERROR\""));
        assertTrue(line.contains("\"message\":\"mensaje json\""));
        assertTrue(line.contains("\"exceptionMessage\":\"java.lang.IllegalArgumentException: falla\""));
    }
    
    
/*----------------------------------------------------------------------------*/
/*                          Contratos del Módulo (RUNNING)                    */
/*----------------------------------------------------------------------------*/
    
    
    /**
     * Verifica que los setters de las propiedades del módulo queden bloqueados
     * tras la inicialización: {@link PyMApp_plat_jse#setConfigFormat(ConfigFORMAT)},
     * {@link PyMApp_plat_jse#setConfigExtens(String)},
     * {@link PyMApp_plat_jse#setLogFormat(LogFORMAT)} y
     * {@link PyMApp_plat_jse#setLogExtens(String)} lanzan
     * {@link ModManagerIllegalStateException} con el módulo en
     * {@link ModState#RUNNING}.
     */
    @Test
    public void test12_SettersBloqueadosTrasInicializacion() {
        PyMApp_plat_jse mod = PyMApp_plat_jse.getInstance();
        
        assertEquals(ModState.RUNNING, mod.getState());
        
        assertThrows(ModManagerIllegalStateException.class, () -> mod.setConfigFormat(ConfigFORMAT.XML));
        assertThrows(ModManagerIllegalStateException.class, () -> mod.setConfigExtens(".conf"));
        assertThrows(ModManagerIllegalStateException.class, () -> mod.setLogFormat(LogFORMAT.JSON));
        assertThrows(ModManagerIllegalStateException.class, () -> mod.setLogExtens(".txt"));
    }
    
    
    /**
     * Verifica la protección contra la doble inicialización: un segundo
     * {@link PyMApp_plat_jse#initialize(String)} con el módulo en
     * {@link ModState#RUNNING} lanza
     * {@link ModManagerIllegalStateException} y el estado permanece
     * {@link ModState#RUNNING}.
     */
    @Test
    public void test13_InitializeDobleInicializacion() {
        PyMApp_plat_jse mod = PyMApp_plat_jse.getInstance();
        
        assertEquals(ModState.RUNNING, mod.getState());
        
        assertThrows(ModManagerIllegalStateException.class, () -> mod.initialize(null));
        assertEquals(ModState.RUNNING, mod.getState());
    }
    
    
    /**
     * Verifica el enrutamiento no AUDIT de {@link PyMApp_plat_jse#setLogLine(
     * LogTYPE, LogLEVEL, String, Object, Throwable, int)} hacia el recurso de
     * log del módulo y el filtrado por activación: con
     * {@code mod.Log.TypeActive.DEBUG=false} y
     * {@code mod.Log.LevelActive.DEBUG=false} (fixture), un registro de tipo y
     * nivel {@code DEBUG} no se escribe, mientras que
     * {@code APPLICATION/INFORMATIONAL} (activos) sí se registran.
     */
    @Test
    public void test14_SetLogLine_ModLog_ActivoInactivo() throws Exception {
        PyMApp_plat_jse mod = PyMApp_plat_jse.getInstance();
        Path modLogFile = Paths.get(logDir.getAbsolutePath(), MOD_CONFIG_NAME + LOG_EXTENS);
        
        mod.setLogLine(LogTYPE.DEBUG, LogLEVEL.DEBUG, "mensaje DEBUG inactivo", "dato", null);
        mod.setLogLine(LogTYPE.APPLICATION, LogLEVEL.INFORMATIONAL, "mensaje modulo activo", "dato", null);
        
        List<String> lines = Files.readAllLines(modLogFile, StandardCharsets.UTF_8);
        
        boolean foundActive = false;
        for (String line : lines) {
            if (line.contains("mensaje modulo activo")) {
                foundActive = true;
            }
            assertFalse("DEBUG no debe registrarse (inactivo en la configuración).", line.contains("mensaje DEBUG inactivo"));
        }
        assertTrue("El log del módulo debe contener el mensaje con tipo/nivel activos.", foundActive);
    }
    
    
    /**
     * Verifica las sobrecargas de {@link PyMApp_plat_jse#setLogLine(LogTYPE,
     * LogLEVEL, String, Object, Throwable)} (5 argumentos) y
     * {@link PyMApp_plat_jse#setLogLine(LogTYPE, LogLEVEL, String, Object)}
     * (4 argumentos): ambas registran la línea en el recurso de log del
     * módulo con el mensaje y los datos correspondientes.
     */
    @Test
    public void test15_SetLogLine_Sobrecargas() throws Exception {
        PyMApp_plat_jse mod = PyMApp_plat_jse.getInstance();
        Path modLogFile = Paths.get(logDir.getAbsolutePath(), MOD_CONFIG_NAME + LOG_EXTENS);
        
        mod.setLogLine(LogTYPE.EXCEPTION, LogLEVEL.ERROR, "mensaje sobrecarga 5", "dato",
            new IllegalArgumentException("falla sobrecarga 5"));
        mod.setLogLine(LogTYPE.APPLICATION, LogLEVEL.WARNING, "mensaje sobrecarga 4", "dato4");
        
        String content = new String(Files.readAllBytes(modLogFile), StandardCharsets.UTF_8);
        assertTrue(content.contains("mensaje sobrecarga 5"));
        assertTrue(content.contains("falla sobrecarga 5"));
        assertTrue(content.contains("mensaje sobrecarga 4"));
    }
    
    
    /**
     * Verifica la rotación de archivos de log a través de
     * {@link PyMApp_plat_jse#setLogLinePLAT(String, String, LogTYPE, LogLEVEL,
     * int, int, String, Object, Throwable, int)} con {@code numRotation == 3}
     * y el patrón {@code %g}: al superar {@code maxBytes} se generan los
     * archivos {@code Rot_0.log}, {@code Rot_1.log} y {@code Rot_2.log}, sin
     * existir {@code Rot_3.log}.
     */
    @Test
    public void test16_LogLinePLAT_Rotacion() throws Exception {
        PyMApp_plat_jse mod = PyMApp_plat_jse.getInstance();
        String resPath = logDir.getAbsolutePath();
        String bigMessage = bigMessage(300);
        
        for (int i = 0; i < 6; i++) {
            mod.setLogLinePLAT(resPath, "Rot", LogTYPE.APPLICATION,LogLEVEL.INFORMATIONAL,256,3,bigMessage,null,null,0);
        }
        
        assertTrue(Files.exists(Paths.get(resPath, "Rot_0.log")));
        assertTrue(Files.exists(Paths.get(resPath, "Rot_1.log")));
        assertTrue(Files.exists(Paths.get(resPath, "Rot_2.log")));
        assertFalse(Files.exists(Paths.get(resPath, "Rot_3.log")));
    }
    
    
/*----------------------------------------------------------------------------*/
/*                                 Cierre                                     */
/*----------------------------------------------------------------------------*/
    
    
    /**
     * Verifica la secuencia de cierre ordenada: primero el {@link Kernel}
     * (módulo aún operativo, lo que permite un registro de cierre limpio) y
     * luego el módulo de plataforma, que pasa a {@link ModState#STOPPED}.<br>
     * También verifica la idempotencia de {@link PyMApp_plat_jse#shutdown()}
     * y que un nuevo {@link PyMApp_plat_jse#initialize(String)} resulte en
     * {@link KernelIllegalStateException} (Kernel ya detenido).
     */
    @Test
    public void testZZ_ShutdownSecuenciaOrdenada() throws Exception {
        Kernel kernel = Kernel.getInstance();
        PyMApp_plat_jse mod = PyMApp_plat_jse.getInstance();
        
        assertEquals(ModState.RUNNING, mod.getState());
        
        kernel.shutdown();
        assertEquals(KernelState.STOPPED, kernel.getState());
        assertEquals(ModState.RUNNING, mod.getState());
        
        mod.shutdown();
        assertEquals(ModState.STOPPED, mod.getState());
        
        Path modLogFile = Paths.get(logDir.getAbsolutePath(), MOD_CONFIG_NAME + LOG_EXTENS);
        List<String> modLogLines = Files.readAllLines(modLogFile, StandardCharsets.UTF_8);
        assertFalse(modLogLines.isEmpty());
        assertTrue(modLogLines.get(modLogLines.size() - 1).contains("Módulo Detenido con Exito."));
        
        mod.shutdown(); // Idempotente
        assertEquals(ModState.STOPPED, mod.getState());
        
        assertThrows(KernelIllegalStateException.class, () -> mod.initialize(null));
        assertEquals(ModState.STOPPED, mod.getState());
    }
    
    
/*----------------------------------------------------------------------------*/
/*                                   Helpers                                  */
/*----------------------------------------------------------------------------*/
    
    
    /**
     * Conmuta el campo privado {@code CONFIG_FORMAT} de
     * {@link PyMApp_plat_jse} por reflexión, utilizado por los tests de
     * formatos XML y JSON dado que el setter queda bloqueado tras la
     * inicialización del módulo.
     * <br><br>
     * 
     * @param value Formato de configuración a establecer.
     * 
     * @throws Exception Si no es posible acceder o modificar el campo.
     */
    private static void setConfigFormatField(ConfigFORMAT value) throws Exception {
        Field formatField = PyMApp_plat_jse.class.getDeclaredField("CONFIG_FORMAT");
        formatField.setAccessible(true);
        formatField.set(PyMApp_plat_jse.getInstance(), value);
    }
    
    
    /**
     * Conmuta el campo privado {@code LOG_FORMAT} de {@link PyMApp_plat_jse}
     * por reflexión, utilizado por los tests del formato JSON dado que el
     * setter queda bloqueado tras la inicialización del módulo.
     * <br><br>
     * 
     * @param value Formato de log a establecer.
     * 
     * @throws Exception Si no es posible acceder o modificar el campo.
     */
    private static void setLogFormatField(LogFORMAT value) throws Exception {
        Field formatField = PyMApp_plat_jse.class.getDeclaredField("LOG_FORMAT");
        formatField.setAccessible(true);
        formatField.set(PyMApp_plat_jse.getInstance(), value);
    }
    
    
    /**
     * Genera un mensaje de la longitud indicada (caracteres {@code 'x'}),
     * utilizado para superar el {@code maxBytes} de los recursos de log y
     * forzar la rotación o el truncado.
     * <br><br>
     * 
     * @param length Longitud deseada del mensaje.
     * 
     * @return Mensaje de la longitud indicada.
     */
    private static String bigMessage(int length) {
        return new String(new char[length]).replace('\0', 'x');
    }
    
    
/*----------------------------------------------------------------------------*/
/*                                   Stub UI                                  */
/*----------------------------------------------------------------------------*/
    
    
    /**
     * Stub de módulo de interfaz de usuario ({@link ModUI}) para el bootstrap
     * del {@link Kernel}: implementa la secuencia mínima de estados
     * {@link ModState#CREATED} → {@link ModState#RUNNING} →
     * {@link ModState#STOPPED} requerida por {@link Kernel#initialize(String)}
     * y {@link Kernel#shutdown()}.
     * <br><br>
     * 
     * @see ModUI
     */
    private static final class StubModUI implements ModUI<Object, Object, Object, Object> {
        private ModState state = ModState.CREATED;
        
        
        @Override
        public ModManager getModManager() {
            return this;
        }
        
        @Override
        public void initialize(String args) {
            state = ModState.RUNNING;
        }
        
        @Override
        public ModState getState() {
            return state;
        }
        
        @Override
        public String getModNAME() {
            return "StubModUI";
        }
        
        @Override
        public String getModVERSION() {
            return "1.0.0";
        }
        
        @Override
        public String getModDISPLAYNAME() {
            return "Stub ModUI";
        }
        
        @Override
        public String getModDESCRIPTION() {
            return "Módulo de UI de prueba para los tests de integración.";
        }
        
        @Override
        public void shutdown() {
            state = ModState.STOPPED;
        }
        
        @Override
        public void setAppMainWin(Object appMainWin) {
            // No Operativo en el Stub
        }
        
        @Override
        public void setAppTitle(String title) {
            // No Operativo en el Stub
        }
        
        @Override
        public void setOnLoginAgain(Runnable onLoginAgain) {
            // No Operativo en el Stub
        }
        
        @Override
        public Object getAppMainWin() {
            return null;
        }
        
        @Override
        public String getAppTitle() {
            return null;
        }
        
        @Override
        public Runnable getOnLoginAgain() {
            return null;
        }
        
        @Override
        public Object toImage(ImageUI value, int width, int height) {
            return null;
        }
        
        @Override
        public Object toIcon(ImageUI value) {
            return null;
        }
        
        @Override
        public void showMessageDialog(Object owner, String title, String message, DialogMsgTYPE msgType) {
            // No Operativo en el Stub
        }
        
        @Override
        public DialogConfOPTION showConfirmDialog(
            Object owner, String title, String message, DialogMsgTYPE msgType, DialogConfTYPE confType
        ) {
            return DialogConfOPTION.YES_OPTION;
        }
    }
}