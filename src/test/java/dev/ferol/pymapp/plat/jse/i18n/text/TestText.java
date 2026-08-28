/* 
 * @(#)TestText.java    1.6.2 25/07/30
 * 
 * Copyright (c) 1999-2025 OLMEDO Fernando R. {ferol.dev}
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */


package dev.ferol.pymapp.plat.jse.i18n.text;


import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import dev.ferol.pymapp.base.ccs.i18n.I18n;
import dev.ferol.pymapp.base.ccs.i18n.I18nListResBundle;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


/**
 * Clase de pruebas unitarias de los recursos de internacionalización de
 * texto del módulo "PyMApp_plat_jse" ({@link Text} y los bundles por idioma
 * {@code Text_xx}).<br>
 * Se verifica que {@link Text} delegue en el idioma predeterminado
 * ({@link Text_es}) y que los 10 idiomas soportados contengan las claves
 * {@code MOD_DISPLAYNAME} y {@code MOD_DESCRIPTION} con valores no vacíos,
 * con un nombre público idéntico en todos los idiomas y descripciones
 * distintas entre sí.<br>
 * Adicionalmente se verifica la resolución del bundle a través del CCS
 * {@link I18n} con idioma explícito, sin requerir {@link Kernel} inicializado.
 * <br><br>
 * 
 * @see Text
 * @see Text_es
 * @see I18n
 * 
 * @author    OLMEDO Fernando R. {ferol.dev}
 * @version    1.6.2 25/07/30
 */
