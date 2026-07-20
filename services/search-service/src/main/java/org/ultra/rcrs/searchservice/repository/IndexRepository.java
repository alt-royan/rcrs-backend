package org.ultra.rcrs.searchservice.repository;

public interface IndexRepository<T> {

    <D extends T> D get(String id, Class<D> clazz);

    <D extends T> boolean exists(String id, Class<D> clazz);

    <D extends T> void index(D doc);

    <D extends T> void delete(String id, Class<D> clazz);
}
