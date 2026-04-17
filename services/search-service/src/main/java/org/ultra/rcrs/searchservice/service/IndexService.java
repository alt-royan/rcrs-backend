package org.ultra.rcrs.searchservice.service;

import java.util.List;

public interface IndexService<T> {

    void index(T doc);

    void delete(String id);

    void indexBatch(List<T> batch);

}
