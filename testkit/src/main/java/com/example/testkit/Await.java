package com.example.testkit;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 *
 */
public class Await {

    /**
     * Will await for <code>completionStage</code> to complete or timeout after 5 seconds. If the
     * <code>completionStage</code> completes with an ExecutionException, the cause will be wrapped and in a
     * RuntimeException and the ExecutionException will be discarded. If <code>completionStage</code> completes with
     * any other type of exception it will be kept and wrapped inside a RuntimeException.
     *
     * @param completionStage
     * @param <T> the type of the expected value.
     * @return the result of the completed <code>completionStage</code>.
     */
    public static <T> T result(CompletionStage<T> completionStage) {
        try {
            return completionStage.toCompletableFuture().get(5, SECONDS);
        } catch (ExecutionException e) {
            throw new RuntimeException(e.getCause());
        } catch (InterruptedException | TimeoutException e) {
            throw new RuntimeException(e);
        }

    }

}
