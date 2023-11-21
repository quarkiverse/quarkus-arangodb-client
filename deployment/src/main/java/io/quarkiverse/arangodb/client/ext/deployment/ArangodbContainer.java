package io.quarkiverse.arangodb.client.ext.deployment;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.Transferable;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

import com.github.dockerjava.api.command.InspectContainerResponse;

public final class ArangodbContainer extends GenericContainer<ArangodbContainer> {
    private static final String IMAGE_NAME = "arangodb";
    private static final String ARANGO_ROOT_PASSWORD = "ARANGO_ROOT_PASSWORD";
    private static final String PASSWORD = "password";
    private static final DockerImageName IMAGE = DockerImageName.parse(IMAGE_NAME);
    private static final int ARANGODB_HTTP_PORT = 8529;
    private static final int ARANGODB_SSL_PORT = 8530;
    private final boolean useSSL;

    public ArangodbContainer(final DockerImageName dockerImageName, final boolean useSSL) {
        super(dockerImageName);
        dockerImageName.assertCompatibleWith(IMAGE);
        this.useSSL = useSSL;
        addExposedPort(ARANGODB_HTTP_PORT);
        withEnv(ARANGO_ROOT_PASSWORD, PASSWORD);
        if (useSSL) {
            addExposedPort(ARANGODB_SSL_PORT);
            final String setupSSLCommand = "sed -i '/endpoint = tcp:\\/\\/0.0.0.0:8529/a endpoint = ssl:\\/\\/0.0.0.0:8530' /tmp/arangod.conf\n"
                    +
                    "echo \"[ssl]\" >> /tmp/arangod.conf\n" +
                    "echo \"keyfile = var/lib/arangodb/server.pem\" >> /tmp/arangod.conf\n";
            withCopyToContainer(Transferable.of(setupSSLCommand, 0744), "/docker-entrypoint-initdb.d/setup-ssl.sh");
            withCopyFileToContainer(
                    MountableFile.forClasspathResource("/server.pem", 0744),
                    "/var/lib/arangodb/server.pem");
        }
        waitingFor(Wait.forLogMessage(".*is ready for business. Have fun.*", 1));
    }

    @Override
    protected void containerIsStarting(final InspectContainerResponse containerInfo, final boolean reused) {
        super.containerIsStarting(containerInfo, reused);
    }

    public String getUser() {
        return "root";
    }

    public String getPassword() {
        return PASSWORD;
    }

    public Integer getPort() {
        return useSSL ? getMappedPort(ARANGODB_SSL_PORT) : getMappedPort(ARANGODB_HTTP_PORT);
    }
}
