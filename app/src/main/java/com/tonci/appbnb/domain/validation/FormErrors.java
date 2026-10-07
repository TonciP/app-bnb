package com.tonci.appbnb.domain.validation;

import androidx.annotation.Nullable;

/** Resultado de validar el formulario: un error opcional por campo. */
public final class FormErrors {

    public static final FormErrors NONE = new FormErrors(null, null, null);

    @Nullable private final ValidationError phone;
    @Nullable private final ValidationError identityCard;
    @Nullable private final ValidationError complement;

    public FormErrors(@Nullable ValidationError phone,
                      @Nullable ValidationError identityCard,
                      @Nullable ValidationError complement) {
        this.phone = phone;
        this.identityCard = identityCard;
        this.complement = complement;
    }

    @Nullable public ValidationError getPhone() { return phone; }

    @Nullable public ValidationError getIdentityCard() { return identityCard; }

    @Nullable public ValidationError getComplement() { return complement; }

    public boolean hasErrors() {
        return phone != null || identityCard != null || complement != null;
    }
}
