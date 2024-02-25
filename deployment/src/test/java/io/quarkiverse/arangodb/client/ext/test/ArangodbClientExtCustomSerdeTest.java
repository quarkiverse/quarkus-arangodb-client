package io.quarkiverse.arangodb.client.ext.test;

import static com.fasterxml.jackson.databind.DeserializationFeature.USE_BIG_INTEGER_FOR_INTS;
import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;

import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.arangodb.ContentType;
import com.arangodb.entity.BaseDocument;
import com.arangodb.model.DocumentCreateOptions;
import com.arangodb.serde.ArangoSerde;
import com.arangodb.serde.jackson.JacksonSerde;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;

import io.quarkus.test.QuarkusUnitTest;

public class ArangodbClientExtCustomSerdeTest extends CommonArangodbExtTest {
    @RegisterExtension
    static final QuarkusUnitTest unitTest = new QuarkusUnitTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
                    .addClass(Person.class)
                    .addClass(CustomArangodbSerde.class));

    @Singleton
    static final class CustomArangodbSerde {
        private static final String PERSON_SERIALIZER_ADDED_PREFIX = "MyNameIs";
        private static final String PERSON_DESERIALIZER_ADDED_PREFIX = "Hello";

        @Singleton
        @Produces
        public ArangoSerde arangodbSerdeProducer() {
            return JacksonSerde.of(ContentType.JSON)
                    .configure(mapper -> {
                        mapper.configure(WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED, true);
                        mapper.configure(USE_BIG_INTEGER_FOR_INTS, true);
                        final SimpleModule module = new SimpleModule("PersonModule");
                        module.addDeserializer(Person.class, new CustomArangodbSerde.PersonDeserializer());
                        module.addSerializer(Person.class, new CustomArangodbSerde.PersonSerializer());
                        mapper.registerModule(module);
                    });
        }

        private static class PersonSerializer extends JsonSerializer<Person> {
            @Override
            public void serialize(final Person value, final JsonGenerator gen, final SerializerProvider serializers)
                    throws IOException {
                gen.writeStartObject();
                gen.writeFieldName("name");
                gen.writeString(PERSON_SERIALIZER_ADDED_PREFIX + value.getName());
                gen.writeEndObject();
            }
        }

        private static class PersonDeserializer extends JsonDeserializer<Person> {
            @Override
            public Person deserialize(final JsonParser parser, final DeserializationContext ctx) throws IOException {
                final JsonNode rootNode = parser.getCodec().readTree(parser);
                final JsonNode nameNode = rootNode.get("name");
                final String name;
                if (nameNode != null && nameNode.isTextual()) {
                    name = PERSON_DESERIALIZER_ADDED_PREFIX + nameNode.asText();
                } else {
                    name = null;
                }
                return new Person(name);
            }
        }
    }

    @Test
    public void shouldStoreAPerson() {
        // Given
        final Person givenPerson = new Person("Damien");

        // When
        final Person person = collection.insertDocument(givenPerson, new DocumentCreateOptions().returnNew(true)).getNew();

        // Then
        assertEquals(new Person("HelloMyNameIsDamien"), person);
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
        assertEquals(new Person("HelloDamien"), person);
    }
}
