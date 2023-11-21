package io.quarkiverse.arangodb.client.ext.runtime;

import java.util.Objects;

import javax.net.ssl.SSLContext;

import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;

import org.eclipse.microprofile.context.ManagedExecutor;

import com.arangodb.ArangoDB;
import com.arangodb.serde.ArangoSerde;

import io.smallrye.mutiny.infrastructure.Infrastructure;

@Singleton
public class ArangodbClientProducer {
    @Produces
    @Singleton
    public ArangoDB produceArangodbClient(final ArangodbClientConfig arangodbClientConfig,
            final ArangodbSSLContextProvider arangodbSSLContextProvider,
            final Instance<ManagedExecutor> managedExecutorInstance,
            final Instance<ArangoSerde> arangoSerdeInstance) {
        Objects.requireNonNull(arangodbClientConfig);
        Objects.requireNonNull(arangodbSSLContextProvider);
        Objects.requireNonNull(managedExecutorInstance);
        Objects.requireNonNull(arangoSerdeInstance);
        if (managedExecutorInstance.isAmbiguous()) {
            throw new IllegalStateException("Multiple implementations of ManagedExecutor. Only one is expected");
        }
        if (arangoSerdeInstance.isAmbiguous()) {
            throw new IllegalStateException("Multiple implementations of ArangoSerde. Only one is expected");
        }
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
        clientBuilder.useSsl(arangodbClientConfig.useSSL());
        if (arangodbClientConfig.useSSL()) {
            try {
                final SSLContext sslContext = Objects.requireNonNull(arangodbSSLContextProvider.provide());
                clientBuilder.sslContext(sslContext);
            } catch (final ArangodbSSLContextException arangodbSSLContextException) {
                throw new IllegalStateException("Unable to load SSL truststore", arangodbSSLContextException.getCause());
            }
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
        }
        return clientBuilder
                .asyncExecutor(managedExecutorInstance.isResolvable() ? managedExecutorInstance.get()
                        : Infrastructure.getDefaultWorkerPool())
                .build();
    }
}
