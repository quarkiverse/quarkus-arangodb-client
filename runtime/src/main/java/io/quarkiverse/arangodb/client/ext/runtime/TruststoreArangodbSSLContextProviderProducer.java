package io.quarkiverse.arangodb.client.ext.runtime;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.util.Objects;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;

import io.quarkiverse.arangodb.client.ext.runtime.ArangodbClientConfig.SSLTruststore;
import io.quarkus.arc.DefaultBean;
import io.quarkus.runtime.util.ClassPathUtils;

// https://github.com/arangodb/arangodb-java-driver/blob/main/driver/src/test/java/com/arangodb/example/ssl/SslExampleTest.java
@Singleton
public final class TruststoreArangodbSSLContextProviderProducer {
    static final class TruststoreArangodbSSLContextProvider implements ArangodbSSLContextProvider {

        private final ArangodbClientConfig arangodbClientConfig;

        private TruststoreArangodbSSLContextProvider(final ArangodbClientConfig arangodbClientConfig) {
            this.arangodbClientConfig = Objects.requireNonNull(arangodbClientConfig);
        }

        @Override
        public SSLContext provide() throws ArangodbSSLContextException {
            try {
                final SSLTruststore SSLTruststore = arangodbClientConfig.sslTruststore()
                        .orElseThrow(() -> new IllegalStateException("sslTruststore configuration is mandatory"));
                final Path truststoreLocation = SSLTruststore.location();
                final String trustStorePassword = SSLTruststore.password();
                final KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
                final InputStream truststore = getResourceAsStream(truststoreLocation);
                ks.load(truststore, trustStorePassword.toCharArray());

                final KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
                kmf.init(ks, trustStorePassword.toCharArray());

                final TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                tmf.init(ks);

                final SSLContext sc = SSLContext.getInstance("TLS");
                sc.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

                return sc;
            } catch (final Exception exception) {
                throw new ArangodbSSLContextException(exception);
            }
        }

        private static InputStream getResourceAsStream(final Path path) throws IOException {
            final InputStream resource = Thread.currentThread().getContextClassLoader()
                    .getResourceAsStream(ClassPathUtils.toResourceName(path));
            if (resource != null) {
                return resource;
            } else {
                return Files.newInputStream(path);
            }
        }
    }

    @Singleton
    @Produces
    @DefaultBean
    public ArangodbSSLContextProvider arangodbSSLContextProviderProducer(final ArangodbClientConfig arangodbClientConfig) {
        return new TruststoreArangodbSSLContextProvider(arangodbClientConfig);
    }
}
