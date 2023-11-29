import javax.net.ssl.SSLContext;

import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;

import io.quarkiverse.arangodb.client.ext.runtime.ArangodbClientConfig;
import io.quarkiverse.arangodb.client.ext.runtime.ArangodbSSLContextException;
import io.quarkiverse.arangodb.client.ext.runtime.ArangodbSSLContextProvider;

@Singleton
public final class CustomArangodbSSLContextProviderProducer {
    static final class CustomArangodbSSLContextProvider implements ArangodbSSLContextProvider {

        @Override
        public SSLContext provide() throws ArangodbSSLContextException {
            throw new RuntimeException("TODO");
        }

    }

    @Singleton
    @Produces
    public ArangodbSSLContextProvider customArangodbSSLContextProviderProducer(
            final ArangodbClientConfig arangodbClientConfig) {
        return new CustomArangodbSSLContextProvider();
    }
}
