package org.ultra.rcrs.workflowservice.exception;

import org.springframework.http.HttpStatus;

public class WorkflowException extends RuntimeException {

    private final HttpStatus status;

    public WorkflowException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public WorkflowException(String message, Throwable cause, HttpStatus status) {
        super(message, cause);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
