package com.capstone.web.member.validation;

import com.capstone.web.member.dto.MemberRegisterRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordMatchesValidator implements ConstraintValidator<PasswordMatches, MemberRegisterRequest> {

    @Override
    public boolean isValid(MemberRegisterRequest value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        boolean matches = value.password() != null && value.password().equals(value.passwordConfirm());

        if (!matches) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                    .addPropertyNode("passwordConfirm")
                    .addConstraintViolation();
        }

        return matches;
    }
}
