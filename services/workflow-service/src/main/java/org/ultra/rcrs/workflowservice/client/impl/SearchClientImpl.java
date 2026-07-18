package org.ultra.rcrs.workflowservice.client.impl;

import org.springframework.stereotype.Component;
import org.ultra.rcrs.workflowservice.client.SearchClient;

@Component
public class SearchClientImpl implements SearchClient {

    @Override
    public void indexEntity(String entityType, String entityId) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void removeEntity(String entityType, String entityId) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void reindex(String entityType) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
