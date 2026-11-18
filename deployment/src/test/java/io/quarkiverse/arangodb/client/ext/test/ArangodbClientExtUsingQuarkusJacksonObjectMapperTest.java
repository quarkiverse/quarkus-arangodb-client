package io.quarkiverse.arangodb.client.ext.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import jakarta.inject.Singleton;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.arangodb.entity.BaseDocument;
import com.arangodb.model.DocumentCreateOptions;

import io.quarkus.builder.Version;
import io.quarkus.jackson.JsonMapperBuilderCustomizer;
import io.quarkus.maven.dependency.Dependency;
import io.quarkus.test.QuarkusExtensionTest;
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;

public class ArangodbClientExtUsingQuarkusJacksonObjectMapperTest extends CommonArangodbExtTest {
    @RegisterExtension
    static final QuarkusExtensionTest unitTest = new QuarkusExtensionTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
                    .addClass(Person.class)
                    .addClass(RegisterCustomModuleCustomizer.class))
            .setForcedDependencies(List.of(
                    Dependency.of("io.quarkus", "quarkus-jackson", Version.getVersion())));

    @Singleton
    static final class RegisterCustomModuleCustomizer implements JsonMapperBuilderCustomizer {
        private static final String PERSON_SERIALIZER_ADDED_PREFIX = "MyNameIs";
        private static final String PERSON_DESERIALIZER_ADDED_PREFIX = "Hello";

        @Override
        public void customize(final JsonMapper.Builder builder) {
            final SimpleModule module = new SimpleModule("PersonModule");
            module.addDeserializer(Person.class, new PersonDeserializer());
            module.addSerializer(Person.class, new PersonSerializer());
            builder.addModule(module);
        }

        private static class PersonSerializer extends ValueSerializer<Person> {
            @Override
            public void serialize(final Person value, final JsonGenerator gen, final SerializationContext context) {
                gen.writeStartObject();
                gen.writeName("name");
                gen.writeString(PERSON_SERIALIZER_ADDED_PREFIX + value.getName());
                gen.writeEndObject();
            }
        }

        private static class PersonDeserializer extends ValueDeserializer<Person> {
            @Override
            public Person deserialize(final JsonParser parser, final DeserializationContext ctx) {
                final JsonNode rootNode = ctx.readTree(parser);
                final JsonNode nameNode = rootNode.get("name");
                final String name;
                if (nameNode != null && nameNode.isString()) {
                    name = PERSON_DESERIALIZER_ADDED_PREFIX + nameNode.asString();
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
