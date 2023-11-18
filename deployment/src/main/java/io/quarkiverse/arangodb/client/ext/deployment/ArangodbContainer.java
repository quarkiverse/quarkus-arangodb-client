package io.quarkiverse.arangodb.client.ext.deployment;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

public final class ArangodbContainer extends GenericContainer<ArangodbContainer> {
    private static final String IMAGE_NAME = "arangodb";
    private static final String ARANGODB_ROOT_PASSWORD = "ARANGO_ROOT_PASSWORD";
    private static final String PASSWORD = "password";
    private static final DockerImageName IMAGE = DockerImageName.parse(IMAGE_NAME);
    private static final int ARANGODB_HTTP_PORT = 8529;

    public ArangodbContainer(final DockerImageName dockerImageName) {
        super(dockerImageName);
        dockerImageName.assertCompatibleWith(IMAGE);
    }

    @Override
    protected void configure() {
        super.configure();
        addExposedPorts(ARANGODB_HTTP_PORT);
        withEnv(ARANGODB_ROOT_PASSWORD, PASSWORD);
        waitingFor(Wait.forLogMessage(".*is ready for business. Have fun.*", 1));
    }

    public String getUser() {
        return "root";
    }

    public String getPassword() {
        return PASSWORD;
    }

    public Integer getHttpPort() {
        return getMappedPort(ARANGODB_HTTP_PORT);
    }
}
