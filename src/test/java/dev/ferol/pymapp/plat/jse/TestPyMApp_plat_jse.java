/* 
 * @(#)TestPyMApp_plat_jse.java    1.6.2 25/07/30
 * 
 * Copyright (c) 1999-2025 OLMEDO Fernando R. {ferol.dev}
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */


package dev.ferol.pymapp.plat.jse;


import java.lang.reflect.Field;

import java.nio.file.Paths;

import dev.ferol.pymapp.base.exception.CCSResourceAccessException;
import dev.ferol.pymapp.base.exception.KernelIllegalStateException;
import dev.ferol.pymapp.base.exception.ModManagerIllegalStateException;
import dev.ferol.pymapp.base.kernel.Kernel;
import dev.ferol.pymapp.base.kernel.KernelState;
import dev.ferol.pymapp.base.mod.ModState;
import dev.ferol.pymapp.base.ccs.log.LogLEVEL;
import dev.ferol.pymapp.base.ccs.log.LogTYPE;

import dev.ferol.pymapp.plat.jse.ccs.ConfigFORMAT;
import dev.ferol.pymapp.plat.jse.ccs.LogFORMAT;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;


/**
 * Clase de pruebas unitarias del administrador del módulo de plataforma
 * {@link PyMApp_plat_jse}.<br>
 * Los tests se ejecutan con el módulo en estado {@link ModState#CREATED},
 * verificando el comportamiento del Singleton, los setters y getters de las
 * propiedades del módulo y la validación de operatividad mínima de los métodos
 * que la requieren.<br>
 * El Singleton {@code modManager} se restablece por reflexión en cada test
 * ({@link #setUp()}), de manera que la suite resulte independiente del orden
 * de ejecución y de la inicialización realizada por la clase de pruebas de
 * integración {@link ITestPyMApp_plat_jse} (que detiene el {@link Kernel}
 * al finalizar, dejándolo en {@link KernelState#STOPPED} o
 * {@link KernelState#CREATED}).
 * <br><br>
 * 
 * @see PyMApp_plat_jse
 * @see ITestPyMApp_plat_jse
 * 
 * @author    OLMEDO Fernando R. {ferol.dev}
 * @version    1.6.2 25/07/30
 */
