package org.ultra.rcrs.workflowservice.client.impl;

import org.springframework.stereotype.Component;
import org.ultra.rcrs.workflowservice.client.TextClient;

@Component
public class TextClientImpl implements TextClient {

    @Override
    public String analyzeText(String text) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public String extractMetadata(String text) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public boolean moderateContent(String text) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
