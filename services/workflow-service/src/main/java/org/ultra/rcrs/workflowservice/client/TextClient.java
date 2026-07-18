package org.ultra.rcrs.workflowservice.client;

public interface TextClient {

    String analyzeText(String text);

    String extractMetadata(String text);

    boolean moderateContent(String text);
}
