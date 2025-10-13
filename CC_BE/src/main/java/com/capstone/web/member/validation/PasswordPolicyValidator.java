package com.capstone.web.member.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

public class PasswordPolicyValidator implements ConstraintValidator<PasswordPolicy, String> {

    private static final Pattern ALLOWED_CHAR_PATTERN = Pattern.compile("^[A-Za-z0-9!@#$%^&*]+$");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("비밀번호를 입력해주세요.")
                    .addConstraintViolation();
            return false;
        }

        boolean valid = true;

        if (value.length() < 8 || value.length() > 20) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("비밀번호는 8자 이상 20자 이하여야 합니다.")
                    .addConstraintViolation();
            valid = false;
        }

        if (!ALLOWED_CHAR_PATTERN.matcher(value).matches()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("비밀번호는 영문, 숫자, !@#$%^&* 만 사용할 수 있습니다.")
                    .addConstraintViolation();
            valid = false;
        }

        int categories = 0;
        if (value.chars().anyMatch(Character::isUpperCase)) categories++;
        if (value.chars().anyMatch(Character::isLowerCase)) categories++;
        if (value.chars().anyMatch(Character::isDigit)) categories++;
        if (value.chars().anyMatch(ch -> "!@#$%^&*".indexOf(ch) >= 0)) categories++;
        if (categories < 2) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("대문자/소문자/숫자/특수문자 중 2종 이상을 포함해야 합니다.")
                    .addConstraintViolation();
            valid = false;
        }

        return valid;
    }
}
