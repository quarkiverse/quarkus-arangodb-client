package io.quarkiverse.arangodb.client.ext.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.arangodb.entity.BaseDocument;
import com.arangodb.model.DocumentCreateOptions;

import io.quarkus.test.QuarkusUnitTest;

public class ArangodbClientExtUsingInternalSerdeTest extends CommonArangodbExtTest {
    @RegisterExtension
    static final QuarkusUnitTest unitTest = new QuarkusUnitTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
                    .addClass(Person.class));

    @Test
    public void shouldStoreAPerson() {
        // Given
        final Person givenPerson = new Person("Damien");

        // When
        final Person person = collection.insertDocument(givenPerson, new DocumentCreateOptions().returnNew(true)).getNew();

        // Then
        assertEquals(new Person("Damien"), person);
    }

    @Test
    public void shouldGetAPerson() {
        // Given
        final BaseDocument givenPerson = new BaseDocument();
        givenPerson.setKey("Damien");
        givenPerson.addAttribute("name", "Damien");
        collection.insertDocument(givenPerson);

        // When
        final Person person = collection.getDocument("Damien", Person.class);

        // Then
        assertEquals(new Person("Damien"), person);
    }
}
