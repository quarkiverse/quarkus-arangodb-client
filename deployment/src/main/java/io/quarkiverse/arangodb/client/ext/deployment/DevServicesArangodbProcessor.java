package io.quarkiverse.arangodb.client.ext.deployment;

import java.util.Map;
import java.util.Optional;

import org.jboss.logging.Logger;
import org.testcontainers.utility.DockerImageName;

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

@BuildSteps(onlyIfNot = IsNormal.class, onlyIf = GlobalDevServicesConfig.Enabled.class)
public class DevServicesArangodbProcessor {
    private static final Logger log = Logger.getLogger(DevServicesArangodbProcessor.class);
    static volatile RunningDevService devService;
    static volatile boolean first = true;

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
        final ArangodbContainer arangodb = new ArangodbContainer(
                DockerImageName.parse("arangodb:3.11.5"));
        arangodb.start();
        return new RunningDevService("ARANGODB_CLIENT",
                arangodb.getContainerId(),
                arangodb::close,
                Map.of(
                        "quarkus.arangodb.hosts[0].hostname", "localhost",
                        "quarkus.arangodb.hosts[0].port", arangodb.getHttpPort().toString(),
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
