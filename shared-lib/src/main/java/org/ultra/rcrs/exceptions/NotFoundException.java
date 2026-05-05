package org.ultra.rcrs.exceptions;


import org.ultra.rcrs.utils.Url62;

import java.util.UUID;

public class NotFoundException extends RuntimeException {

    public NotFoundException(String prefix, UUID id) {
        super(String.format("%s with id %s not found", prefix, Url62.encode(id)));
    }

    public NotFoundException(String prefix, String id) {
        super(String.format("%s with id %s not found", prefix, id));
    }
}
