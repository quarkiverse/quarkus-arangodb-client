package io.quarkiverse.arangodb.client.ext.deployment;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.microprofile.config.ConfigProvider;
import org.jboss.logging.Logger;
import org.testcontainers.utility.DockerImageName;

import io.quarkus.deployment.IsDockerWorking;
import io.quarkus.deployment.IsNormal;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.BuildSteps;
import io.quarkus.deployment.builditem.CuratedApplicationShutdownBuildItem;
import io.quarkus.deployment.builditem.DevServicesResultBuildItem;
import io.quarkus.deployment.builditem.DevServicesResultBuildItem.RunningDevService;
import io.quarkus.deployment.builditem.LaunchModeBuildItem;
import io.quarkus.deployment.console.ConsoleInstalledBuildItem;
import io.quarkus.deployment.console.StartupLogCompressor;
import io.quarkus.deployment.dev.devservices.GlobalDevServicesConfig;
import io.quarkus.deployment.logging.LoggingSetupBuildItem;
import io.quarkus.runtime.configuration.ConfigUtils;

@BuildSteps(onlyIfNot = IsNormal.class, onlyIf = GlobalDevServicesConfig.Enabled.class)
public class DevServicesArangodbProcessor {
    private static final Logger log = Logger.getLogger(DevServicesArangodbProcessor.class);
    static volatile RunningDevService devService;
    static volatile boolean first = true;
    private final IsDockerWorking isDockerWorking = new IsDockerWorking(true);

    @BuildStep
    public DevServicesResultBuildItem startArangodbDevService(
            final LaunchModeBuildItem launchMode,
            final Optional<ConsoleInstalledBuildItem> consoleInstalledBuildItem,
            final CuratedApplicationShutdownBuildItem closeBuildItem,
            final LoggingSetupBuildItem loggingSetupBuildItem) {
        final StartupLogCompressor compressor = new StartupLogCompressor(
                (launchMode.isTest() ? "(test) " : "") + "Arangodb Dev Services Starting:",
                consoleInstalledBuildItem, loggingSetupBuildItem);
        try {
            devService = startArangodb();
            if (devService == null) {
                compressor.closeAndDumpCaptured();
            } else {
                compressor.close();
            }
        } catch (Throwable t) {
            compressor.closeAndDumpCaptured();
            throw new RuntimeException(t);
        }

        if (devService == null) {
            return null;
        }

        // Configure the watch dog
        if (first) {
            first = false;
            Runnable closeTask = () -> {
                if (devService != null) {
                    shutdownArangodb();
                }
                first = true;
                devService = null;
            };
            closeBuildItem.addCloseTask(closeTask, true);
        }
        return devService.toBuildItem();
    }

    private RunningDevService startArangodb() {
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
        final ArangodbContainer arangodb = new ArangodbContainer(
                DockerImageName.parse("arangodb:3.11.5"), useSSL);
        arangodb.start();
        return new RunningDevService("ARANGODB_CLIENT",
                arangodb.getContainerId(),
                arangodb::close,
                Map.of(
                        "quarkus.arangodb.hosts.hostname", "localhost",
                        "quarkus.arangodb.hosts.port", arangodb.getPort().toString(),
                        "quarkus.arangodb.user", arangodb.getUser(),
                        "quarkus.arangodb.password", arangodb.getPassword()));
    }

    private void shutdownArangodb() {
        if (devService != null) {
            try {
                devService.close();
            } catch (Throwable e) {
                log.error("Failed to stop arangodb", e);
            } finally {
                devService = null;
            }
        }
    }
}
