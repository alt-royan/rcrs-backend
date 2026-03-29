package org.ultra.rcrs.catalogservice.validation.annotation;

import jakarta.validation.Constraint;
import org.ultra.rcrs.catalogservice.validation.Base62UUIDValidator;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Constraint(validatedBy = Base62UUIDValidator.class)
@Target({PARAMETER})
@Retention(RUNTIME)
public @interface ValidBase62UUID {

    String message() default "Invalid base62 id";

    Class[] groups() default {};

    Class[] payload() default {};
}