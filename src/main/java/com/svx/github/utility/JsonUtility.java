package com.svx.github.utility;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Map;

public class JsonUtility {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static String serialize(Map<String, String> entries) {
        try {
            return mapper.writeValueAsString(entries);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static Map<String, String> deserialize(String json) {
        try {
            return mapper.readValue(json, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}