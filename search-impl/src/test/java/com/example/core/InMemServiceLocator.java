package com.example.core;

import com.lightbend.lagom.internal.testkit.TestServiceLocatorPort;
import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.ServiceLocator;
import scala.compat.java8.FutureConverters;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;


// Don't use this on your code.
// This class is work-in-progress for a feature that might be merged into Lagom core.
// https://github.com/lagom/lagom/issues/322
@Singleton
public class InMemServiceLocator implements ServiceLocator {

    private TestServiceLocatorPort testPort;
    private Map<String, Integer> service = new HashMap<>();

    // This constructor is exploiting a weakness in the scala-java interop: `TestServiceLocatorPort` is a scala class
    // that's "package private" and is on an 'internal' lagom package it should not be used.
    //
    // http://www.scala-lang.org/files/archive/spec/2.11/05-classes-and-objects.html#private
    //
    @Inject
    public InMemServiceLocator(TestServiceLocatorPort testPort) {
        this.testPort = testPort;
    }

    public void registerService(String name, int port) {
        service.put(name, port);
    }

    @Override
    public CompletionStage<Optional<URI>> locate(String name) {
        return FutureConverters
                .toJava(testPort.port())
                .thenApply(defaultPort -> {
                            int port = service.getOrDefault(name, (Integer) defaultPort);
                            try {
                                return Optional.of(new URI("http://localhost:" + port));
                            } catch (URISyntaxException e) {
                                throw new RuntimeException(e);
                            }
                        }
                );
    }

    @Override
    public CompletionStage<Optional<URI>> locate(String name, Descriptor.Call<?, ?> serviceCall) {
        return locate(name);
    }

    @Override
    public <T> CompletionStage<Optional<T>> doWithService(String serviceName, Descriptor.Call<?, ?> serviceCall, Function<URI, CompletionStage<T>> block) {
        return serviceCall
                .circuitBreaker()
                .map(cb -> doWithServiceImpl(serviceName, serviceCall, uri -> block.apply(uri)))
                .orElseGet(() -> doWithServiceImpl(serviceName, serviceCall, block)
                );
    }

    protected <T> CompletionStage<Optional<T>> doWithServiceImpl(String name, Descriptor.Call<?, ?> serviceCall, Function<URI, CompletionStage<T>> block) {
        return locate(name, serviceCall)
                .thenCompose(uri -> {
                    if (uri.isPresent()) {
                        return block.apply(uri.get()).thenApply(Optional::of);
                    } else {
                        return CompletableFuture.completedFuture(Optional.empty());
                    }
                });
    }


}
