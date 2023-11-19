package io.quarkiverse.arangodb.client.ext.runtime;

import java.util.Objects;

import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;

import org.eclipse.microprofile.context.ManagedExecutor;

import com.arangodb.ArangoDB;
import com.arangodb.serde.ArangoSerde;

import io.smallrye.mutiny.infrastructure.Infrastructure;

@Singleton
public class ArangodbClient {
    private final ArangodbSSLContextProvider arangodbSSLContextProvider;
    private final Instance<ManagedExecutor> managedExecutorInstance;
    private final Instance<ArangoSerde> arangoSerdeInstance;

    public ArangodbClient(final ArangodbSSLContextProvider arangodbSSLContextProvider,
            final Instance<ManagedExecutor> managedExecutorInstance,
            final Instance<ArangoSerde> arangoSerdeInstance) {
        this.arangodbSSLContextProvider = Objects.requireNonNull(arangodbSSLContextProvider);
        this.managedExecutorInstance = Objects.requireNonNull(managedExecutorInstance);
        this.arangoSerdeInstance = Objects.requireNonNull(arangoSerdeInstance);
    }

    @Produces
    @Singleton
    public ArangoDB produceArangodbClient(final ArangodbClientConfig arangodbClientConfig) {
        if (arangodbClientConfig.hosts().isEmpty()) {
            throw new IllegalStateException("At least one host is expected");
        }
        final ArangoDB.Builder clientBuilder = new ArangoDB.Builder();
        arangodbClientConfig.hosts().forEach(host -> clientBuilder.host(host.hostname(), host.port()));
        arangodbClientConfig.protocol().ifPresent(clientBuilder::protocol);
        arangodbClientConfig.timeout().ifPresent(clientBuilder::timeout);
        arangodbClientConfig.user().ifPresent(clientBuilder::user);
        clientBuilder.password(arangodbClientConfig.password());
        arangodbClientConfig.jwt().ifPresent(clientBuilder::jwt);
        try {
            arangodbSSLContextProvider.provide().ifPresentOrElse(
                    sslContext -> clientBuilder.useSsl(true).sslContext(sslContext),
                    () -> clientBuilder.useSsl(false));
        } catch (final ArangodbSSLContextException arangodbSSLContextException) {
            throw new IllegalStateException("Unable to load SSL truststore", arangodbSSLContextException.getCause());
        }
        arangodbClientConfig.verifyHost().ifPresent(clientBuilder::verifyHost);
        arangodbClientConfig.chunkSize().ifPresent(clientBuilder::chunkSize);
        arangodbClientConfig.maxConnections().ifPresent(clientBuilder::maxConnections);
        arangodbClientConfig.connectionTtl().ifPresent(clientBuilder::connectionTtl);
        arangodbClientConfig.keepAliveInterval().ifPresent(clientBuilder::keepAliveInterval);
        arangodbClientConfig.acquireHostList().ifPresent(clientBuilder::acquireHostList);
        arangodbClientConfig.acquireHostListInterval().ifPresent(clientBuilder::acquireHostListInterval);
        arangodbClientConfig.loadBalancingStrategy().ifPresent(clientBuilder::loadBalancingStrategy);
        arangodbClientConfig.responseQueueTimeSamples().ifPresent(clientBuilder::responseQueueTimeSamples);
        if (arangoSerdeInstance.isResolvable()) {
            clientBuilder.serde(arangoSerdeInstance.get());
        } else if (arangoSerdeInstance.isAmbiguous()) {
            throw new IllegalStateException("Multiple implementations of ArangoSerde. Only a default one is expected");
        }
        return clientBuilder
                .asyncExecutor(managedExecutorInstance.isResolvable() ? managedExecutorInstance.get()
                        : Infrastructure.getDefaultWorkerPool())
                .build();
    }
}
