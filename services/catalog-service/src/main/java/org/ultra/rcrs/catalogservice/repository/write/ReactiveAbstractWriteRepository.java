package org.ultra.rcrs.catalogservice.repository.write;

import lombok.RequiredArgsConstructor;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static org.springframework.data.relational.core.query.Criteria.where;
import static org.springframework.data.relational.core.query.Query.query;

@RequiredArgsConstructor
public class ReactiveAbstractWriteRepository<T> {

    protected final R2dbcEntityTemplate template;
    private final Class<T> entityClass;

    public Mono<T> insert(T entity) {
        return template.insert(entity);
    }

    public Mono<Long> delete(UUID id) {
        return template.delete(query(where("id").is(id)), entityClass);
    }

}