public class TestPyMApp_plat_jse {
    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();
    
    
/*----------------------------------------------------------------------------*/
/*                                 Setup                                      */
/*----------------------------------------------------------------------------*/
    
    
    /**
     * Restablece el Singleton {@code modManager} de {@link PyMApp_plat_jse}
     * por reflexión, garantizando una instancia nueva en estado
     * {@link ModState#CREATED} para cada test.
     * <br><br>
     * 
     * @throws Exception Si no es posible acceder o modificar el campo
     *         {@code modManager}.
     */
    @Before
    public void setUp() throws Exception {
        Field modManagerField = PyMApp_plat_jse.class.getDeclaredField("modManager");
        modManagerField.setAccessible(true);
        modManagerField.set(null, null);
    }
    
    
/*----------------------------------------------------------------------------*/
/*                                  Singleton                                 */
/*----------------------------------------------------------------------------*/
    
    
    /**
     * Verifica que {@link PyMApp_plat_jse#getInstance()} devuelva siempre la
     * misma instancia (patrón Singleton) y que dicha instancia sea la expuesta
     * por {@link PyMApp_plat_jse#getModManager()}.
     */
    @Test
    public void testGetInstance_Singleton() {
        PyMApp_plat_jse mod1 = PyMApp_plat_jse.getInstance();
        PyMApp_plat_jse mod2 = PyMApp_plat_jse.getInstance();
        
        assertSame(mod1, mod2);
        assertSame(mod1, mod1.getModManager());
    }
    
    
    /**
     * Verifica que el estado inicial del módulo recién instanciado sea
     * {@link ModState#CREATED}.
     */
    @Test
    public void testGetState_EstadoInicial() {
        assertEquals(ModState.CREATED, PyMApp_plat_jse.getInstance().getState());
    }
    
    
    /**
     * Verifica los identificadores internos del módulo, accesibles
     * independientemente del estado de inicialización.
     */
    @Test
    public void testIdentificadores_Modulo() {
        PyMApp_plat_jse mod = PyMApp_plat_jse.getInstance();
        
        assertEquals("PyMApp_plat_jse", mod.getModNAME());
        assertEquals("1.6.2", mod.getModVERSION());
    }
    
    
    /**
     * Verifica que el recurso CCS de internacionalización {@code modI18n}
     * devuelva {@code null} mientras el módulo no fue inicializado.
     */
    @Test
    public void testGetModI18n_SinInicializar() {
        assertNull(PyMApp_plat_jse.getInstance().getModI18n());
    }
    
    
/*----------------------------------------------------------------------------*/
/*                          Propiedades del Módulo                            */
/*----------------------------------------------------------------------------*/
    
    
    /**
     * Verifica los valores predeterminados de las propiedades del módulo:
     * formato y extensión de los recursos CCS de configuración y de log.
     */
    @Test
    public void testPropiedades_ValoresPredeterminados() {
        PyMApp_plat_jse mod = PyMApp_plat_jse.getInstance();
        
        assertEquals(ConfigFORMAT.TEXT, mod.getConfigFormat());
        assertEquals(".config", mod.getConfigExtens());
        assertEquals(LogFORMAT.CSV, mod.getLogFormat());
        assertEquals(".log", mod.getLogExtens());
    }
    
    
    /**
     * Verifica que {@link PyMApp_plat_jse#setConfigFormat(ConfigFORMAT)}
     * establezca el formato solicitado y que un valor {@code null} sea
     * ignorado, conservando el formato vigente.
     */
    @Test
    public void testSetConfigFormat() {
        PyMApp_plat_jse mod = PyMApp_plat_jse.getInstance();
        
        mod.setConfigFormat(ConfigFORMAT.XML);
        assertEquals(ConfigFORMAT.XML, mod.getConfigFormat());
        
        mod.setConfigFormat(null);
        assertEquals(ConfigFORMAT.XML, mod.getConfigFormat());
    }
    
    
    /**
     * Verifica que {@link PyMApp_plat_jse#setConfigExtens(String)}
     * establezca la extensión solicitada y que un valor {@code null} o en
     * blanco resulte en extensión vacía ({@code ""}).
     */
    @Test
    public void testSetConfigExtens() {
        PyMApp_plat_jse mod = PyMApp_plat_jse.getInstance();
        
        mod.setConfigExtens(".conf");
        assertEquals(".conf", mod.getConfigExtens());
        
        mod.setConfigExtens(null);
        assertEquals("", mod.getConfigExtens());
        
        mod.setConfigExtens(" ");
        assertEquals("", mod.getConfigExtens());
    }
    
    
    /**
     * Verifica que {@link PyMApp_plat_jse#setLogFormat(LogFORMAT)}
     * establezca el formato solicitado y que un valor {@code null} sea
     * ignorado, conservando el formato vigente.
     */
    @Test
    public void testSetLogFormat() {
        PyMApp_plat_jse mod = PyMApp_plat_jse.getInstance();
        
        mod.setLogFormat(LogFORMAT.JSON);
        assertEquals(LogFORMAT.JSON, mod.getLogFormat());
        
        mod.setLogFormat(null);
        assertEquals(LogFORMAT.JSON, mod.getLogFormat());
    }
    
    
    /**
     * Verifica que {@link PyMApp_plat_jse#setLogExtens(String)}
     * establezca la extensión solicitada y que un valor {@code null} o en
     * blanco resulte en extensión vacía ({@code ""}).
     */
    @Test
    public void testSetLogExtens() {
        PyMApp_plat_jse mod = PyMApp_plat_jse.getInstance();
        
        mod.setLogExtens(".txt");
        assertEquals(".txt", mod.getLogExtens());
        
        mod.setLogExtens(null);
        assertEquals("", mod.getLogExtens());
        
        mod.setLogExtens(" ");
        assertEquals("", mod.getLogExtens());
    }
    
    
/*----------------------------------------------------------------------------*/
/*                      Operatividad Mínima (Estado CREATED)                  */
/*----------------------------------------------------------------------------*/
    
    
    /**
     * Verifica que {@link PyMApp_plat_jse#setConfigKey(String, String)}
     * lance {@link ModManagerIllegalStateException} cuando el módulo no se
     * encuentra en {@link ModState#RUNNING}.
     */
    @Test
    public void testSetConfigKey_EstadoCREATED() {
        PyMApp_plat_jse mod = PyMApp_plat_jse.getInstance();
        
        assertThrows(ModManagerIllegalStateException.class, () -> mod.setConfigKey("clave", "valor"));
    }
    
    
    /**
     * Verifica que {@link PyMApp_plat_jse#setConfigNewKey(String, String)}
     * lance {@link ModManagerIllegalStateException} cuando el módulo no se
     * encuentra en {@link ModState#RUNNING}.
     */
    @Test
    public void testSetConfigNewKey_EstadoCREATED() {
        PyMApp_plat_jse mod = PyMApp_plat_jse.getInstance();
        
        assertThrows(ModManagerIllegalStateException.class, () -> mod.setConfigNewKey("clave", "valor"));
    }
    
    
    /**
     * Verifica que {@link PyMApp_plat_jse#getConfigKey(String)} lance
     * {@link ModManagerIllegalStateException} cuando el módulo no se encuentra
     * en {@link ModState#RUNNING} o {@link ModState#INITIALIZING}.
     */
    @Test
    public void testGetConfigKey_EstadoCREATED() {
        PyMApp_plat_jse mod = PyMApp_plat_jse.getInstance();
        
        assertThrows(ModManagerIllegalStateException.class, () -> mod.getConfigKey("clave"));
    }
    
    
    /**
     * Verifica que {@link PyMApp_plat_jse#setLogLine(LogTYPE, LogLEVEL,
     * String, Object, Throwable, int)} lance {@link ModManagerIllegalStateException}
     * cuando el módulo no se encuentra en {@link ModState#RUNNING} o
     * {@link ModState#INITIALIZING}.
     */
    @Test
    public void testSetLogLine_EstadoCREATED() {
        PyMApp_plat_jse mod = PyMApp_plat_jse.getInstance();
        
        assertThrows(ModManagerIllegalStateException.class, () -> mod.setLogLine(
            LogTYPE.APPLICATION, LogLEVEL.INFORMATIONAL, "mensaje", null, null, 0)
        );
    }
    
    
    /**
     * Verifica que {@link PyMApp_plat_jse#setConfigKeyPLAT(String, String,
     * String, String, boolean)} lance {@link ModManagerIllegalStateException}
     * cuando el módulo no se encuentra en {@link ModState#RUNNING}.
     */
    @Test
    public void testSetConfigKeyPLAT_EstadoCREATED() throws Exception {
        PyMApp_plat_jse mod = PyMApp_plat_jse.getInstance();
        String resPath = tempFolder.getRoot().getAbsolutePath();
        
        assertThrows(ModManagerIllegalStateException.class, () -> mod.setConfigKeyPLAT(
            resPath, "Recurso", "clave", "valor", true)
        );
    }
    
    
    /**
     * Verifica que {@link PyMApp_plat_jse#setLogLinePLAT(String, String,
     * LogTYPE, LogLEVEL, int, int, String, Object, Throwable, int)} lance
     * {@link ModManagerIllegalStateException} cuando el módulo no se encuentra
     * en {@link ModState#RUNNING} o {@link ModState#INITIALIZING}.
     */
    @Test
    public void testSetLogLinePLAT_EstadoCREATED() throws Exception {
        PyMApp_plat_jse mod = PyMApp_plat_jse.getInstance();
        String resPath = tempFolder.getRoot().getAbsolutePath();
        
        assertThrows(ModManagerIllegalStateException.class, () -> mod.setLogLinePLAT(
            resPath, "Recurso", LogTYPE.APPLICATION, LogLEVEL.INFORMATIONAL, 256, 1, "mensaje", null, null, 0)
        );
    }
    
    
    /**
     * Verifica que {@link PyMApp_plat_jse#getModDISPLAYNAME()} lance
     * {@link ModManagerIllegalStateException} cuando el módulo no se encuentra
     * en {@link ModState#RUNNING}.
     */
    @Test
    public void testGetModDISPLAYNAME_EstadoCREATED() {
        PyMApp_plat_jse mod = PyMApp_plat_jse.getInstance();
        
        assertThrows(ModManagerIllegalStateException.class, () -> mod.getModDISPLAYNAME());
    }
    
    
    /**
     * Verifica que {@link PyMApp_plat_jse#getModDESCRIPTION()} lance
     * {@link ModManagerIllegalStateException} cuando el módulo no se encuentra
     * en {@link ModState#RUNNING}.
     */
    @Test
    public void testGetModDESCRIPTION_EstadoCREATED() {
        PyMApp_plat_jse mod = PyMApp_plat_jse.getInstance();
        
        assertThrows(ModManagerIllegalStateException.class, () -> mod.getModDESCRIPTION());
    }
    
    
    /**
     * Verifica que {@link PyMApp_plat_jse#shutdown()} sobre un módulo en
     * estado {@link ModState#CREATED} retorne sin efecto, conservando el
     * estado {@link ModState#CREATED} (el cierre solo aplica sobre estados
     * {@link ModState#INITIALIZING} o {@link ModState#RUNNING}).
     */
    @Test
    public void testShutdown_EstadoCREATED() {
        PyMApp_plat_jse mod = PyMApp_plat_jse.getInstance();
        
        mod.shutdown();
        assertEquals(ModState.CREATED, mod.getState());
    }
    
    
/*----------------------------------------------------------------------------*/
/*                            getConfigKeyPLAT (CREATED)                      */
/*----------------------------------------------------------------------------*/
    
    
    /**
     * Verifica el comportamiento particular de
     * {@link PyMApp_plat_jse#getConfigKeyPLAT(String, String, String)}: no
     * valida el estado del módulo antes de leer el recurso, por lo que con el
     * módulo en {@link ModState#CREATED} y un recurso existente accesible
     * devuelve el valor de la clave ({@code null} si no existe) sin lanzar
     * excepción.
     */
    @Test
    public void testGetConfigKeyPLAT_ArchivoValidoClaveInexistente() throws Exception {
        PyMApp_plat_jse mod = PyMApp_plat_jse.getInstance();
        String resPath = tempFolder.getRoot().getAbsolutePath();
        java.nio.file.Files.write(Paths.get(resPath, "Recurso.config"), new byte[0]);
        
        assertNull(mod.getConfigKeyPLAT(resPath, "Recurso", "clave"));
    }
    
    
    /**
     * Verifica que ante un recurso inaccesible,
     * {@link PyMApp_plat_jse#getConfigKeyPLAT(String, String, String)} falle
     * al intentar registrar el log del error (módulo en
     * {@link ModState#CREATED}), propagando {@link ModManagerIllegalStateException}
     * en lugar de {@link CCSResourceAccessException}.
     */
    @Test
    public void testGetConfigKeyPLAT_ArchivoInexistente() throws Exception {
        PyMApp_plat_jse mod = PyMApp_plat_jse.getInstance();
        String resPath = tempFolder.getRoot().getAbsolutePath();
        
        assertThrows(ModManagerIllegalStateException.class, () -> mod.getConfigKeyPLAT(resPath, "NoExiste", "clave"));
    }
    
    
    /**
     * Verifica la validación de parámetros de
     * {@link PyMApp_plat_jse#getConfigKeyPLAT(String, String, String)}: una
     * clave {@code null} o vacía lanza {@link IllegalArgumentException}.
     */
    @Test
    public void testGetConfigKeyPLAT_ParametrosInvalidos() throws Exception {
        PyMApp_plat_jse mod = PyMApp_plat_jse.getInstance();
        String resPath = tempFolder.getRoot().getAbsolutePath();
        
        assertThrows(IllegalArgumentException.class, () -> mod.getConfigKeyPLAT(resPath, "Recurso", null));
        assertThrows(IllegalArgumentException.class, () -> mod.getConfigKeyPLAT(resPath, "Recurso", " "));
        assertThrows(IllegalArgumentException.class, () -> mod.getConfigKeyPLAT(resPath, null, "clave"));
        assertThrows(IllegalArgumentException.class, () -> mod.getConfigKeyPLAT(null, "Recurso", "clave"));
    }
    
    
/*----------------------------------------------------------------------------*/
/*                                Inicialización                              */
/*----------------------------------------------------------------------------*/
    
    
    /**
     * Verifica que {@link PyMApp_plat_jse#initialize(String)} lance
     * {@link KernelIllegalStateException} cuando el {@link Kernel} no se
     * encuentra en {@link KernelState#RUNNING} o
     * {@link KernelState#INITIALIZING}, dejando el módulo en
     * {@link ModState#CREATED}.
     */
    @Test
    public void testInitialize_KernelSinInicializar() {
        KernelState kernelState = Kernel.getInstance().getState();
        
        assertTrue("Precondición: el Kernel no debe estar operativo.",
            kernelState != KernelState.RUNNING && kernelState != KernelState.INITIALIZING);
        
        PyMApp_plat_jse mod = PyMApp_plat_jse.getInstance();
        
        assertThrows(KernelIllegalStateException.class, () -> mod.initialize(null));
        assertEquals(ModState.CREATED, mod.getState());
    }
}
