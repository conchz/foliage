package com.github.lavenderx.netty_rpc.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.HashMap;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@SuppressWarnings("unused")
public final class JsonUtils {
    private static ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        objectMapper.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
        objectMapper.configure(JsonGenerator.Feature.AUTO_CLOSE_JSON_CONTENT, false);
        objectMapper.disable(SerializationFeature.FLUSH_AFTER_WRITE_VALUE);
        objectMapper.disable(SerializationFeature.CLOSE_CLOSEABLE);
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        objectMapper.configure(JsonParser.Feature.IGNORE_UNDEFINED, true);
    }

    public static <T> byte[] serialize(T obj) {
        byte[] bytes = new byte[0];
        try {
            bytes = objectMapper.writeValueAsBytes(obj);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException(ex.getMessage(), ex);
        }

        return bytes;
    }

    public static <T> T deserialize(byte[] data, Class<T> clazz) {
        T obj = null;
        try {
            obj = objectMapper.readValue(data, clazz);
        } catch (IOException ex) {
            throw new IllegalStateException(ex.getMessage(), ex);
        }

        return obj;
    }


    public static <T> T jsonToObject(String json, Class<?> clazz) {
        T obj = null;
        JavaType javaType = objectMapper.getTypeFactory().constructType(clazz);
        try {
            obj = objectMapper.readValue(json, javaType);
        } catch (IOException ex) {
            throw new IllegalStateException(ex.getMessage(), ex);
        }

        return obj;
    }

    public static <T> T jsonToObjectList(String json,
                                         Class<?> collectionClass, Class<?>... elementClass) {
        T obj = null;
        JavaType javaType = objectMapper.getTypeFactory().constructParametricType(
                collectionClass, elementClass);
        try {
            obj = objectMapper.readValue(json, javaType);
        } catch (IOException ex) {
            throw new IllegalStateException(ex.getMessage(), ex);
        }

        return obj;
    }

    public static <T> T jsonToObjectHashMap(String json, Class<?> keyClass, Class<?> valueClass) {
        T obj = null;
        JavaType javaType = objectMapper.getTypeFactory().constructParametricType(HashMap.class, keyClass, valueClass);
        try {
            obj = objectMapper.readValue(json, javaType);
        } catch (IOException ex) {
            throw new IllegalStateException(ex.getMessage(), ex);
        }

        return obj;
    }

    public static String objectToJson(Object o) {
        String json = "";
        try {
            json = objectMapper.writeValueAsString(o);
        } catch (IOException ex) {
            throw new IllegalStateException(ex.getMessage(), ex);
        }

        return json;
    }
}
