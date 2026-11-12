package io.quarkiverse.arangodb.client.ext.deployment;

import java.util.List;
import java.util.Map;

import org.eclipse.microprofile.config.ConfigProvider;
import org.jboss.logging.Logger;
import org.testcontainers.utility.DockerImageName;

import io.quarkus.deployment.IsDockerWorking;
import io.quarkus.deployment.IsNormal;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.BuildSteps;
import io.quarkus.deployment.builditem.DevServicesResultBuildItem;
import io.quarkus.deployment.dev.devservices.DevServicesConfig;
import io.quarkus.runtime.configuration.ConfigUtils;

@BuildSteps(onlyIfNot = IsNormal.class, onlyIf = DevServicesConfig.Enabled.class)
public class DevServicesArangodbProcessor {
    private static final Logger log = Logger.getLogger(DevServicesArangodbProcessor.class);
    private final IsDockerWorking isDockerWorking = new IsDockerWorking(true);

    @BuildStep
    public DevServicesResultBuildItem startArangodbDevService() {
        if (!isDockerWorking.getAsBoolean()) {
            log.debug("Not starting Dev Services for Arangodb, as Docker is not working.");
            return null;
        }
        for (var name : ConfigProvider.getConfig().getPropertyNames()) {
            if (name.startsWith("quarkus.arangodb.hosts.")
                    || name.equals("quarkus.arangodb.user")
                    || name.equals("quarkus.arangodb.password")) {
                log.debug("Not starting Dev Services for Arangodb, as there is explicit configuration present.");
                return null;
            }
        }
        final boolean useSSL = ConfigUtils.getFirstOptionalValue(List.of("quarkus.arangodb.use-ssl"), Boolean.class)
                .orElse(Boolean.FALSE);

        // Quarkus starts the container, reuses it as long as useSSL does not change, and stops it
        return DevServicesResultBuildItem.<ArangodbContainer> owned()
                .feature(ArangodbClientExtProcessor.FEATURE)
                .serviceConfig(useSSL)
                .startable(() -> new ArangodbContainer(DockerImageName.parse("arangodb:3.11.5"), useSSL))
                .config(Map.of(
                        "quarkus.arangodb.hosts.hostname", "localhost",
                        "quarkus.arangodb.user", ArangodbContainer.USER,
                        "quarkus.arangodb.password", ArangodbContainer.PASSWORD))
                // the Function-based variant is only available in Quarkus 4
                .configProvider(Map.of("quarkus.arangodb.hosts.port", arangodb -> arangodb.getPort().toString()))
                .build();
    }
}
