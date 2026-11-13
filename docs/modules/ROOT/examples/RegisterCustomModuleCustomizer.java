import jakarta.inject.Singleton;

import io.quarkus.jackson.JsonMapperBuilderCustomizer;
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;

@Singleton
public final class RegisterCustomModuleCustomizer implements JsonMapperBuilderCustomizer {
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
