
package com.capstone.web.member.validation;

import com.capstone.web.member.dto.MemberRegisterRequest;
import com.capstone.web.member.dto.MemberPasswordChangeRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordMatchesValidator implements ConstraintValidator<PasswordMatches, Object> {

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        String password = null;
        String passwordConfirm = null;
        String confirmField = "passwordConfirm";

        if (value instanceof MemberRegisterRequest req) {
            password = req.password();
            passwordConfirm = req.passwordConfirm();
            confirmField = "passwordConfirm";
        } else if (value instanceof MemberPasswordChangeRequest req) {
            password = req.newPassword();
            passwordConfirm = req.newPasswordConfirm();
            confirmField = "newPasswordConfirm";
        }

        boolean matches = password != null && password.equals(passwordConfirm);

        if (!matches) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                    .addPropertyNode(confirmField)
                    .addConstraintViolation();
        }

        return matches;
    }
}
