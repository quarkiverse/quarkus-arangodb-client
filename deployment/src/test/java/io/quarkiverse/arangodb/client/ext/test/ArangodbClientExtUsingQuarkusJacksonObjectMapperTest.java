package io.quarkiverse.arangodb.client.ext.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.List;

import jakarta.inject.Singleton;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.arangodb.entity.BaseDocument;
import com.arangodb.model.DocumentCreateOptions;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;

import io.quarkus.builder.Version;
import io.quarkus.jackson.ObjectMapperCustomizer;
import io.quarkus.maven.dependency.Dependency;
import io.quarkus.test.QuarkusUnitTest;

public class ArangodbClientExtUsingQuarkusJacksonObjectMapperTest extends CommonArangodbExtTest {
    @RegisterExtension
    static final QuarkusUnitTest unitTest = new QuarkusUnitTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
                    .addClass(Person.class)
                    .addClass(RegisterCustomModuleCustomizer.class))
            .setForcedDependencies(List.of(
                    Dependency.of("io.quarkus", "quarkus-jackson", Version.getVersion())));

    @Singleton
    public static class RegisterCustomModuleCustomizer implements ObjectMapperCustomizer {
        private static final String PERSON_SERIALIZER_ADDED_PREFIX = "MyNameIs";
        private static final String PERSON_DESERIALIZER_ADDED_PREFIX = "Hello";

        @Override
        public void customize(final ObjectMapper mapper) {
            final SimpleModule module = new SimpleModule("PersonModule");
            module.addDeserializer(Person.class, new PersonDeserializer());
            module.addSerializer(Person.class, new PersonSerializer());
            mapper.registerModule(module);
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
