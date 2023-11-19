package io.quarkiverse.arangodb.client.ext.test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collection;
import java.util.List;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.test.QuarkusUnitTest;

public class ArangodbClientExtTest extends CommonArangodbExtTest {
    @RegisterExtension
    static final QuarkusUnitTest unitTest = new QuarkusUnitTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class));

    @Test
    public void shouldListDatabases() {
        final Collection<String> databases = arangoDB.getDatabases();
        assertAll(
                () -> assertTrue(databases.size() > 0),
                () -> assertIterableEquals(databases, List.of("_system", "test")));
    }

}
