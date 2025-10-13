package com.capstone.web.member.validation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = PasswordPolicyValidator.class)
@Target({FIELD})
@Retention(RUNTIME)
public @interface PasswordPolicy {

    String message() default "비밀번호는 8자 이상 20자 이하이며 대문자/소문자/숫자/특수문자 중 2가지 이상을 포함해야 합니다.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
