package org.ultra.rcrs.catalogservice.repository.persist;

import jakarta.annotation.Nonnull;
import reactor.core.publisher.Mono;

public interface AlbumPersistRepository<T> {

    @Nonnull
    <S extends T> Mono<S> save(@Nonnull S entity);
}
