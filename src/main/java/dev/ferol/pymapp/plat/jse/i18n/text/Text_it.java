/*
 * @(#)Text_it.java    1.6.2 25/07/30
 * 
 * Copyright (c) 1999-2025 OLMEDO Fernando R. {ferol.dev}
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */


package dev.ferol.pymapp.plat.jse.i18n.text;


import dev.ferol.pymapp.base.ccs.i18n.I18n;
import dev.ferol.pymapp.base.ccs.i18n.I18nListResBundle;


/**
 * Clase de internacionalización de recursos de texto del módulo PyMApp_plat_jse.
 * (Idioma Italiano)
 * <br><br>
 * 
 * @see I18n
 * @see I18n#getText(String)
 * @see I18n#getText(String, java.util.Locale)
 * @see I18nListResBundle
 *
 * @author    OLMEDO Fernando R. {ferol.dev}
 * @version    1.6.2 25/07/30
 */
public final class Text_it extends I18nListResBundle {
    private static final Object[][] RESOURCE = {
        {"MOD_DISPLAYNAME", "PyMApp PLAT.mod - Java SE"},
        {"MOD_DESCRIPTION", "Modulo di piattaforma PyMApp che implementa l'integrazione e l'esecuzione in un ambiente Java SE."},
    };
    
    
    /**
     * Proporcionar una matriz del tipo {@code Object[][]}, en donde cada
     * elemento de la matriz es un par de objetos. El primer elemento de cada
     * par es la clave y el segundo elemento es el valor asociado con esa clave,
     * en ambos casos debe ser de tipo {@code String}.
     * <br><br>
     *
     * @return Una matriz del tipo {@code Object[][]} que representa un par
     *         "[clave][valor]".
     */
    @Override
    protected Object[][] getContents() {
        return RESOURCE;
    }
}
