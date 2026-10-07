package com.tonci.appbnb.ui.authentication;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;

/** Recomendación mostrada en el carrusel previo a la prueba de autenticación. */
public final class AuthTip {

    @StringRes private final int text;
    @DrawableRes private final int illustration;

    public AuthTip(@StringRes int text, @DrawableRes int illustration) {
        this.text = text;
        this.illustration = illustration;
    }

    @StringRes public int getText() { return text; }

    @DrawableRes public int getIllustration() { return illustration; }
}
