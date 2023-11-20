package io.quarkiverse.arangodb.client.ext.runtime;

import java.util.Objects;

import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;

import com.arangodb.serde.ArangoSerde;
import com.arangodb.serde.jackson.internal.JacksonSerdeImpl;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkus.arc.DefaultBean;

@Singleton
public class QuarkusJacksonArangodbSerdeProducer {

    @Singleton
    @Produces
    @DefaultBean
    public ArangoSerde arangodbSerdeProducer(final ObjectMapper objectMapper) {
        Objects.requireNonNull(objectMapper);
        return new JacksonSerdeImpl(objectMapper);
    }
}
