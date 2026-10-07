package com.tonci.appbnb.domain.validation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class FormValidatorTest {

    private static final String PHONE = "71234567";
    private static final String CARD = "1234567890";

    @Test
    public void validForm_hasNoErrors() {
        assertFalse(FormValidator.validate(PHONE, CARD, "1A").hasErrors());
    }

    @Test
    public void complementIsOptional() {
        assertFalse(FormValidator.validate(PHONE, CARD, "").hasErrors());
        assertFalse(FormValidator.validate(PHONE, CARD, null).hasErrors());
    }

    @Test
    public void emptyRequiredFields_areRequired() {
        FormErrors errors = FormValidator.validate("", null, "");
        assertEquals(ValidationError.REQUIRED, errors.getPhone());
        assertEquals(ValidationError.REQUIRED, errors.getIdentityCard());
        assertNull(errors.getComplement());
    }

    @Test
    public void phone_mustBeExactlyEightDigits() {
        assertEquals(ValidationError.INVALID_LENGTH,
                FormValidator.validate("7123456", CARD, "").getPhone());
        assertEquals(ValidationError.INVALID_LENGTH,
                FormValidator.validate("712345678", CARD, "").getPhone());
    }

    @Test
    public void phone_rejectsNonDigits() {
        assertEquals(ValidationError.INVALID_FORMAT,
                FormValidator.validate("7123456a", CARD, "").getPhone());
        assertEquals(ValidationError.INVALID_FORMAT,
                FormValidator.validate("+7123456", CARD, "").getPhone());
        assertEquals(ValidationError.INVALID_FORMAT,
                FormValidator.validate("7123 567", CARD, "").getPhone());
    }

    @Test
    public void digits_rejectNonAsciiDigits() {
        // Dígitos árabe-índicos: \p{Nd} los aceptaría, pero no son válidos aquí.
        assertEquals(ValidationError.INVALID_FORMAT,
                FormValidator.validate("١٢٣٤٥٦٧٨", CARD, "")
                        .getPhone());
    }

    @Test
    public void identityCard_mustBeExactlyTenDigits() {
        assertEquals(ValidationError.INVALID_LENGTH,
                FormValidator.validate(PHONE, "123456789", "").getIdentityCard());
        assertEquals(ValidationError.INVALID_LENGTH,
                FormValidator.validate(PHONE, "12345678901", "").getIdentityCard());
        assertEquals(ValidationError.INVALID_FORMAT,
                FormValidator.validate(PHONE, "12345678-0", "").getIdentityCard());
    }

    @Test
    public void complement_acceptsLettersAndDigits() {
        assertNull(FormValidator.validate(PHONE, CARD, "AB").getComplement());
        assertNull(FormValidator.validate(PHONE, CARD, "1a").getComplement());
        assertNull(FormValidator.validate(PHONE, CARD, "12").getComplement());
    }

    @Test
    public void complement_mustBeExactlyTwoCharactersWhenPresent() {
        assertEquals(ValidationError.INVALID_LENGTH,
                FormValidator.validate(PHONE, CARD, "A").getComplement());
        assertEquals(ValidationError.INVALID_LENGTH,
                FormValidator.validate(PHONE, CARD, "ABC").getComplement());
    }

    @Test
    public void complement_rejectsSpecialCharacters() {
        assertEquals(ValidationError.INVALID_FORMAT,
                FormValidator.validate(PHONE, CARD, "A#").getComplement());
        assertEquals(ValidationError.INVALID_FORMAT,
                FormValidator.validate(PHONE, CARD, "Ñ1").getComplement());
        assertEquals(ValidationError.INVALID_FORMAT,
                FormValidator.validate(PHONE, CARD, " A").getComplement());
    }

    @Test
    public void errorsAreReportedPerField() {
        FormErrors errors = FormValidator.validate("123", CARD, "A#");
        assertTrue(errors.hasErrors());
        assertEquals(ValidationError.INVALID_LENGTH, errors.getPhone());
        assertNull(errors.getIdentityCard());
        assertEquals(ValidationError.INVALID_FORMAT, errors.getComplement());
    }
}
