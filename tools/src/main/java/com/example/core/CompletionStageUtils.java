package com.example.core;

import akka.Done;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

// TODO: move to core?
public final class CompletionStageUtils {
    private CompletionStageUtils() {
        throw new Error("no instances");
    }

    public static <R, X extends RuntimeException> Function<Optional<R>, R> throwIfEmpty(Supplier<X> exceptionThrower) {
        return result -> result.orElseThrow(exceptionThrower);
    }

    public static <T> Function<T, Done> accept(Consumer<T> f) {
        return t -> {
            f.accept(t);
            return Done.getInstance();
        };
    }

    public static CompletionStage<Done> doAll(CompletionStage<?>... stages) {
        return doAll(Arrays.asList(stages));
    }

    public static CompletionStage<Done> doAll(List<CompletionStage<?>> stages) {
        CompletionStage<Done> result = CompletableFuture.completedFuture(Done.getInstance());
        for (CompletionStage<?> stage : stages) {
            result = result.thenCombine(stage, (d1, d2) -> Done.getInstance());
        }
        return result;
    }
}
