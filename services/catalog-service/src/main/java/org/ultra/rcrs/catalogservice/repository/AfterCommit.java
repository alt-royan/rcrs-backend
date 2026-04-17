package org.ultra.rcrs.catalogservice.repository;

import jakarta.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.reactive.TransactionSynchronization;
import org.springframework.transaction.reactive.TransactionSynchronizationManager;
import reactor.core.publisher.Mono;

@Slf4j
public class AfterCommit {

    public static Mono<Void> log(String var1, Object... var2) {
        return execute(() -> log.info(var1, var2));
    }

    public static Mono<Void> execute(Runnable runnable) {
        return TransactionSynchronizationManager.forCurrentTransaction()
                .doOnNext(manager ->
                        manager.registerSynchronization(
                                new TransactionSynchronization() {
                                    @Override
                                    @Nonnull
                                    public Mono<Void> afterCommit() {
                                        runnable.run();
                                        return Mono.empty();
                                    }
                                }
                        )
                )
                .then();
    }

    public static Mono<Void> execute(Mono<?> mono) {
        return TransactionSynchronizationManager.forCurrentTransaction()
                .doOnNext(manager ->
                        manager.registerSynchronization(
                                new TransactionSynchronization() {
                                    @Override
                                    @Nonnull
                                    public Mono<Void> afterCommit() {
                                        return mono.then();
                                    }
                                }
                        )
                )
                .then();
    }
}
