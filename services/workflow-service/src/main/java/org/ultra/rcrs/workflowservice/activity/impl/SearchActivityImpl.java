package org.ultra.rcrs.workflowservice.activity.impl;

import org.springframework.stereotype.Component;
import org.ultra.rcrs.workflowservice.activity.SearchActivity;
import org.ultra.rcrs.workflowservice.client.SearchClient;

@Component
public class SearchActivityImpl implements SearchActivity {

    private final SearchClient searchClient;

    public SearchActivityImpl(SearchClient searchClient) {
        this.searchClient = searchClient;
    }

    @Override
    public void indexEntity(String entityType, String entityId) {
        searchClient.indexEntity(entityType, entityId);
    }

    @Override
    public void removeEntity(String entityType, String entityId) {
        searchClient.removeEntity(entityType, entityId);
    }

    @Override
    public void reindex(String entityType) {
        searchClient.reindex(entityType);
    }
}
