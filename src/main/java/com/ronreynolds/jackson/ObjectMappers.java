package com.ronreynolds.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

public class ObjectMappers {
    public static class TypeSerDeTuple<T> {
        final Class<T> type;
        final StdSerializer<T> serializer;
        final StdDeserializer<? extends T> deserializer;

        public TypeSerDeTuple(Class<T> type, StdSerializer<T> serializer, StdDeserializer<T> deserializer) {
            this.type = type;
            this.serializer = serializer;
            this.deserializer = deserializer;
        }

        public static <T> TypeSerDeTuple<T> of(Class<T> type, StdSerializer<T> serializer, StdDeserializer<T> deserializer) {
            return new TypeSerDeTuple<>(type, serializer, deserializer);
        }

        public void addToModule(SimpleModule module) {
            module.addSerializer(type, serializer);
            module.addDeserializer(type, deserializer);
        }
    }

    // don't auto-close writers after they're read
    private static final ObjectMapper DEFAULT = new ObjectMapper().configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);

    // supports /*comments*/, 'single-quotes', fieldsWithoutQuotes:, and trailing commas,
    private static final ObjectMapper RELAXED = new ObjectMapper()
            .configure(Feature.ALLOW_COMMENTS, true)
            .configure(Feature.ALLOW_SINGLE_QUOTES, true)
            .configure(Feature.ALLOW_UNQUOTED_FIELD_NAMES, true) // - not available in 2.4.4, Feature.ALLOW_TRAILING_COMMA)
            .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    public static ObjectMapper defaultMapper() {
        return DEFAULT;
    }

    public static ObjectMapper relaxedMapper() {
        return RELAXED;
    }

    public static void addSerDe(TypeSerDeTuple<?>... tuples) {
        SimpleModule module = new SimpleModule();
        for (TypeSerDeTuple<?> tuple : tuples) {
            tuple.addToModule(module);
        }
        DEFAULT.registerModule(module);
        RELAXED.registerModule(module);
    }

    public static String toPrettyJson(Object o) {
        try {
            return defaultMapper().writerWithDefaultPrettyPrinter().writeValueAsString(o);
        } catch (IOException jpx) {
            throw new IllegalStateException("failed to generate JSON", jpx);
        }
    }

    public static void toPrettyJson(Object o, Writer writer) throws IOException {
        defaultMapper().writerWithDefaultPrettyPrinter().writeValue(writer, o);
    }

    public static void toPrettyJson(Object o, OutputStream outputStream) throws IOException {
        defaultMapper().writerWithDefaultPrettyPrinter().writeValue(outputStream, o);
    }

    public static String toCompactJson(Object o) {
        try {
            return defaultMapper().writeValueAsString(o);
        } catch (IOException jpx) {
            throw new IllegalStateException("failed to generate JSON", jpx);
        }
    }

    public static void toCompactJson(Object o, Writer writer) throws IOException {
        defaultMapper().writeValue(writer, o);
    }

    public static void toCompactJson(Object o, OutputStream outputStream) throws IOException {
        defaultMapper().writeValue(outputStream, o);
    }
}
