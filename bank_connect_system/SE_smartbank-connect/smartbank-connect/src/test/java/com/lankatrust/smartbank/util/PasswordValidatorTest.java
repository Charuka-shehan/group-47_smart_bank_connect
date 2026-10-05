package com.lankatrust.smartbank.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordValidatorTest {

    @Test
    void validPasswordAccepted() {
        assertTrue(PasswordValidator.isValid("Password@123"));
    }

    @Test
    void shortPasswordRejected() {
        assertFalse(PasswordValidator.isValid("Pass1!"));
    }

    @Test
    void missingSpecialCharacterRejected() {
        assertFalse(PasswordValidator.isValid("Password123"));
    }

    @Test
    void nullPasswordRejected() {
        assertFalse(PasswordValidator.isValid(null));
    }
}
