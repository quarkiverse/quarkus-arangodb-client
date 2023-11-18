package io.quarkiverse.arangodb.client.ext.runtime;

import java.security.KeyStore;
import java.util.Objects;
import java.util.Optional;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import jakarta.inject.Singleton;

import io.quarkiverse.arangodb.client.ext.runtime.ArangodbClientConfig.SSLTruststore;

// https://github.com/arangodb/arangodb-java-driver/blob/main/driver/src/test/java/com/arangodb/example/ssl/SslExampleTest.java
@Singleton
public final class DefaultArangodbSSLContextProvider implements ArangodbSSLContextProvider {
    private final ArangodbClientConfig arangodbClientConfig;

    public DefaultArangodbSSLContextProvider(final ArangodbClientConfig arangodbClientConfig) {
        this.arangodbClientConfig = Objects.requireNonNull(arangodbClientConfig);
    }

    @Override
    public Optional<SSLContext> provide() throws ArangodbSSLContextException {
        if (Boolean.FALSE.equals(arangodbClientConfig.useSSL())) {
            return Optional.empty();
        }
        try {
            final SSLTruststore SSLTruststore = arangodbClientConfig.sslTruststore()
                    .orElseThrow(() -> new IllegalStateException("sslTruststore configuration is mandatory"));
            final String truststorePath = SSLTruststore.path();
            final String trustStorePassword = SSLTruststore.password();
            final KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
            ks.load(this.getClass().getResourceAsStream(truststorePath), trustStorePassword.toCharArray());

            final KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(ks, trustStorePassword.toCharArray());

            final TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(ks);

            final SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

            return Optional.of(sc);
        } catch (final Exception exception) {
            throw new ArangodbSSLContextException(exception);
        }
    }
}
