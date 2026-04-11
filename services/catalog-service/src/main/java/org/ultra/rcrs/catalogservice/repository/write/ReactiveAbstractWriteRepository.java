package org.ultra.rcrs.catalogservice.repository.write;

import lombok.RequiredArgsConstructor;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class ReactiveAbstractWriteRepository<T> {

    protected final R2dbcEntityTemplate template;
    private final Class<T> entityClass;

    public Mono<T> insert(T entity) {
        return template.insert(entity);
    }

}
