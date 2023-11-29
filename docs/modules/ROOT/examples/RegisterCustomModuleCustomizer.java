import java.io.IOException;

import jakarta.inject.Singleton;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;

import io.quarkus.jackson.ObjectMapperCustomizer;

@Singleton
public final class RegisterCustomModuleCustomizer implements ObjectMapperCustomizer {
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
