package org.ultra.rcrs.workflowservice.activity;

public interface TextActivity {

    String analyzeText(String text);

    String extractMetadata(String text);

    boolean moderateContent(String text);
}
