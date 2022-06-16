package io.github.ilkou.springbootchallenge.util;

import io.github.ilkou.springbootchallenge.exceptions.BadRequestException;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

public class ValidatorHelper {

    public static void validate(Object message) {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<Object>> violations = validator.validate(message);

        if (violations != null && !violations.isEmpty()) {
            throw new BadRequestException("Exchange message with validation error : " + buildViolationsMessage(violations));
        }
    }

    private static String buildViolationsMessage(Set<ConstraintViolation<Object>> violations) {
        StringBuilder errors = new StringBuilder();
        if (violations != null && !violations.isEmpty()) {
            for (ConstraintViolation<Object> violation : violations) {
                errors.append("[").append(violation.getPropertyPath()).append(" : ").append(violation.getMessage()).append("]");
            }
        }

        return errors.toString();
    }
}
