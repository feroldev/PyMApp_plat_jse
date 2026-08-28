/* 
 * @(#)TestConfigPLAT.java    1.6.2 25/07/30
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

import dev.ferol.pymapp.base.exception.CCSResourceAccessException;
import dev.ferol.pymapp.base.exception.CCSResourceFormatException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;


/**
 * Clase de pruebas unitarias de la implementación de plataforma del CCS de
 * configuración {@link ConfigPLAT}, para los tres formatos soportados:
 * texto plano ({@link ConfigPLAT#setTextKey(Path, String, String, boolean)}
 * y {@link ConfigPLAT#getTextKey(Path, String)}), XML de propiedades
 * ({@link ConfigPLAT#setXmlKey(Path, String, String, boolean)} y
 * {@link ConfigPLAT#getXmlKey(Path, String)}) y JSON de objeto plano
 * ({@link ConfigPLAT#setJsonKey(Path, String, String, boolean)} y
 * {@link ConfigPLAT#getJsonKey(Path, String)}).<br>
 * Cada test opera sobre un directorio temporal propio (regla
 * {@link TemporaryFolder}), creando los recursos necesarios dado que los
 * métodos validan la existencia y legibilidad del archivo
 * ({@code CCSResourceAccessException} en caso contrario).
 * <br><br>
 * 
 * @see ConfigPLAT
 * @see CCSResourceAccessException
 * @see CCSResourceFormatException
 * 
 * @author    OLMEDO Fernando R. {ferol.dev}
 * @version    1.6.2 25/07/30
 */
