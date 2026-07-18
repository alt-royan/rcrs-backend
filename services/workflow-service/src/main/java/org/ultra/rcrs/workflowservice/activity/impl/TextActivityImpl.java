package org.ultra.rcrs.workflowservice.activity.impl;

import org.springframework.stereotype.Component;
import org.ultra.rcrs.workflowservice.activity.TextActivity;
import org.ultra.rcrs.workflowservice.client.TextClient;

@Component
public class TextActivityImpl implements TextActivity {

    private final TextClient textClient;

    public TextActivityImpl(TextClient textClient) {
        this.textClient = textClient;
    }

    @Override
    public String analyzeText(String text) {
        return textClient.analyzeText(text);
    }

    @Override
    public String extractMetadata(String text) {
        return textClient.extractMetadata(text);
    }

    @Override
    public boolean moderateContent(String text) {
        return textClient.moderateContent(text);
    }
}
