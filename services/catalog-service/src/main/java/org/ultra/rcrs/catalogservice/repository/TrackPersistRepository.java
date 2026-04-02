package org.ultra.rcrs.catalogservice.repository;

import jakarta.annotation.Nonnull;
import reactor.core.publisher.Mono;

public interface TrackPersistRepository<T> {

    @Nonnull
    <S extends T> Mono<S> save(@Nonnull S entity);
}
