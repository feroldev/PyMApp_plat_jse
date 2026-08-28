/*
 * @(#)Text.java    1.6.2 25/07/30
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
 * (Idioma predeterminado Español {@link Text_es}).
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
public final class Text extends I18nListResBundle {
    private final Text_es predeterminado = new Text_es(); // Predetermina Text como Text_es
    
    
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
        return predeterminado.getContents();
    }
}
