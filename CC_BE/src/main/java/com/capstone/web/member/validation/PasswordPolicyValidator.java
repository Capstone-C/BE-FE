package com.capstone.web.member.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

public class PasswordPolicyValidator implements ConstraintValidator<PasswordPolicy, String> {

    private static final Pattern ALLOWED_CHAR_PATTERN = Pattern.compile("^[A-Za-z0-9!@#$%^&*]+$");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return false;
        }

        if (value.length() < 8 || value.length() > 20) {
            return false;
        }

        if (!ALLOWED_CHAR_PATTERN.matcher(value).matches()) {
            return false;
        }

        int categories = 0;
        if (value.chars().anyMatch(Character::isUpperCase)) {
            categories++;
        }
        if (value.chars().anyMatch(Character::isLowerCase)) {
            categories++;
        }
        if (value.chars().anyMatch(Character::isDigit)) {
            categories++;
        }
        if (value.chars().anyMatch(ch -> "!@#$%^&*".indexOf(ch) >= 0)) {
            categories++;
        }

        return categories >= 2;
    }
}
