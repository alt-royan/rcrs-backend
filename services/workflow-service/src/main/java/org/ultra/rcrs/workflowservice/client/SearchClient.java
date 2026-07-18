package org.ultra.rcrs.workflowservice.client;

public interface SearchClient {

    void indexEntity(String entityType, String entityId);

    void removeEntity(String entityType, String entityId);

    void reindex(String entityType);
}
