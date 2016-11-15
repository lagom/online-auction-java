package com.example.auction.item.impl;

import akka.Done;
import com.datastax.driver.core.BoundStatement;
import com.lightbend.lagom.javadsl.persistence.cassandra.CassandraReadSide;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

final class CompletionStageUtils {
    private CompletionStageUtils() {
        throw new Error("no instances");
    }

    static <R, X extends RuntimeException> Function<Optional<R>, R> throwIfEmpty(Supplier<X> exceptionThrower) {
        return result -> result.orElseThrow(exceptionThrower);
    }

    static CompletionStage<List<BoundStatement>> completedStatements(BoundStatement... statements) {
        return CassandraReadSide.completedStatements(Arrays.asList(statements));
    }

    static <T> Function<T, Done> accept(Consumer<T> f) {
        return t -> {
            f.accept(t);
            return Done.getInstance();
        };
    }

    static <T> CompletionStage<Done> doAll(CompletionStage<T>... stages) {
        return doAll(Arrays.asList(stages));
    }

    static <T> CompletionStage<Done> doAll(List<CompletionStage<T>> stages) {
        CompletionStage<Done> result = CompletableFuture.completedFuture(Done.getInstance());
        for (CompletionStage<?> stage : stages) {
            result = result.thenCombine(stage, (d1, d2) -> Done.getInstance());
        }
        return result;
    }
}
