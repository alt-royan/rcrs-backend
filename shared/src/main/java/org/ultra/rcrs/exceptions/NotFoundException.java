package org.ultra.rcrs.exceptions;


public class NotFoundException extends RuntimeException {

    private final static String messageTemplate = "%s with %s=%s was not found";

    public NotFoundException(Class<?> entityClass, String idValue) {
        this(entityClass, "id", idValue);
    }

    public NotFoundException(Class<?> entityClass, String param, Object paramValue) {
        super(String.format(messageTemplate, entityClass.getSimpleName(), param, paramValue.toString()));
    }
}
