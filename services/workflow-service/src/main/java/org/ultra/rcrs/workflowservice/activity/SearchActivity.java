package org.ultra.rcrs.workflowservice.activity;

public interface SearchActivity {

    void indexEntity(String entityType, String entityId);

    void removeEntity(String entityType, String entityId);

    void reindex(String entityType);
}