public class TestText {
    private static final String KEY_DISPLAYNAME = "MOD_DISPLAYNAME";
    private static final String KEY_DESCRIPTION = "MOD_DESCRIPTION";
    private static final String EXPECTED_DISPLAYNAME = "PyMApp PLAT.mod - Java SE";
    
    
/*----------------------------------------------------------------------------*/
/*                                    Text                                    */
/*----------------------------------------------------------------------------*/
    
    
    /**
     * Verifica que {@link Text} delegue sus contenidos en el idioma
     * predeterminado {@link Text_es}.
     */
    @Test
    public void testText_DelegaEnEspanol() {
        Map<String, Object> textContents = new Text().getContentsAsMap();
        Map<String, Object> textEsContents = new Text_es().getContentsAsMap();
        
        assertEquals(textEsContents, textContents);
    }
    
    
    /**
     * Verifica las claves del bundle español: {@code MOD_DISPLAYNAME} y
     * {@code MOD_DESCRIPTION} presentes y con valores no vacíos.
     */
    @Test
    public void testTextEs_Claves() {
        Map<String, Object> contents = new Text_es().getContentsAsMap();
        
        assertEquals(EXPECTED_DISPLAYNAME, contents.get(KEY_DISPLAYNAME));
        assertTrue(String.valueOf(contents.get(KEY_DESCRIPTION)).length() > 0);
    }
    
    
/*----------------------------------------------------------------------------*/
/*                                 Idiomas                                    */
/*----------------------------------------------------------------------------*/
    
    
    /**
     * Verifica la completitud de los 10 idiomas soportados
     * ({@code Text_es, Text_en, Text_de, Text_fr, Text_it, Text_ja, Text_ko,
     * Text_pt, Text_ru, Text_zh}): todos contienen las claves
     * {@code MOD_DISPLAYNAME} y {@code MOD_DESCRIPTION} con valores no vacíos,
     * el nombre público es idéntico en todos los idiomas y las descripciones
     * son todas distintas entre sí.
     */
    @Test
    public void testIdiomas_CompletitudYCoherencia() {
        Text_es es = new Text_es();
        Text_en en = new Text_en();
        Text_de de = new Text_de();
        Text_fr fr = new Text_fr();
        Text_it it = new Text_it();
        Text_ja ja = new Text_ja();
        Text_ko ko = new Text_ko();
        Text_pt pt = new Text_pt();
        Text_ru ru = new Text_ru();
        Text_zh zh = new Text_zh();
        
        Set<String> descriptions = new HashSet<String>();
        
        assertContents(es, descriptions);
        assertContents(en, descriptions);
        assertContents(de, descriptions);
        assertContents(fr, descriptions);
        assertContents(it, descriptions);
        assertContents(ja, descriptions);
        assertContents(ko, descriptions);
        assertContents(pt, descriptions);
        assertContents(ru, descriptions);
        assertContents(zh, descriptions);
        
        assertEquals("Las descripciones deben ser todas distintas entre sí.", 10, descriptions.size());
    }
    
    
/*----------------------------------------------------------------------------*/
/*                                 Integración                                */
/*----------------------------------------------------------------------------*/
    
    
    /**
     * Verifica la resolución del bundle {@code Text} a través del CCS
     * {@link I18n} con idioma explícito español, sin requerir el
     * {@link Kernel} inicializado.
     */
    @Test
    public void testI18n_BundleEspanol() {
        I18n i18n = new I18n("dev.ferol.pymapp.plat.jse.i18n");
        
        assertEquals(EXPECTED_DISPLAYNAME, i18n.getText(KEY_DISPLAYNAME, new Locale("es")));
    }
    
    
    /**
     * Verifica la resolución del bundle {@code Text} en inglés a través del
     * CCS {@link I18n}, con una descripción distinta de la española.
     */
    @Test
    public void testI18n_BundleIngles() {
        I18n i18n = new I18n("dev.ferol.pymapp.plat.jse.i18n");
        
        String englishDescription = i18n.getText(KEY_DESCRIPTION, new Locale("en"));
        String spanishDescription = i18n.getText(KEY_DESCRIPTION, new Locale("es"));
        
        assertTrue(englishDescription.startsWith("PyMApp platform module"));
        assertFalse(englishDescription.equals(spanishDescription));
    }
    
    
    /**
     * Verifica la resolución del bundle {@code Text} a través del CCS
     * {@link I18n} para los 10 idiomas soportados: el nombre público es
     * idéntico en todos ellos y la descripción resulta no vacía.
     */
    @Test
    public void testI18n_TodosLosIdiomasResuelven() {
        I18n i18n = new I18n("dev.ferol.pymapp.plat.jse.i18n");
        
        Locale[] locales = {
            new Locale("es"), new Locale("en"), new Locale("de"), new Locale("fr"),
            new Locale("it"), new Locale("ja"), new Locale("ko"), new Locale("pt"),
            new Locale("ru"), new Locale("zh")
        };
        
        for (Locale locale : locales) {
            assertEquals(EXPECTED_DISPLAYNAME, i18n.getText(KEY_DISPLAYNAME, locale));
            assertTrue("Descripción no vacía para el idioma [" + locale.getLanguage() + "].",
                i18n.getText(KEY_DESCRIPTION, locale).length() > 0);
        }
    }
    
    
/*----------------------------------------------------------------------------*/
/*                                   Helpers                                  */
/*----------------------------------------------------------------------------*/
    
    
    /**
     * Verifica que el bundle contenga las claves {@code MOD_DISPLAYNAME} y
     * {@code MOD_DESCRIPTION} con valores no vacíos y el nombre público
     * esperado, agregando la descripción al conjunto de descripciones.
     * <br><br>
     * 
     * @param contents Bundle de idioma a verificar.
     * @param descriptions Conjunto acumulador de descripciones.
     */
    private void assertContents(I18nListResBundle contents, Set<String> descriptions) {
        Map<String, Object> map = contents.getContentsAsMap();
        
        assertEquals(EXPECTED_DISPLAYNAME, map.get(KEY_DISPLAYNAME));
        assertTrue(String.valueOf(map.get(KEY_DESCRIPTION)).length() > 0);
        descriptions.add(String.valueOf(map.get(KEY_DESCRIPTION)));
    }
}