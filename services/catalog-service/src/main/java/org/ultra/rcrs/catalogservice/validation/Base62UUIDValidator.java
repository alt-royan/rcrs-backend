package org.ultra.rcrs.catalogservice.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.ultra.rcrs.catalogservice.validation.annotation.ValidBase62UUID;
import org.ultra.rcrs.utils.Base62Utils;

public class Base62UUIDValidator implements ConstraintValidator<ValidBase62UUID, String> {

    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
        return Base62Utils.isValid(s);
    }
}
