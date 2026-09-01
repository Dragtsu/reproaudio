package com.player.reproaudio.utils;


import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.util.Set;

public class EntityValidator<T> {
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    Validator validator = factory.getValidator();

    public String[] validateEntity(T t) {
        String[] message = {"OK", "Acción realizada correctamente"};
        Set<ConstraintViolation<T>> errors = validator.validate(t);

        if (!errors.isEmpty()) {
            message = new String[]{"ERROR", errors.iterator().next().getMessage()};
        }

        return message;
    }
}