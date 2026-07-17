package org.ultra.rcrs.pipeline;

public interface Handler<I, O> {
    O process(I input);
}