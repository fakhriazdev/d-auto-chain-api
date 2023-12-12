package com.danamon.autochain.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import java.util.Set;

@Component
public class ValidationUtil {
    private final Validator validator;

    @Autowired
    public ValidationUtil(Validator validator) {
        this.validator = validator;
    }

    public void validate(Object obj) {
        Set<ConstraintViolation<Object>> result = validator.validate(obj);
        if (!result.isEmpty()) {
            throw new ConstraintViolationException(result);
        }
    }
}
