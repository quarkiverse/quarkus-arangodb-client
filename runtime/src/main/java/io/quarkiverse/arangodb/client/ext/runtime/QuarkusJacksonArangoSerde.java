package io.quarkiverse.arangodb.client.ext.runtime;

import java.lang.annotation.Annotation;
import java.util.Map;

import com.arangodb.serde.ArangoSerde;
import com.arangodb.serde.jackson.From;
import com.arangodb.serde.jackson.Id;
import com.arangodb.serde.jackson.Key;
import com.arangodb.serde.jackson.Rev;
import com.arangodb.serde.jackson.To;
import com.fasterxml.jackson.annotation.JsonInclude;

import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.PropertyName;
import tools.jackson.databind.cfg.MapperConfig;
import tools.jackson.databind.introspect.Annotated;
import tools.jackson.databind.introspect.JacksonAnnotationIntrospector;

/**
 * {@link ArangoSerde} based on the Jackson 3 {@link ObjectMapper} of Quarkus.
 * <p>
 * The serde provided by the ArangoDB driver only supports Jackson 2. This one behaves the same way, using a copy of the
 * Quarkus {@link ObjectMapper} so that the application one is not altered.
 */
public final class QuarkusJacksonArangoSerde implements ArangoSerde {

    private final ObjectMapper mapper;

    public QuarkusJacksonArangoSerde(final ObjectMapper objectMapper) {
        this.mapper = objectMapper.rebuild()
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .annotationIntrospector(new ArangoAnnotationIntrospector())
                .build();
    }

    @Override
    public byte[] serialize(final Object value) {
        return mapper.writeValueAsBytes(value);
    }

    @Override
    public <T> T deserialize(final byte[] content, final Class<T> clazz) {
        return mapper.readerFor(clazz).readValue(content);
    }

    /**
     * Maps the ArangoDB annotations to the document system attributes, as done by the driver Jackson 2 serde.
     */
    private static final class ArangoAnnotationIntrospector extends JacksonAnnotationIntrospector {

        private static final Map<Class<? extends Annotation>, String> MAPPINGS = Map.of(
                Id.class, "_id",
                Key.class, "_key",
                Rev.class, "_rev",
                From.class, "_from",
                To.class, "_to");

        private static final JsonInclude.Value NON_NULL = JsonInclude.Value.construct(JsonInclude.Include.NON_NULL, null);

        @Override
        public PropertyName findNameForSerialization(final MapperConfig<?> config, final Annotated annotated) {
            final PropertyName mapping = findMapping(annotated);
            return mapping != null ? mapping : super.findNameForSerialization(config, annotated);
        }

        @Override
        public PropertyName findNameForDeserialization(final MapperConfig<?> config, final Annotated annotated) {
            final PropertyName mapping = findMapping(annotated);
            return mapping != null ? mapping : super.findNameForDeserialization(config, annotated);
        }

        @Override
        public JsonInclude.Value findPropertyInclusion(final MapperConfig<?> config, final Annotated annotated) {
            for (Class<? extends Annotation> annotation : MAPPINGS.keySet()) {
                if (annotated.hasAnnotation(annotation)) {
                    return NON_NULL;
                }
            }
            return super.findPropertyInclusion(config, annotated);
        }

        private static PropertyName findMapping(final Annotated annotated) {
            for (Map.Entry<Class<? extends Annotation>, String> mapping : MAPPINGS.entrySet()) {
                if (annotated.hasAnnotation(mapping.getKey())) {
                    return PropertyName.construct(mapping.getValue());
                }
            }
            return null;
        }
    }
}
