package com.tonci.appbnb.domain.validation;

import androidx.annotation.Nullable;

import java.util.regex.Pattern;

/**
 * Reglas de validación del formulario de información. Es Java puro (sin Android) para poder
 * probarlo con tests unitarios; la UI usa estas mismas constantes para limitar la entrada.
 */
public final class FormValidator {

    public static final int PHONE_LENGTH = 8;
    public static final int IDENTITY_CARD_LENGTH = 10;
    public static final int COMPLEMENT_LENGTH = 2;

    // Clases explícitas ASCII: así dígitos de otros alfabetos (p. ej. árabe-índicos) no pasan.
    private static final Pattern DIGITS = Pattern.compile("[0-9]+");
    private static final Pattern ALPHANUMERIC = Pattern.compile("[A-Za-z0-9]+");

    private FormValidator() { }

    public static FormErrors validate(@Nullable String phone,
                                      @Nullable String identityCard,
                                      @Nullable String complement) {
        return new FormErrors(
                validateDigits(phone, PHONE_LENGTH),
                validateDigits(identityCard, IDENTITY_CARD_LENGTH),
                validateComplement(complement));
    }

    /** Campo numérico obligatorio de longitud exacta. */
    @Nullable
    static ValidationError validateDigits(@Nullable String value, int length) {
        if (value == null || value.isEmpty()) return ValidationError.REQUIRED;
        if (!DIGITS.matcher(value).matches()) return ValidationError.INVALID_FORMAT;
        if (value.length() != length) return ValidationError.INVALID_LENGTH;
        return null;
    }

    /** El complemento es opcional; si se ingresa, son exactamente 2 letras/números. */
    @Nullable
    static ValidationError validateComplement(@Nullable String value) {
        if (value == null || value.isEmpty()) return null;
        if (!ALPHANUMERIC.matcher(value).matches()) return ValidationError.INVALID_FORMAT;
        if (value.length() != COMPLEMENT_LENGTH) return ValidationError.INVALID_LENGTH;
        return null;
    }
}
