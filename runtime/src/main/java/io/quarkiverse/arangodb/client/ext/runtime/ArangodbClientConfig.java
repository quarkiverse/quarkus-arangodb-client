package io.quarkiverse.arangodb.client.ext.runtime;

import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

import com.arangodb.Protocol;
import com.arangodb.entity.LoadBalancingStrategy;

import io.quarkus.runtime.annotations.*;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithUnnamedKey;

// https://docs.arangodb.com/3.11/develop/drivers/java/reference-version-7/driver-setup/
@ConfigMapping(prefix = "quarkus.arangodb")
@ConfigRoot(phase = ConfigPhase.RUN_TIME)
public interface ArangodbClientConfig {
    /**
     * list of hosts to connect on. At least one is expected.
     */
    @ConfigDocSection
    @WithUnnamedKey("<default>")
    @ConfigDocMapKey("host-name")
    Map<String, HostConfig> hosts();

    @ConfigGroup
    interface HostConfig {
        /**
         * host hostname
         */
        String hostname();

        /**
         * host port
         */
        Integer port();
    }

    /**
     * communication protocol, possible values are: VST, HTTP_JSON, HTTP_VPACK, HTTP2_JSON, HTTP2_VPACK, (default: HTTP2_JSON)
     */
    Optional<Protocol> protocol();

    /**
     * connection and request timeout (ms), (default 0, no timeout)
     */
    Optional<Integer> timeout();

    /**
     * username for authentication, (default: root)
     */
    Optional<String> user();

    /**
     * password for authentication
     */
    Optional<String> password();

    /**
     * use SSL connection, (default: false)
     */
    @WithDefault("false")
    Boolean useSSL();

    /**
     * sslTruststore configuration
     */
    Optional<SSLTruststore> sslTruststore();

    @ConfigGroup
    interface SSLTruststore {

        /**
         * location where to find the cert file
         */
        Path location();

        /**
         * trustStore password
         */
        String password();
    }

    /**
     * enable hostname verification, (HTTP only, default: true)
     */
    Optional<Boolean> verifyHost();

    /**
     * VST chunk size in bytes, (default: 30000)
     */
    Optional<Integer> chunkSize();

    /**
     * max number of connections per host, (default: 1 VST, 1 HTTP/2, 20 HTTP/1.1)
     */
    Optional<Integer> maxConnections();

    /**
     * max lifetime of a connection (ms), (default: no ttl)
     */
    Optional<Long> connectionTtl();

    /**
     * VST keep-alive interval (s), (default: no keep-alive probes will be sent)
     */
    Optional<Integer> keepAliveInterval();

    /**
     * acquire the list of available hosts, (default: false)
     */
    Optional<Boolean> acquireHostList();

    /**
     * acquireHostList interval (ms), (default: 3_600_000, 1 hour)
     */
    Optional<Integer> acquireHostListInterval();

    /**
     * load balancing strategy, possible values are: NONE, ROUND_ROBIN, ONE_RANDOM, (default: NONE)
     */
    Optional<LoadBalancingStrategy> loadBalancingStrategy();

    /**
     * amount of samples kept for queue time metrics, (default: 10)
     */
    Optional<Integer> responseQueueTimeSamples();
}
