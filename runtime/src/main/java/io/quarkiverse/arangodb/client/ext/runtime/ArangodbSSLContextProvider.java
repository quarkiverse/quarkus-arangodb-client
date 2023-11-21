package io.quarkiverse.arangodb.client.ext.runtime;

import javax.net.ssl.SSLContext;

public interface ArangodbSSLContextProvider {
    SSLContext provide() throws ArangodbSSLContextException;
}