public class TestConfigPLAT {
    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();
    
    
/*----------------------------------------------------------------------------*/
/*                                   Texto                                    */
/*----------------------------------------------------------------------------*/
    
    
    /**
     * Verifica la actualización de una clave existente en formato texto,
     * preservando los comentarios y las demás claves del archivo.
     */
    @Test
    public void testTextKey_ActualizarExistente() throws Exception {
        Path resFile = writeFixture(
            "# Comentario Preservado\n" +
            "clave1=valor1\n" +
            "otra=otroValor\n");
        
        ConfigPLAT.setTextKey(resFile, "clave1", "valor2", false);
        
        assertEquals("valor2", ConfigPLAT.getTextKey(resFile, "clave1"));
        assertEquals("otroValor", ConfigPLAT.getTextKey(resFile, "otra"));
        
        List<String> lines = Files.readAllLines(resFile, StandardCharsets.UTF_8);
        assertTrue(lines.get(0).startsWith("# Comentario Preservado"));
        assertEquals(1, lines.stream().filter(line -> line.startsWith("clave1=")).count());
    }
    
    
    /**
     * Verifica la inserción de una clave nueva con {@code newKey == true} y
     * que con {@code newKey == false} la clave inexistente no se agregue.
     */
    @Test
    public void testTextKey_AgregarNueva() throws Exception {
        Path resFile = writeFixture("clave1=valor1\n");
        
        ConfigPLAT.setTextKey(resFile, "claveNueva", "valorNuevo", true);
        assertEquals("valorNuevo", ConfigPLAT.getTextKey(resFile, "claveNueva"));
        
        ConfigPLAT.setTextKey(resFile, "claveNoAgregada", "valor", false);
        assertNull(ConfigPLAT.getTextKey(resFile, "claveNoAgregada"));
    }
    
    
    /**
     * Verifica el round-trip de escape de claves y valores con caracteres
     * especiales (espacios, {@code =}, {@code :}, barra invertida) mediante
     * {@link ConfigPLAT#setTextKey(Path, String, String, boolean)} y
     * {@link ConfigPLAT#getTextKey(Path, String)}.
     */
    @Test
    public void testTextKey_EscapeRoundTrip() throws Exception {
        Path resFile = writeFixture("");
        String key = "clave con espacios=iguales:dos puntos";
        String value = "valor con = iguales : dos puntos \\ barra";
        
        ConfigPLAT.setTextKey(resFile, key, value, true);
        assertEquals(value, ConfigPLAT.getTextKey(resFile, key));
    }
    
    
    /**
     * Verifica que un valor {@code null} se trate como cadena vacía
     * ({@code ""}).
     */
    @Test
    public void testTextKey_ValorNull() throws Exception {
        Path resFile = writeFixture("");
        
        ConfigPLAT.setTextKey(resFile, "clave", null, true);
        assertEquals("", ConfigPLAT.getTextKey(resFile, "clave"));
    }
    
    
    /**
     * Verifica el reemplazo de una clave cuya línea original posee
     * continuación (barra invertida final): las líneas de continuación se
     * descartan y se escribe la clave reemplazada en una sola línea.
     */
    @Test
    public void testTextKey_Continuacion() throws Exception {
        Path resFile = writeFixture(
            "clave.multilinea=primera\\\n" +
            "    continua\n" +
            "otra=x\n");
        
        ConfigPLAT.setTextKey(resFile, "clave.multilinea", "nuevo", false);
        
        assertEquals("nuevo", ConfigPLAT.getTextKey(resFile, "clave.multilinea"));
        assertEquals("x", ConfigPLAT.getTextKey(resFile, "otra"));
    }
    
    
    /**
     * Verifica que una línea terminada en una cantidad par de barras
     * invertidas ({@code \\}) no abra continuación según la semántica de
     * {@code java.util.Properties}: la clave se reemplaza en su lugar y la
     * línea siguiente se preserva intacta.
     */
    @Test
    public void testTextKey_ContinuacionNoAbierta_BarraEscapada() throws Exception {
        Path resFile = writeFixture(
            "clave1=valor\\\\\n" +
            "otra=x\n");
        
        ConfigPLAT.setTextKey(resFile, "clave1", "nuevo", false);
        
        assertEquals("nuevo", ConfigPLAT.getTextKey(resFile, "clave1"));
        assertEquals("x", ConfigPLAT.getTextKey(resFile, "otra"));
    }
    
    
    /**
     * Verifica la escritura sobre un archivo vacío (formato texto no exige
     * estructura raíz) con {@code newKey == true}.
     */
    @Test
    public void testTextKey_ArchivoVacio() throws Exception {
        Path resFile = writeFixture("");
        
        ConfigPLAT.setTextKey(resFile, "clave", "valor", true);
        assertEquals("valor", ConfigPLAT.getTextKey(resFile, "clave"));
    }
    
    
    /**
     * Verifica que la lectura y escritura sobre un archivo inexistente lancen
     * {@link CCSResourceAccessException}.
     */
    @Test
    public void testTextKey_ArchivoInexistente() throws Exception {
        Path resFile = Paths.get(tempFolder.getRoot().getAbsolutePath(), "NoExiste.config");
        
        assertThrows(CCSResourceAccessException.class, () -> ConfigPLAT.getTextKey(resFile, "clave"));
        assertThrows(CCSResourceAccessException.class, () -> ConfigPLAT.setTextKey(resFile, "clave", "valor", true));
    }
    
    
    /**
     * Verifica la validación de parámetros: {@code resFile} o {@code key}
     * {@code null} o vacíos lanzan {@link IllegalArgumentException}.
     */
    @Test
    public void testTextKey_ParametrosInvalidos() throws Exception {
        Path resFile = writeFixture("clave=valor\n");
        
        assertThrows(IllegalArgumentException.class, () -> ConfigPLAT.getTextKey(null, "clave"));
        assertThrows(IllegalArgumentException.class, () -> ConfigPLAT.getTextKey(resFile, null));
        assertThrows(IllegalArgumentException.class, () -> ConfigPLAT.getTextKey(resFile, " "));
        assertThrows(IllegalArgumentException.class, () -> ConfigPLAT.setTextKey(null, "clave", "valor", true));
        assertThrows(IllegalArgumentException.class, () -> ConfigPLAT.setTextKey(resFile, " ", "valor", true));
    }
    
    
/*----------------------------------------------------------------------------*/
/*                                    XML                                     */
/*----------------------------------------------------------------------------*/
    
    
    /**
     * Verifica la actualización de una clave existente en formato XML,
     * preservando el comentario XML y la indentación de las demás entradas.
     */
    @Test
    public void testXmlKey_ActualizarExistente() throws Exception {
        Path resFile = writeFixture(
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<!DOCTYPE properties SYSTEM \"http://java.sun.com/dtd/properties.dtd\">\n" +
            "<!-- Comentario Preservado -->\n" +
            "<properties>\n" +
            "  <entry key=\"clave1\">valor1</entry>\n" +
            "  <entry key=\"clave2\">valor2</entry>\n" +
            "</properties>\n");
        
        ConfigPLAT.setXmlKey(resFile, "clave2", "valorActualizado", false);
        
        assertEquals("valorActualizado", ConfigPLAT.getXmlKey(resFile, "clave2"));
        assertEquals("valor1", ConfigPLAT.getXmlKey(resFile, "clave1"));
        
        String content = readContent(resFile);
        assertTrue(content.contains("<!-- Comentario Preservado -->"));
        assertTrue(content.contains("  <entry key=\"clave2\">valorActualizado</entry>"));
    }
    
    
    /**
     * Verifica la inserción de una clave nueva con {@code newKey == true}
     * (indentada según la referencia de la primera entrada) y que con
     * {@code newKey == false} la clave inexistente no se agregue.
     */
    @Test
    public void testXmlKey_AgregarNueva() throws Exception {
        Path resFile = writeFixture(
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<!DOCTYPE properties SYSTEM \"http://java.sun.com/dtd/properties.dtd\">\n" +
            "<properties>\n" +
            "  <entry key=\"clave1\">valor1</entry>\n" +
            "</properties>\n");
        
        ConfigPLAT.setXmlKey(resFile, "claveNueva", "valorNuevo", true);
        assertEquals("valorNuevo", ConfigPLAT.getXmlKey(resFile, "claveNueva"));
        assertTrue(readContent(resFile).contains("  <entry key=\"claveNueva\">valorNuevo</entry>"));
        
        ConfigPLAT.setXmlKey(resFile, "claveNoAgregada", "valor", false);
        assertNull(ConfigPLAT.getXmlKey(resFile, "claveNoAgregada"));
    }
    
    
    /**
     * Verifica el reemplazo de una entrada XML multi-línea por una entrada
     * de una sola línea.
     */
    @Test
    public void testXmlKey_EntradaMultilinea() throws Exception {
        Path resFile = writeFixture(
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<!DOCTYPE properties SYSTEM \"http://java.sun.com/dtd/properties.dtd\">\n" +
            "<properties>\n" +
            "  <entry key=\"multi\">\n" +
            "    linea interna\n" +
            "  </entry>\n" +
            "</properties>\n");
        
        ConfigPLAT.setXmlKey(resFile, "multi", "nuevo", false);
        
        assertEquals("nuevo", ConfigPLAT.getXmlKey(resFile, "multi"));
        String content = readContent(resFile);
        assertFalse(content.contains("linea interna"));
        assertTrue(content.contains("<entry key=\"multi\">nuevo</entry>"));
    }
    
    
    /**
     * Verifica el tratamiento de una entrada self-closing
     * ({@code <entry key="..."/>}): se considera de valor vacío y se
     * reemplaza por una entrada con el valor indicado.
     */
    @Test
    public void testXmlKey_SelfClosing() throws Exception {
        Path resFile = writeFixture(
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<!DOCTYPE properties SYSTEM \"http://java.sun.com/dtd/properties.dtd\">\n" +
            "<properties>\n" +
            "  <entry key=\"vacia\"/>\n" +
            "</properties>\n");
        
        ConfigPLAT.setXmlKey(resFile, "vacia", "valor", false);
        
        assertEquals("valor", ConfigPLAT.getXmlKey(resFile, "vacia"));
        assertTrue(readContent(resFile).contains("<entry key=\"vacia\">valor</entry>"));
    }
    
    
    /**
     * Verifica el round-trip de escape XML de caracteres reservados
     * ({@code <}, {@code >}, {@code &}, comillas) en los valores.
     */
    @Test
    public void testXmlKey_EscapeRoundTrip() throws Exception {
        Path resFile = writeFixture(
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<!DOCTYPE properties SYSTEM \"http://java.sun.com/dtd/properties.dtd\">\n" +
            "<properties>\n" +
            "</properties>\n");
        String value = "valor con <angulo> & ampersand \"comillas\"";
        
        ConfigPLAT.setXmlKey(resFile, "clave", value, true);
        assertEquals(value, ConfigPLAT.getXmlKey(resFile, "clave"));
    }
    
    
    /**
     * Verifica el round-trip de escape de claves XML con caracteres
     * reservados ({@code &}, comillas dobles y espacios) mediante
     * {@code XML.escapingAttr} en la escritura y {@code loadFromXML} en la
     * lectura.
     */
    @Test
    public void testXmlKey_ClaveConCaracteresEspeciales_EscapeRoundTrip() throws Exception {
        Path resFile = writeFixture(
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<!DOCTYPE properties SYSTEM \"http://java.sun.com/dtd/properties.dtd\">\n" +
            "<properties>\n" +
            "</properties>\n");
        String key = "clave & \"especial\" con espacios";
        
        ConfigPLAT.setXmlKey(resFile, key, "valor", true);
        
        assertEquals("valor", ConfigPLAT.getXmlKey(resFile, key));
        assertTrue(readContent(resFile).contains("clave &amp; &quot;especial&quot; con espacios"));
    }
    
    
    /**
     * Verifica que un archivo XML vacío lance {@link CCSResourceFormatException}
     * (estructura raíz obligatoria).
     */
    @Test
    public void testXmlKey_ArchivoVacio() throws Exception {
        Path resFile = writeFixture("");
        
        assertThrows(CCSResourceFormatException.class, () -> ConfigPLAT.setXmlKey(resFile, "clave", "valor", true));
    }
    
    
    /**
     * Verifica que la lectura y escritura sobre un archivo XML inexistente
     * lancen {@link CCSResourceAccessException}.
     */
    @Test
    public void testXmlKey_ArchivoInexistente() throws Exception {
        Path resFile = Paths.get(tempFolder.getRoot().getAbsolutePath(), "NoExiste.config");
        
        assertThrows(CCSResourceAccessException.class, () -> ConfigPLAT.getXmlKey(resFile, "clave"));
        assertThrows(CCSResourceAccessException.class, () -> ConfigPLAT.setXmlKey(resFile, "clave", "valor", true));
    }
    
    
/*----------------------------------------------------------------------------*/
/*                                    JSON                                    */
/*----------------------------------------------------------------------------*/
    
    
    /**
     * Verifica la actualización de una clave existente en formato JSON,
     * preservando la indentación y el formato de las demás entradas.
     */
    @Test
    public void testJsonKey_ActualizarExistente() throws Exception {
        Path resFile = writeFixture(
            "{\n" +
            "  \"clave1\": \"valor1\",\n" +
            "  \"clave2\": \"valor2\"\n" +
            "}\n");
        
        ConfigPLAT.setJsonKey(resFile, "clave2", "valorActualizado", false);
        
        assertEquals("valorActualizado", ConfigPLAT.getJsonKey(resFile, "clave2"));
        assertEquals("valor1", ConfigPLAT.getJsonKey(resFile, "clave1"));
        assertTrue(readContent(resFile).contains("  \"clave2\": \"valorActualizado\""));
    }
    
    
    /**
     * Verifica la inserción de una clave nueva con {@code newKey == true}
     * (sin coma final) y que con {@code newKey == false} la clave inexistente
     * no se agregue.
     */
    @Test
    public void testJsonKey_AgregarNueva() throws Exception {
        Path resFile = writeFixture(
            "{\n" +
            "  \"clave1\": \"valor1\"\n" +
            "}\n");
        
        ConfigPLAT.setJsonKey(resFile, "claveNueva", "valorNuevo", true);
        assertEquals("valorNuevo", ConfigPLAT.getJsonKey(resFile, "claveNueva"));
        
        String content = readContent(resFile);
        assertTrue(content.contains("  \"claveNueva\": \"valorNuevo\""));
        assertFalse(content.contains("valorNuevo\",\n")); // Última Entrada Sin Coma Final
        assertTrue(content.contains(",\n  \"claveNueva\"")); // Separada de la Entrada Previa
        assertTrue(content.contains("\"valor1\",\n  \"claveNueva\"")); // Coma Ajustada en la Entrada Previa
        
        ConfigPLAT.setJsonKey(resFile, "claveNoAgregada", "valor", false);
        assertNull(ConfigPLAT.getJsonKey(resFile, "claveNoAgregada"));
    }
    
    
    /**
     * Verifica la inserción de una clave nueva en un objeto vacío de una sola
     * línea ({@code {}}), que se normaliza en apertura, entrada y cierre con
     * las llaves en líneas propias.
     */
    @Test
    public void testJsonKey_ObjetoVacioLineaUnica() throws Exception {
        Path resFile = writeFixture("{}\n");
        
        ConfigPLAT.setJsonKey(resFile, "clave", "valor", true);
        assertEquals("valor", ConfigPLAT.getJsonKey(resFile, "clave"));
        
        String content = readContent(resFile);
        assertTrue(content.contains("{\n")); // Llave de Apertura en Línea Propia
        assertTrue(content.contains("\n}\n")); // Llave de Cierre en Línea Propia
    }
    
    
    /**
     * Verifica que la lectura de valores no textuales JSON (numéricos,
     * booleanos) devuelva {@code null} (el CCS de configuración solo soporta
     * valores de texto).
     */
    @Test
    public void testJsonKey_ValorNoTextual_LecturaDevuelveNull() throws Exception {
        Path resFile = writeFixture(
            "{\n" +
            "  \"numero\": 123,\n" +
            "  \"booleano\": true\n" +
            "}\n");
        
        assertNull(ConfigPLAT.getJsonKey(resFile, "numero"));
        assertNull(ConfigPLAT.getJsonKey(resFile, "booleano"));
    }
    
    
    /**
     * Verifica la unicidad de claves con valores no textuales: al actualizar
     * una clave existente cuyo valor es numérico, booleano o {@code null}, el
     * valor se reemplaza en el lugar por el texto indicado, sin agregar una
     * clave duplicada.
     */
    @Test
    public void testJsonKey_ValorNoTextual_ActualizacionInPlace() throws Exception {
        Path resFile = writeFixture(
            "{\n" +
            "  \"numero\": 123,\n" +
            "  \"booleano\": true,\n" +
            "  \"nulo\": null\n" +
            "}\n");
        
        ConfigPLAT.setJsonKey(resFile, "numero", "nuevo", true);
        assertEquals("nuevo", ConfigPLAT.getJsonKey(resFile, "numero"));
        
        ConfigPLAT.setJsonKey(resFile, "booleano", "si", false);
        assertEquals("si", ConfigPLAT.getJsonKey(resFile, "booleano"));
        
        ConfigPLAT.setJsonKey(resFile, "nulo", "vacio", true);
        assertEquals("vacio", ConfigPLAT.getJsonKey(resFile, "nulo"));
        
        String content = readContent(resFile);
        assertEquals("No debe existir más de una entrada por clave.", 1, content.split("numero", -1).length - 1);
        assertTrue(content.contains("\"numero\": \"nuevo\""));
        assertFalse(content.contains("\"numero\": 123"));
    }
    
    
    /**
     * Verifica la preservación de la coma final de la última entrada al
     * actualizar una clave con estilo de coma final ({@code "clave": "valor",}).
     */
    @Test
    public void testJsonKey_ComaFinal() throws Exception {
        Path resFile = writeFixture(
            "{\n" +
            "  \"clave1\": \"valor1\",\n" +
            "}\n");
        
        ConfigPLAT.setJsonKey(resFile, "clave1", "nuevo", false);
        assertTrue(readContent(resFile).contains("  \"clave1\": \"nuevo\","));
    }
    
    
    /**
     * Verifica el round-trip de escape JSON de comillas dobles y barra
     * invertida en los valores.
     */
    @Test
    public void testJsonKey_EscapeRoundTrip() throws Exception {
        Path resFile = writeFixture("{\n  \"clave\": \"v1\"\n}\n");
        String value = "valor con \"comillas\" y \\barra";
        
        ConfigPLAT.setJsonKey(resFile, "clave", value, false);
        assertEquals(value, ConfigPLAT.getJsonKey(resFile, "clave"));
    }
    
    
    /**
     * Documenta el comportamiento actual respecto de las claves JSON con
     * comillas dobles escapadas ({@code \"}): la escritura escapa la clave
     * correctamente (la entrada queda bien formada en el recurso), pero
     * {@link ConfigPLAT#getJsonKey(Path, String)} no soporta su relectura,
     * dado que el parseo de la clave se trunca en la comilla escapada y
     * devuelve {@code null}.
     */
    @Test
    public void testJsonKey_ClaveConComillaEscapada_LecturaNoSoportada() throws Exception {
        Path resFile = writeFixture("{\n  \"clave\": \"v1\"\n}\n");
        String key = "clave\"especial";
        
        ConfigPLAT.setJsonKey(resFile, key, "valor", true);
        
        assertTrue(readContent(resFile).contains("\"clave\\\"especial\": \"valor\""));
        assertNull(ConfigPLAT.getJsonKey(resFile, key));
    }
    
    
    /**
     * Verifica la normalización de un objeto JSON de una sola línea con
     * entradas: las llaves se mueven a líneas propias, se agregan y actualizan
     * claves sin pérdida de contenido.
     */
    @Test
    public void testJsonKey_ObjetoLineaUnica_Normalizacion() throws Exception {
        Path resFile = writeFixture("{\"clave1\": \"valor1\"}\n");
        
        ConfigPLAT.setJsonKey(resFile, "claveNueva", "valorNuevo", true);
        assertEquals("valorNuevo", ConfigPLAT.getJsonKey(resFile, "claveNueva"));
        assertEquals("valor1", ConfigPLAT.getJsonKey(resFile, "clave1"));
        
        String content = readContent(resFile);
        assertTrue(content.contains("{\n")); // Llave de Apertura en Línea Propia
        assertTrue(content.contains("\n}\n")); // Llave de Cierre en Línea Propia
        assertTrue(content.contains("\"clave1\": \"valor1\","));
        assertTrue(content.contains("\"claveNueva\": \"valorNuevo\""));
        
        ConfigPLAT.setJsonKey(resFile, "clave1", "valorActualizado", false);
        assertEquals("valorActualizado", ConfigPLAT.getJsonKey(resFile, "clave1"));
    }
    
    
    /**
     * Verifica la normalización de una línea que comparte únicamente la llave
     * de cierre con la última entrada ({@code "clave": "valor"}}): la llave se
     * mueve a línea propia y la coma de la última entrada se ajusta al agregar
     * una clave nueva.
     */
    @Test
    public void testJsonKey_ObjetoLineaUnica_LlaveCierreCompartida() throws Exception {
        Path resFile = writeFixture(
            "{\n" +
            "  \"clave1\": \"valor1\"}\n");
        
        ConfigPLAT.setJsonKey(resFile, "claveNueva", "valorNuevo", true);
        assertEquals("valorNuevo", ConfigPLAT.getJsonKey(resFile, "claveNueva"));
        assertEquals("valor1", ConfigPLAT.getJsonKey(resFile, "clave1"));
        
        String content = readContent(resFile);
        assertTrue(content.contains("  \"clave1\": \"valor1\","));
        assertTrue(content.contains("  \"claveNueva\": \"valorNuevo\""));
        assertTrue(content.contains("\n}\n")); // Llave de Cierre en Línea Propia
    }
    
    
    /**
     * Verifica el tratamiento de una línea de cierre con coma final
     * ({@code },}) en línea propia: al agregar una clave nueva se ajusta la
     * coma de la entrada previa, las llaves permanecen en líneas propias y no
     * hay pérdida de contenido.
     */
    @Test
    public void testJsonKey_LlaveCierreConComa_LineaPropia() throws Exception {
        Path resFile = writeFixture(
            "{\n" +
            "  \"clave1\": \"valor1\"\n" +
            "},\n");
        
        ConfigPLAT.setJsonKey(resFile, "claveNueva", "valorNuevo", true);
        
        assertEquals("valorNuevo", ConfigPLAT.getJsonKey(resFile, "claveNueva"));
        assertEquals("valor1", ConfigPLAT.getJsonKey(resFile, "clave1"));
        
        String content = readContent(resFile);
        assertTrue(content.contains("\"clave1\": \"valor1\","));
        assertTrue(content.contains("\"claveNueva\": \"valorNuevo\""));
        assertTrue(content.contains("\n},\n")); // Línea de Cierre Preservada en Línea Propia
    }
    
    
    /**
     * Verifica que una línea con más de una entrada
     * ({@code "a": "1", "b": "2"}) lance {@link CCSResourceFormatException}
     * (formato no soportado).
     */
    @Test
    public void testJsonKey_MultiplesEntradasEnLinea_FormatoInvalido() throws Exception {
        Path resFile = writeFixture("{\"a\": \"1\", \"b\": \"2\"}\n");
        
        assertThrows(CCSResourceFormatException.class, () -> ConfigPLAT.setJsonKey(resFile, "clave", "valor", true));
    }
    
    
    /**
     * Verifica que los objetos anidados lancen {@link CCSResourceFormatException}
     * (formato no soportado), tanto en una sola línea como en multilínea.
     */
    @Test
    public void testJsonKey_ObjetoAnidado_FormatoInvalido() throws Exception {
        Path resFile = writeFixture("{\"clave\": {\"anidado\": \"x\"}}\n");
        
        assertThrows(CCSResourceFormatException.class, () -> ConfigPLAT.setJsonKey(resFile, "clave", "valor", true));
        
        Path resFile2 = writeFixture(
            "{\n" +
            "  \"clave\": {\n" +
            "    \"anidado\": \"x\"\n" +
            "  }\n" +
            "}\n");
        
        assertThrows(CCSResourceFormatException.class, () -> ConfigPLAT.setJsonKey(resFile2, "clave", "valor", true));
    }
    
    
    /**
     * Verifica que un archivo JSON vacío lance {@link CCSResourceFormatException}
     * (estructura raíz obligatoria).
     */
    @Test
    public void testJsonKey_ArchivoVacio() throws Exception {
        Path resFile = writeFixture("");
        
        assertThrows(CCSResourceFormatException.class, () -> ConfigPLAT.setJsonKey(resFile, "clave", "valor", true));
    }
    
    
    /**
     * Verifica que la lectura y escritura sobre un archivo JSON inexistente
     * lancen {@link CCSResourceAccessException}.
     */
    @Test
    public void testJsonKey_ArchivoInexistente() throws Exception {
        Path resFile = Paths.get(tempFolder.getRoot().getAbsolutePath(), "NoExiste.config");
        
        assertThrows(CCSResourceAccessException.class, () -> ConfigPLAT.getJsonKey(resFile, "clave"));
        assertThrows(CCSResourceAccessException.class, () -> ConfigPLAT.setJsonKey(resFile, "clave", "valor", true));
    }
    
    
/*----------------------------------------------------------------------------*/
/*                                   Helpers                                  */
/*----------------------------------------------------------------------------*/
    
    
    /**
     * Crea el recurso de configuración con el contenido indicado en el
     * directorio temporal del test.
     * <br><br>
     * 
     * @param content Contenido del archivo, en codificación UTF-8.
     * 
     * @return Ruta del recurso creado.
     * 
     * @throws Exception Si falla la creación del archivo.
     */
    private Path writeFixture(String content) throws Exception {
        Path resFile = Paths.get(tempFolder.getRoot().getAbsolutePath(), "recurso.config");
        
        Files.write(resFile, content.getBytes(StandardCharsets.UTF_8));
        return resFile;
    }
    
    
    /**
     * Lee el contenido completo del recurso en codificación UTF-8.
     * <br><br>
     * 
     * @param resFile Ruta del recurso a leer.
     * 
     * @return Contenido del archivo.
     * 
     * @throws Exception Si falla la lectura del archivo.
     */
    private String readContent(Path resFile) throws Exception {
        return new String(Files.readAllBytes(resFile), StandardCharsets.UTF_8);
    }
}