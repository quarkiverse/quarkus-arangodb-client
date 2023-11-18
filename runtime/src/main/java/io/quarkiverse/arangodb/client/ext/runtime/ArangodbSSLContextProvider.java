package io.quarkiverse.arangodb.client.ext.runtime;

import java.util.Optional;

import javax.net.ssl.SSLContext;

public interface ArangodbSSLContextProvider {
    Optional<SSLContext> provide() throws ArangodbSSLContextException;
}
